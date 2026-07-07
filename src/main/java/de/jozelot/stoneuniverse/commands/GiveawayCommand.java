package de.jozelot.stoneuniverse.commands;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.interfaces.Command;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.modals.Modal;

public class GiveawayCommand implements Command {

    private final StoneUniverse bot;

    public GiveawayCommand(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("giveaway", "\uD83C\uDF89 | Admin: Verwalte und erstelle Giveaways").addSubcommands(
                new SubcommandData("setup", "\uD83C\uDF89 | Admin: Erstelle ein Giveaway"),
                new SubcommandData("end", "\uD83C\uDF89 | Admin: Beende ein Giveaway vorzeitig und lose es aus"),
                new SubcommandData("cancel", "\uD83C\uDF89 | Admin: Breche ein Giveaway ab"),
                new SubcommandData("list", "\uD83C\uDF89 | Admin: Liste alle aktuellen Giveaways auf"),
                new SubcommandData("reroll", "\uD83C\uDF89 | Admin: Lose die Gewinner eines Giveaways neu aus")
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
        } else {
            event.replyComponents(Messages.getError("Unknown Subcommand: " + subcommandName)).useComponentsV2().setEphemeral(true).queue();
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

    /**
     * End Subcommand
     * @param event
     */
    public void end(SlashCommandInteractionEvent event) {

    }

    /**
     * Cancel Subcommand
     * @param event
     */
    public void cancel(SlashCommandInteractionEvent event) {

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
