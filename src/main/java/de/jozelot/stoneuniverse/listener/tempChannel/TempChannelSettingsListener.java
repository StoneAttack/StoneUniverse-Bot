package de.jozelot.stoneuniverse.listener.tempChannel;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.mechanics.tempChannels.TempChannel;
import de.jozelot.stoneuniverse.mechanics.tempChannels.TempChannelSystem;
import de.jozelot.stoneuniverse.mechanics.tempChannels.TempChannelUI;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

public class TempChannelSettingsListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TempChannelSettingsListener.class);
    private final StoneUniverse bot;
    private final TempChannelUI ui;

    public TempChannelSettingsListener(StoneUniverse bot) {
        this.bot = bot;
        this.ui = bot.getBootstrap().getTempChannelSystem().getTempChannelUI();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getCustomId();

        TempChannel tempChannel = bot.getBootstrap().getTempChannelSystem().getTempChannel(event.getChannelIdLong());
        if (tempChannel == null) return;

        VoiceChannel channel = event.getChannel().asVoiceChannel();
        if (tempChannel.getOwnerId() != event.getMember().getIdLong()) {
            event.replyComponents(Messages.getError("Can't change settings: Member not owner of tempchannel!")).useComponentsV2().setEphemeral(true).queue();
            return;
        }

        if (buttonId.startsWith("tempchannel:change_name:")) {
            Modal modal = ui.getChangeName(event.getChannelId(), event.getChannel().getName());
            event.replyModal(modal).queue();
        } else if (buttonId.startsWith("tempchannel:lock:")) {
            Role everyoneRole = channel.getGuild().getPublicRole();
            Message message = event.getMessage();

            channel.upsertPermissionOverride(everyoneRole).deny(Permission.VOICE_CONNECT).queue(success -> {

                message.editMessageComponents(ui.getSettingsMessage(tempChannel.getOwnerId(), channel, true))
                        .useComponentsV2()
                        .queue();

                event.replyComponents(ui.getChangeSuccess("Kanal gesperrt")).useComponentsV2().setEphemeral(true).queue();
            });
        } else if (buttonId.startsWith("tempchannel:unlock:")) {
            Role everyoneRole = channel.getGuild().getPublicRole();
            Message message = event.getMessage();

            channel.upsertPermissionOverride(everyoneRole).grant(Permission.VOICE_CONNECT).queue(success -> {
                message.editMessageComponents(ui.getSettingsMessage(tempChannel.getOwnerId(), channel, false)).useComponentsV2().queue();
                event.replyComponents(ui.getChangeSuccess("Kanal entsperrt")).useComponentsV2().setEphemeral(true).queue();
            });
        } else if (buttonId.startsWith("tempchannel:change_limit:")) {
            Modal modal = ui.getChangeLimit(event.getChannelId(), event.getChannel().asAudioChannel().getUserLimit());
            event.replyModal(modal).queue();

        } else if (buttonId.startsWith("tempchannel:kick:")) {
            Modal modal = ui.getKickModal(channel.getId());
            event.replyModal(modal).queue();
        } else if (buttonId.startsWith("tempchannel:change_owner:")) {
            Modal modal = ui.getOwnerModal(channel.getId());
            event.replyModal(modal).queue();
        } else if (buttonId.startsWith("tempchannel:ban:")) {
            Modal modal = ui.getBanModal(channel.getId());
            event.replyModal(modal).queue();
        } else if (buttonId.startsWith("tempchannel:invite:")) {
            Modal modal = ui.getInviteModal(channel.getId());
            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();

        TempChannel tempChannel = bot.getBootstrap().getTempChannelSystem().getTempChannel(event.getChannelIdLong());
        if (tempChannel == null) return;
        long channelId = tempChannel.getChannelId();
        VoiceChannel channel = event.getGuild().getVoiceChannelById(channelId);

        if (tempChannel.getOwnerId() != event.getMember().getIdLong()) {
            event.replyComponents(Messages.getError("Can't change settings: Member not owner of tempchannel!")).useComponentsV2().setEphemeral(true).queue();
            return;
        }

        if (modalId.startsWith("tempchannel:change_name:")) {
            String newName = event.getValue("tempchannel:change_name:value").getAsString();

            if (channel != null) {
                channel.getManager().setName(newName).queue(success -> {
                    event.replyComponents(ui.getChangeSuccess("Namensänderung")).useComponentsV2().setEphemeral(true).queue();
                }, throwable -> {
                    event.replyComponents(Messages.getError("Name change not possible: Bot ran into a rate limit! You are changing the name to fast!")).useComponentsV2().setEphemeral(true).queue();
                });
            }

        } else if (modalId.startsWith("tempchannel:change_limit:")) {
            String limitRaw = event.getValue("tempchannel:change_limit:value").getAsString();

            if (channel == null) return;

            int neuesLimit;
            try {
                neuesLimit = Integer.parseInt(limitRaw);
                if (neuesLimit < 0 || neuesLimit > 99) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                event.replyComponents(Messages.getError("Can't apply limit: Number has to be from 0 to 99")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            Role everyoneRole = channel.getGuild().getPublicRole();
            var override = channel.getPermissionOverride(everyoneRole);
            boolean isCurrentlyLocked = override != null && override.getDenied().contains(Permission.VOICE_CONNECT);

            channel.getManager().setUserLimit(neuesLimit).queue(success -> {
                event.getMessage().editMessageComponents(ui.getSettingsMessage(tempChannel.getOwnerId(), channel, isCurrentlyLocked)).useComponentsV2().queue();

                String responseText = (neuesLimit == 0) ? "Nutzerlimit wurde aufgehoben." : "Nutzerlimit wurde auf " + neuesLimit + " gesetzt.";
                event.replyComponents(ui.getChangeSuccess(responseText)).useComponentsV2().setEphemeral(true).queue();
            }, throwable -> {
                event.replyComponents(Messages.getError("Can't apply limit!")).useComponentsV2().setEphemeral(true).queue();
            });
        } else if (modalId.startsWith("tempchannel:kick:")) {
            var mapping = event.getValue("tempchannel:kick:value");

            if (mapping == null) {
                event.replyComponents(Messages.getError("No choice found")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            var members = mapping.getAsMentions().getMembers();
            Member member = members.getFirst();

            var voiceState = member.getVoiceState();

            if (voiceState == null || voiceState.getChannel().getIdLong() != event.getMember().getVoiceState().getChannel().getIdLong()) {
                event.replyComponents(Messages.getError("Can't disconnect user: User is not in a voicechannel")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            if (member.getIdLong() == event.getMember().getIdLong()) {
                event.replyComponents(Messages.getError("Can't disconnect user: You can't kick yourself")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            event.getGuild().kickVoiceMember(member).queue(success -> {
                event.replyComponents(ui.getKickSuccess(member.getIdLong())).useComponentsV2().setEphemeral(true).queue();
            }, throwable -> {
                event.replyComponents(Messages.getError("Can't disconnect user: Missing permission")).useComponentsV2().setEphemeral(true).queue();
            });

            // User not in a voicechannel
        } else if (modalId.startsWith("tempchannel:change_owner:")) {
            var mapping = event.getValue("tempchannel:change_owner:value");

            if (mapping == null) {
                event.replyComponents(Messages.getError("No choice found")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            var members = mapping.getAsMentions().getMembers();
            if (members.isEmpty()) return;

            Member member = members.getFirst();

            if (member.getIdLong() == event.getMember().getIdLong()) {
                event.replyComponents(Messages.getError("Can't change owner: You are already the owner!")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            var targetVoiceState = member.getVoiceState();
            var ownerVoiceState = event.getMember().getVoiceState();

            if (targetVoiceState == null || !targetVoiceState.inAudioChannel() ||
                    ownerVoiceState == null || !ownerVoiceState.inAudioChannel() ||
                    targetVoiceState.getChannel().getIdLong() != ownerVoiceState.getChannel().getIdLong()) {

                event.replyComponents(Messages.getError("Can't change owner: User is not in your voicechannel!")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            channel.upsertPermissionOverride(member).grant(Permission.MANAGE_CHANNEL).queue(success -> {
                channel.upsertPermissionOverride(event.getMember()).deny(Permission.MANAGE_CHANNEL).queue(success2 -> {
                    event.replyComponents(ui.getNewOwner(event.getMember().getIdLong(), member.getIdLong()))
                            .useComponentsV2()
                            .setAllowedMentions(Collections.EMPTY_LIST)
                            .mentionUsers(member.getIdLong())
                            .queue();
                    tempChannel.setNewOwner(member.getIdLong());
                }, throwable -> {
                    event.replyComponents(Messages.getError("Could not apply permissions for old owner!")).useComponentsV2().setEphemeral(true).queue();
                });
            }, throwable -> {
                event.replyComponents(Messages.getError("Could not apply permissions for new owner!")).useComponentsV2().setEphemeral(true).queue();
            });
        } else if (modalId.startsWith("tempchannel:ban:")) {
            var mapping = event.getValue("tempchannel:ban:value");

            if (mapping == null) {
                event.replyComponents(Messages.getError("No choice found")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            var members = mapping.getAsMentions().getMembers();
            if (members.isEmpty()) return;

            Member member = members.getFirst();
            var targetVoiceState = member.getVoiceState();

            if (channel == null) return;

            if (targetVoiceState != null && targetVoiceState.inAudioChannel() &&
                    targetVoiceState.getChannel().getIdLong() == channel.getIdLong()) {

                event.getGuild().kickVoiceMember(member).queue();
            }

            channel.upsertPermissionOverride(member).deny(Permission.VOICE_CONNECT).queue(success -> {
                event.replyComponents(ui.getBanSuccess(member.getIdLong())).useComponentsV2().setEphemeral(true).queue();
            }, throwable -> {
                event.replyComponents(Messages.getError("Could not apply ban permissions!")).useComponentsV2().setEphemeral(true).queue();
            });
        } else if (modalId.startsWith("tempchannel:invite:")) {
            var mapping = event.getValue("tempchannel:invite:value");

            if (mapping == null) {
                event.replyComponents(Messages.getError("No choice found")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            var members = mapping.getAsMentions().getMembers();
            if (members.isEmpty()) return;

            Member member = members.getFirst();

            if (member.getIdLong() == event.getMember().getIdLong()) {
                event.replyComponents(Messages.getError("Can't invite yourself")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            if (channel == null) return;

            channel.upsertPermissionOverride(member).grant(Permission.VOICE_CONNECT).queue(success -> {
                event.replyComponents(ui.getInviteSuccess(member.getIdLong())).useComponentsV2().setEphemeral(true).queue();
                member.getUser().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessageComponents(ui.getInvite(bot.getBootstrap().getConfig(), tempChannel, member.getIdLong())).useComponentsV2().queue();
                });
            }, throwable -> {
                event.replyComponents(Messages.getError("Could not apply invite because of a permission lack!")).useComponentsV2().setEphemeral(true).queue();
            });
        }
    }
}
