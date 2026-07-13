package de.jozelot.stoneuniverse.commands;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.interfaces.Command;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaCommand implements Command {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(MediaCommand.class);

    public MediaCommand(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("media", "\uD83D\uDCF9 | Zeige die Media-Anforderungen an.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        logger.info(event.getMember().getEffectiveName() + " issued server command: /" + event.getFullCommandName());
        if (event.getChannelIdLong() == bot.getBootstrap().getConfig().getMinigames().getCounting().getChannelId()) {
            event.replyComponents(Messages.getError("Can't use that command in this Channel.")).useComponentsV2().setEphemeral(true).queue();
            return;
        }

        event.replyComponents(Messages.getMediaAnf(bot.getBootstrap().getConfig())).useComponentsV2().queue();
    }

    @Override
    public String getGuildId() {
        long id = bot.getBootstrap().getConfig().getBotConf().getGuildId();
        return String.valueOf(id);
    }
}
