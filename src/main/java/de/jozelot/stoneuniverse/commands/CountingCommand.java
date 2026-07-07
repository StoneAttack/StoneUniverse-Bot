package de.jozelot.stoneuniverse.commands;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.core.BotBootstrap;
import de.jozelot.stoneuniverse.interfaces.Command;
import de.jozelot.stoneuniverse.mechanics.levelSystem.UserLevel;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class CountingCommand implements Command {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(RankCommand.class);

    public CountingCommand(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("counting", "\uD83D\uDD01 | Command für das Counting Minispiel.").addSubcommands(
                new SubcommandData("stats", "\uD83D\uDCCA | Schaue dir die Statistiken des Counting Minispiels an.")
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String subcommandName = event.getSubcommandName();
        if (subcommandName.equalsIgnoreCase("stats")) {
            event.replyComponents(Messages.getCountingStats(bot.getBootstrap().getCountingSystem())).useComponentsV2().setEphemeral(true).queue();
        }
    }

    @Override
    public String getGuildId() {
        long id = bot.getBootstrap().getConfig().getBotConf().getGuildId();
        return String.valueOf(id);
    }
}
