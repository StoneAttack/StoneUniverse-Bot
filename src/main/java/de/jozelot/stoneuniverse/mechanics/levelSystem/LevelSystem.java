package de.jozelot.stoneuniverse.mechanics.levelSystem;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class LevelSystem {

    private static final Logger logger = LoggerFactory.getLogger(LevelSystem.class);
    private final StoneUniverse bot;

    private final Map<Long, UserLevel> cachedLevels = new ConcurrentHashMap<>(); // User ID | UserLevel Objekt

    public LevelSystem(StoneUniverse bot) {
        this.bot = bot;
    }

    public void initialize() {
        logger.info("Loading user levels from database into cache...");

        CompletableFuture.runAsync(() -> {
            try (Connection conn = bot.getBootstrap().getDatabaseLoader().getConnection()) {
                String sql = "SELECT user_id, xp, level FROM user_levels;";

                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        long userId = rs.getLong("user_id");
                        int xp = rs.getInt("xp");
                        int level = rs.getInt("level");

                        UserLevel ul = new UserLevel(bot, userId, xp, level);
                        cachedLevels.put(userId, ul);
                    }
                    logger.info("Successfully cached {} user levels from database.", cachedLevels.size());
                }
            } catch (SQLException e) {
                logger.error("Failed to load user levels from database!", e);
            }
        });
    }

    public UserLevel getUserLevel(long userId) {
        return cachedLevels.computeIfAbsent(userId, id -> new UserLevel(bot, id, 0, 0));
    }

    public List<UserLevel> getTopLevel(int count) {
        return cachedLevels.values().stream()
                .sorted((o1, o2) -> Integer.compare(o2.getLevel(), o1.getLevel()))
                .limit(count)
                .toList();
    }

    public int getRank(UserLevel userLevel) {
        return getRank(userLevel.getUserId());
    }

    public int getRank(long userId) {
        UserLevel targetUser = cachedLevels.get(userId);
        if (targetUser == null) {
            return -1;
        }

        long betterPlayers = cachedLevels.values().stream()
                .filter(ul -> ul.getLevel() > targetUser.getLevel() ||
                        (ul.getLevel() == targetUser.getLevel() && ul.getXp() > targetUser.getXp()))
                .count();

        return (int) betterPlayers + 1;
    }

    public void saveUserLevel(UserLevel ul) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = bot.getBootstrap().getDatabaseLoader().getConnection()) {
                String sql = "INSERT INTO user_levels (user_id, xp, level) VALUES (?, ?, ?) " +
                        "ON CONFLICT (user_id) DO UPDATE SET xp = EXCLUDED.xp, level = EXCLUDED.level;";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, ul.getUserId());
                    stmt.setInt(2, ul.getXp());
                    stmt.setInt(3, ul.getLevel());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                logger.error("Failed to save user level to database for user {}", ul.getUserId(), e);
            }
        });
    }

    public void checkVoiceChannels() {
        var shardManager = bot.getBootstrap().getBotManager().getShardManager();
        if (shardManager == null) return;

        for (var jda : shardManager.getShards()) {
            for (var guild : jda.getGuilds()) {
                for (var voiceChannel : guild.getVoiceChannels()) {

                    for (var member : voiceChannel.getMembers()) {
                        UserLevel userLevel = getUserLevel(member.getIdLong());
                        if (userLevel.isEligibleForXp()) {
                            if (userLevel.addMessageXp(15,25)) {
                                voiceChannel.sendMessageComponents(Messages.getLevelUp(member, userLevel.getLevel(), userLevel.getLevel(), userLevel.getXpNeeded())).useComponentsV2().queue();
                            }
                        }
                    }

                }
            }
        }
    }

    public void shutdown() {
        logger.info("Saving all user levels to database...");

        try (Connection conn = bot.getBootstrap().getDatabaseLoader().getConnection()) {
            String sql = "INSERT INTO user_levels (user_id, xp, level) VALUES (?, ?, ?) " +
                    "ON CONFLICT (user_id) DO UPDATE SET xp = EXCLUDED.xp, level = EXCLUDED.level;";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (UserLevel ul : cachedLevels.values()) {
                    stmt.setLong(1, ul.getUserId());
                    stmt.setInt(2, ul.getXp());
                    stmt.setInt(3, ul.getLevel());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                logger.info("All user levels saved!");
            }
        } catch (SQLException e) {
            logger.error("Error while trying to save all user levels!", e);
        }
    }
}