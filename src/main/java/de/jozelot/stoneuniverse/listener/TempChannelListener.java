package de.jozelot.stoneuniverse.listener;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.mechanics.tempChannels.TempChannel;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class TempChannelListener extends ListenerAdapter {

    private final StoneUniverse bot;

    public TempChannelListener(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getChannelJoined() != null) {
            long joinId = bot.getBootstrap().getConfig().getSystem().getTempChannel().getChannelId();
            if (event.getChannelJoined().getIdLong() == joinId) {
                bot.getBootstrap().getTempChannelSystem().create(event.getMember());
            }
        }

        if (event.getChannelLeft() != null) {
            if (!(event.getChannelLeft() instanceof VoiceChannel channelLeft)) {
                return;
            }

            TempChannel tempChannel = bot.getBootstrap().getTempChannelSystem().getTempChannel(channelLeft.getIdLong());
            if (tempChannel == null) {
                return;
            }

            if (channelLeft.getMembers().isEmpty()) {
                bot.getBootstrap().getTempChannelSystem().remove(tempChannel.getChannelId());
                return;
            }

            if (tempChannel.getOwnerId() == event.getMember().getIdLong()) {
                Member newOwner = channelLeft.getMembers().get(0);
                Member oldOwner = event.getMember();

                tempChannel.setNewOwner(newOwner.getIdLong());

                var oldOverride = channelLeft.getPermissionOverride(oldOwner);
                if (oldOverride != null) {
                    oldOverride.delete().queue();
                }

                channelLeft.upsertPermissionOverride(newOwner)
                        .grant(Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.PRIORITY_SPEAKER, Permission.VOICE_DEAF_OTHERS, Permission.VOICE_SET_STATUS)
                        .queue();
            }
        }
    }
}
