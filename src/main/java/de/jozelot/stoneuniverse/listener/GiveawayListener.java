package de.jozelot.stoneuniverse.listener;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.mechanics.giveaway.Giveaway;
import de.jozelot.stoneuniverse.mechanics.giveaway.GiveawayEnterError;
import de.jozelot.stoneuniverse.mechanics.giveaway.GiveawayUI;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GiveawayListener extends ListenerAdapter {

    private final StoneUniverse bot;

    public GiveawayListener(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modelId = event.getModalId();

        if (modelId.startsWith("giveaway:setup:")) {
            String titel = event.getValue("giveaway:titel").getAsString();

            var descMapping = event.getValue("giveaway:description");
            String beschreibung = (descMapping != null) ? descMapping.getAsString() : "Keine Beschreibung";

            var limitMapping = event.getValue("giveaway:entry_limit");
            String limitRaw = (limitMapping != null) ? limitMapping.getAsString() : "";

            var winnerMapping = event.getValue("giveaway:winner");
            String winnerRaw = (winnerMapping != null) ? winnerMapping.getAsString() : "";

            String rollDateRaw = event.getValue("giveaway:roll_date").getAsString();

            int entryLimit = 0; // 0 = Unbegrenzt
            int winnerCount = 1; // Standard = 1
            long drawDate;

            try {
                if (!limitRaw.isEmpty()) {
                    entryLimit = Integer.parseInt(limitRaw);
                    if (entryLimit < 1) throw new NumberFormatException();
                }

                if (!winnerRaw.isEmpty()) {
                    winnerCount = Integer.parseInt(winnerRaw);
                    if (winnerCount < 1) throw new NumberFormatException();
                }

                drawDate = parseDurationToTimestamp(rollDateRaw);

            } catch (NumberFormatException e) {
                event.replyComponents(Messages.getError("Invalid EntryLimit: Has to be more than 1")).useComponentsV2().setEphemeral(true).queue();
                return;
            } catch (IllegalArgumentException e) {
                event.replyComponents(Messages.getError("Invalid Timeformat: Should be: `1d 5h` (d=Days, h=Hours, m=Minutes, s=Seconds)")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            long creatorId = event.getUser().getIdLong();

            long channelId = Long.parseLong(modelId.replace("giveaway:setup:", ""));

            Giveaway giveaway = bot.getBootstrap().getGiveawayService().createGiveaway(creatorId, titel, beschreibung, entryLimit, winnerCount, drawDate, channelId);

            var shardManager = bot.getBootstrap().getBotManager().getShardManager();
            var channel = shardManager.getTextChannelById(channelId);

            if (channel != null) {
                channel.sendMessageComponents(bot.getBootstrap().getGiveawayService().getGiveawayUI().getGiveawayMessage(giveaway))
                        .useComponentsV2()
                        .setAllowedMentions(Collections.emptyList())
                        .queue(successMessage -> {
                            long generatedMessageId = successMessage.getIdLong();

                            giveaway.setMessageId(generatedMessageId);

                            event.replyComponents(bot.getBootstrap().getGiveawayService().getGiveawayUI().getSetupSuccess()).useComponentsV2().setEphemeral(true).queue();
                        }, throwable -> {
                            event.replyComponents(Messages.getError("Can't send messages in this channel")).useComponentsV2().setEphemeral(true).queue();
                        });
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getCustomId();
        var giveawayService = bot.getBootstrap().getGiveawayService();
        var shardManager = bot.getBootstrap().getBotManager().getShardManager();

        if (buttonId.startsWith("giveaway:enter:")) {
            String giveawayId = buttonId.replace("giveaway:enter:", "") ;

            Giveaway giveaway = giveawayService.getGiveawayById(giveawayId);

            if (giveaway == null) {
                event.replyComponents(giveawayService.getGiveawayUI().getGiveawayEnterError(GiveawayEnterError.ENDED, giveaway)).useComponentsV2().setEphemeral(true).queue();
                return;
            }
            boolean enterSuccess = giveaway.addEntry(event.getMember().getIdLong());

            if (!enterSuccess) {
                event.replyComponents(giveawayService.getGiveawayUI().getGiveawayEnterError(GiveawayEnterError.ALREAD_IN, giveaway)).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            event.getMessage().editMessageComponents(bot.getBootstrap().getGiveawayService().getGiveawayUI().getGiveawayMessage(giveaway))
                    .useComponentsV2()
                    .setAllowedMentions(Collections.emptyList())
                    .queue();
            event.replyComponents(giveawayService.getGiveawayUI().getGiveawayEnterSuccess(giveaway)).useComponentsV2().setEphemeral(true).queue();
        } else if (buttonId.startsWith("giveaway:leave:")) {
            String giveawayId = buttonId.replace("giveaway:leave:", "") ;

            Giveaway giveaway = giveawayService.getGiveawayById(giveawayId);

            if (giveaway == null) {
                event.replyComponents(giveawayService.getGiveawayUI().getGiveawayEnterError(GiveawayEnterError.ENDED, giveaway)).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            boolean leaveSuccess = giveaway.removeEntry(event.getMember().getIdLong());

            if (!leaveSuccess) {
                event.replyComponents(Messages.getError("Can't remove from giveaway: Member was not a participant")).useComponentsV2().setEphemeral(true).queue();
                return;
            }
            TextChannel textChannel = shardManager.getTextChannelById(giveaway.getChannelId());
            if (textChannel == null) {
                event.replyComponents(Messages.getError("Giveaway textchannel not found")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            textChannel.retrieveMessageById(giveaway.getMessageId()).queue(message -> {
                message.editMessageComponents(giveawayService.getGiveawayUI().getGiveawayMessage(giveaway))
                        .setAllowedMentions(Collections.emptyList())
                        .useComponentsV2()
                        .queue();

                event.replyComponents(giveawayService.getGiveawayUI().getGiveawayLeaveSuccess(giveaway)).useComponentsV2().queue();

            }, throwable -> {
                event.replyComponents(Messages.getError("Giveaway message not found")).useComponentsV2().setEphemeral(true).queue();
            });
        }
    }

    private long parseDurationToTimestamp(String input) throws IllegalArgumentException {
        long totalMillis = 0;

        Matcher matcher = Pattern.compile("(\\d+)([dhms])").matcher(input.toLowerCase().trim());

        boolean found = false;
        while (matcher.find()) {
            found = true;
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "d" -> totalMillis += value * 24 * 60 * 60 * 1000;
                case "h" -> totalMillis += value * 60 * 60 * 1000;
                case "m" -> totalMillis += value * 60 * 1000;
                case "s" -> totalMillis += value * 1000;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Ungültiges Zeitformat.");
        }

        return System.currentTimeMillis() + totalMillis;
    }
}
