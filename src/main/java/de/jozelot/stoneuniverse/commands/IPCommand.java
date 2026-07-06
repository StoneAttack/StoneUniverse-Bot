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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IPCommand implements Command {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(IPCommand.class);

    public IPCommand(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("ip", "\uD83D\uDCBB | Zeige die Server-IP an.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyComponents(Messages.getConnectionInfo(bot.getBootstrap().getHosts())).useComponentsV2().queue();
    }

    @Override
    public String getGuildId() {
        long id = bot.getBootstrap().getConfig().getBotConf().getGuildId();
        return String.valueOf(id);
    }
}
