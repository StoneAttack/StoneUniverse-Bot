package de.jozelot.stoneuniverse.commands;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.interfaces.Command;
import de.jozelot.stoneuniverse.mechanics.giveaway.Giveaway;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.modals.Modal;

import java.util.Collections;
import java.util.List;

public class GiveawayCommand implements Command {

    private final StoneUniverse bot;

    public GiveawayCommand(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("giveaway", "\uD83C\uDF89 | Admin: Verwalte und erstelle Giveaways").addSubcommands(
                new SubcommandData("setup", "\uD83C\uDF89 | Admin: Erstelle ein Giveaway"),

                new SubcommandData("end", "\uD83C\uDF89 | Admin: Beende ein Giveaway vorzeitig und lose es aus")
                            .addOption(OptionType.STRING, "id", "Die ID des Giveaways", true, true),

                new SubcommandData("cancel", "\uD83C\uDF89 | Admin: Breche ein Giveaway ab")
                        .addOption(OptionType.STRING, "id", "Die ID des Giveaways", true, true),

                new SubcommandData("list", "\uD83C\uDF89 | Admin: Liste alle aktuellen Giveaways auf"),

                new SubcommandData("reroll", "\uD83C\uDF89 | Admin: Lose die Gewinner eines Giveaways neu aus")
                        .addOption(OptionType.STRING, "id", "Die ID des Giveaways", true, true)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String subcommandName = event.getSubcommandName();
        if (subcommandName.equalsIgnoreCase("setup")) {
            setup(event);
        } else if (subcommandName.equalsIgnoreCase("end")) {
            end(event);
        } else if (subcommandName.equalsIgnoreCase("cancel")) {
            cancel(event);
        } else if (subcommandName.equalsIgnoreCase("list")) {
            list(event);
        } else {
            event.replyComponents(Messages.getError("Unknown Subcommand: " + subcommandName)).useComponentsV2().setEphemeral(true).queue();
        }
    }

    @Override
    public void autoComplete(CommandAutoCompleteInteractionEvent event) {
        String subcommandName = event.getSubcommandName();

        if (event.getFocusedOption().getName().equalsIgnoreCase("id")) {
            var giveawayService = bot.getBootstrap().getGiveawayService();
            String currentInput = event.getFocusedOption().getValue().toLowerCase();

            List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = giveawayService.getGiveaways().stream()
                    .filter(g -> g.getTitel().toLowerCase().contains(currentInput) || g.getId().toLowerCase().contains(currentInput))
                    .filter(g -> {
                        if (subcommandName.equalsIgnoreCase("reroll")) {
                            return g.hasEnded();
                        } else {
                            return !g.hasEnded();
                        }
                    })
                    .limit(25)
                    .map(g -> {
                        String icon = g.hasEnded() ? "🔒" : "🎉";
                        String name = String.format("%s %s | ID: %s (%d👤)",
                                icon, g.getTitel(), g.getId(), g.getEntryCount());

                        if (name.length() > 100) {
                            name = name.substring(0, 96) + "...";
                        }
                        return new net.dv8tion.jda.api.interactions.commands.Command.Choice(name, g.getId());
                    })
                    .toList();

            event.replyChoices(choices).queue();
        }
    }

    /**
     * Setup subcommand
     * @param event
     */
    public void setup(SlashCommandInteractionEvent event) {
        Modal modal = bot.getBootstrap().getGiveawayService().getGiveawayUI().getSetupModal(event.getChannelId());
        event.replyModal(modal).queue();
    }

    public void list(SlashCommandInteractionEvent event) {
        event.replyComponents(bot.getBootstrap().getGiveawayService().getGiveawayUI().getGiveaways(event.getGuild().getId())).useComponentsV2().setEphemeral(true).queue();
    }

    /**
     * End Subcommand
     * @param event
     */
    public void end(SlashCommandInteractionEvent event) {
        String giveawayId = event.getOption("id").getAsString();
        var giveawayService = bot.getBootstrap().getGiveawayService();
        var shardManager = bot.getBootstrap().getBotManager().getShardManager();

        Giveaway giveaway = giveawayService.getGiveawayById(giveawayId);

        if (giveaway == null) {
            event.replyComponents(Messages.getError("Giveaway not found")).useComponentsV2().setEphemeral(true).queue();
            return;
        }
        if (giveaway.hasEnded()) {
            event.replyComponents(Messages.getError("Giveaway has already ended")).useComponentsV2().setEphemeral(true).queue();
            return;
        }
        if (!giveaway.roll()) {
            event.replyComponents(Messages.getError("Giveaway has already ended")).useComponentsV2().setEphemeral(true).queue();
            return;
        }

        TextChannel textChannel = shardManager.getTextChannelById(giveaway.getChannelId());
        if (textChannel == null) {
            event.replyComponents(Messages.getError("Giveaway textchannel not found")).useComponentsV2().setEphemeral(true).queue();
            return;
        }
        textChannel.retrieveMessageById(giveaway.getMessageId()).queue(message -> {
            message.editMessageComponents(giveawayService.getGiveawayUI().getGiveawayMessageRolled(giveaway))
                    .setAllowedMentions(Collections.emptyList())
                    .useComponentsV2()
                    .queue();

            message.replyComponents(giveawayService.getGiveawayUI().getGiveawayRollSuccess(giveaway))
                            .useComponentsV2()
                            .queue();

            event.replyComponents(giveawayService.getGiveawayUI().getGiveawayPreRollReply(giveaway)).useComponentsV2().setEphemeral(true).queue();

        }, throwable -> {
            event.replyComponents(Messages.getError("Giveaway message not found")).useComponentsV2().setEphemeral(true).queue();
        });
    }

    /**
     * Cancel Subcommand
     * @param event
     */
    public void cancel(SlashCommandInteractionEvent event) {
        String giveawayId = event.getOption("id").getAsString();
        var giveawayService = bot.getBootstrap().getGiveawayService();
        var shardManager = bot.getBootstrap().getBotManager().getShardManager();

        Giveaway giveaway = giveawayService.getGiveawayById(giveawayId);

        if (giveaway == null) {
            event.replyComponents(Messages.getError("Giveaway not found")).useComponentsV2().setEphemeral(true).queue();
            return;
        }
        if (giveaway.hasEnded()) {
            event.replyComponents(Messages.getError("Giveaway has already ended")).useComponentsV2().setEphemeral(true).queue();
            return;
        }
        if(!giveaway.cancel()) {
            event.replyComponents(Messages.getError("Giveaway has already ended")).useComponentsV2().setEphemeral(true).queue();
            return;
        }

        TextChannel textChannel = shardManager.getTextChannelById(giveaway.getChannelId());
        if (textChannel == null) {
            event.replyComponents(Messages.getError("Giveaway textchannel not found")).useComponentsV2().setEphemeral(true).queue();
            return;
        }

        textChannel.retrieveMessageById(giveaway.getMessageId()).queue(message -> {
            message.editMessageComponents(giveawayService.getGiveawayUI().getGiveawayMessageCancel(giveaway))
                    .setAllowedMentions(Collections.emptyList())
                    .useComponentsV2()
                    .queue();

            message.replyComponents(giveawayService.getGiveawayUI().getGiveawayCancelSuccess(giveaway))
                    .useComponentsV2()
                    .queue();

            event.replyComponents(giveawayService.getGiveawayUI().getGiveawayCancelReply(giveaway)).useComponentsV2().setEphemeral(true).queue();

        }, throwable -> {
            event.replyComponents(Messages.getError("Giveaway message not found")).useComponentsV2().setEphemeral(true).queue();
        });
    }

    @Override
    public String getGuildId() {
        long id = bot.getBootstrap().getConfig().getBotConf().getGuildId();
        return String.valueOf(id);
    }

    @Override
    public Permission getPermission() {
        return Permission.ADMINISTRATOR;
    }
}
