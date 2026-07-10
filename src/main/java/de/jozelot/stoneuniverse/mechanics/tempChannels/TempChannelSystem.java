package de.jozelot.stoneuniverse.mechanics.tempChannels;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.core.BotBootstrap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

public class TempChannelSystem {

    private static final Logger logger = LoggerFactory.getLogger(TempChannelSystem.class);
    private final StoneUniverse bot;
    private final TempChannelUI tempChannelUI;
    private final Map<Long, TempChannel> tempChannels = new HashMap<>(); // ChannelId | TempChannel Object

    public TempChannelSystem(StoneUniverse bot) {
        this.bot = bot;
        this.tempChannelUI = new TempChannelUI(bot);
    }

    public void initialize() {
        logger.info("Loading temp channels from database...");

        CompletableFuture.runAsync(() -> {
            var shardManager = bot.getBootstrap().getBotManager().getShardManager();

            try (Connection conn = bot.getBootstrap().getDatabaseLoader().getConnection()) {
                String sql = "SELECT channel_id, owner_id FROM temp_channels;";

                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        long channelId = rs.getLong("channel_id");
                        long ownerId = rs.getLong("owner_id");

                        VoiceChannel voiceChannel = shardManager.getVoiceChannelById(channelId);

                        if (voiceChannel == null) {
                            deleteFromDatabase(channelId);
                            continue;
                        }

                        if (voiceChannel.getMembers().isEmpty()) {
                            voiceChannel.delete().queue();
                            deleteFromDatabase(channelId);
                            logger.info("Temp-Channels with no members cleaned: {}", channelId);
                            continue;
                        }

                        TempChannel tc = new TempChannel(ownerId, channelId);
                        tempChannels.put(channelId, tc);
                    }
                    logger.info("Temp Channels synced! Loaded: {}", tempChannels.size());
                }
            } catch (SQLException e) {
                logger.error("Error while trying to load Temp Channels!", e);
            }
        });
    }

    private void saveToDatabase(long channelId, long ownerId) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = bot.getBootstrap().getDatabaseLoader().getConnection()) {
                String sql = "INSERT INTO temp_channels (channel_id, owner_id) VALUES (?, ?) ON CONFLICT (channel_id) DO UPDATE SET owner_id = EXCLUDED.owner_id;";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, channelId);
                    stmt.setLong(2, ownerId);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                logger.error("Fehler beim Speichern des Temp-Channels in der DB", e);
            }
        });
    }

    private void deleteFromDatabase(long channelId) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = bot.getBootstrap().getDatabaseLoader().getConnection()) {
                String sql = "DELETE FROM temp_channels WHERE channel_id = ?;";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, channelId);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                logger.error("Fehler beim Löschen des Temp-Channels aus der DB", e);
            }
        });
    }


    public void create(Member member) {
        if (!member.getVoiceState().inAudioChannel()) return;

        Guild guild = member.getGuild();

        String nameFormat = bot.getBootstrap().getConfig().getSystem().getTempChannel().getDefaultFormat();
        String safeUsername = Matcher.quoteReplacement(member.getEffectiveName());

        String finalChannelName = nameFormat.replace("{username}", safeUsername);

        if (finalChannelName.length() > 100) {
            finalChannelName = finalChannelName.substring(0, 100);
        }

        long categoryId = bot.getBootstrap().getConfig().getSystem().getTempChannel().getCategoryId();
        Category category = null;

        if (categoryId != 0) {
            category = guild.getCategoryById(categoryId);
        }

        ChannelAction<VoiceChannel> channelAction;

        if (category != null) {
            channelAction = category.createVoiceChannel(finalChannelName);
        } else {
            channelAction = guild.createVoiceChannel(finalChannelName);
        }

        channelAction.queue(voiceChannel -> {

            TempChannel tempChannel = new TempChannel(member.getIdLong(), voiceChannel.getIdLong());
            tempChannels.put(voiceChannel.getIdLong(), tempChannel);
            saveToDatabase(voiceChannel.getIdLong(), member.getIdLong());

            voiceChannel.upsertPermissionOverride(member)
                    .grant(Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.PRIORITY_SPEAKER, Permission.VOICE_DEAF_OTHERS, Permission.VOICE_SET_STATUS)
                    .queue();

            guild.moveVoiceMember(member, voiceChannel).queue(
                    success -> logger.info("Moved {} to their new temp channel.", member.getEffectiveName()),
                    throwable -> logger.warn("Could not move user, maybe they left the voice chat in the meantime.")
            );
            sendMessage(tempChannel, voiceChannel);

        }, throwable -> {
            logger.error("Failed to create temporary voice channel!", throwable);
        });
    }

    public void remove(long channelId) {
        tempChannels.remove(channelId);
        deleteFromDatabase(channelId);

        VoiceChannel voiceChannel = bot.getBootstrap().getBotManager().getShardManager().getVoiceChannelById(channelId);

        if (voiceChannel != null) {
            voiceChannel.delete().queue(
                    success -> logger.info("Temp Channel removed successfully: {}", channelId),
                    throwable -> logger.error("Error while deleting Temp Channel: {}", channelId, throwable)
            );
        }
    }

    public boolean isTemp(long channelId) {
        return tempChannels.containsKey(channelId);
    }

    public Map<Long, TempChannel> getTempChannels() {
        return Collections.unmodifiableMap(tempChannels);
    }

    public Collection<TempChannel> getTempChannelValues() {
        return Collections.unmodifiableCollection(tempChannels.values());
    }

    public TempChannel getTempChannel(long channelId) {
        return tempChannels.get(channelId);
    }
    public TempChannelUI getTempChannelUI() {
        return tempChannelUI;
    }

    public void sendMessage(TempChannel tempChannel, VoiceChannel voiceChannel) {
        Container infoMessage = bot.getBootstrap().getTempChannelSystem().getTempChannelUI().getSettingsMessage(tempChannel.getOwnerId(), voiceChannel);
        voiceChannel.sendMessageComponents(infoMessage).useComponentsV2().queue();
    }
}
