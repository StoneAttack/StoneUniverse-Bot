package de.jozelot.stoneuniverse.commands;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.interfaces.Command;
import de.jozelot.stoneuniverse.mechanics.levelSystem.UserLevel;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class LeaderboardCommand implements Command {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardCommand.class);

    public LeaderboardCommand(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("leaderboard", "\uD83C\uDFC6 | Zeige dir das Level Leaderboard an.");
    }

    @Override
    public List<CommandData> getAliases() {
        return List.of(Commands.slash("lb", "\uD83C\uDFC6 | Zeige dir das Level Leaderboard an."));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        if (event.getChannelIdLong() == bot.getBootstrap().getConfig().getMinigames().getCounting().getChannelId()) {
            event.replyComponents(Messages.getError("Can't use that command in this Channel.")).useComponentsV2().setEphemeral(true).queue();
            return;
        }

        var levelMgr = bot.getBootstrap().getLevelSystem();

        event.deferReply().queue(hook -> {

            Messages.getLeaderboard(levelMgr.getTopLevel(10), event.getGuild()).thenAccept(container -> {

                hook.sendMessageComponents(container).useComponentsV2()
                        .setAllowedMentions(Collections.emptyList())
                        .queue();

            }).exceptionally(throwable -> {
                hook.sendMessageComponents(Messages.getError("Leaderboard couldn't be loaded")).useComponentsV2().queue();
                return null;
            });

        });
    }

    @Override
    public String getGuildId() {
        long id = bot.getBootstrap().getConfig().getBotConf().getGuildId();
        return String.valueOf(id);
    }
}
