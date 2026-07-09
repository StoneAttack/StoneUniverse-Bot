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

import java.util.Collections;
import java.util.List;

public class RankCommand implements Command {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(RankCommand.class);

    public RankCommand(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("rank", "\uD83D\uDCCA | Schaue dir deinen Rank oder den eines anderen Mitglieds an.").addOption(OptionType.USER, "target", "Mitglied zum Nachschauen.", false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getChannelIdLong() == bot.getBootstrap().getConfig().getMinigames().getCounting().getChannelId()) {
            event.replyComponents(Messages.getError("Can't use that command in this Channel.")).useComponentsV2().setEphemeral(true).queue();
            return;
        }

        var levelMgr = bot.getBootstrap().getLevelSystem();

        var targetOption = event.getOption("target");

        Member target = (targetOption != null) ? targetOption.getAsMember() : event.getMember();

        if (target == null) {
            event.replyComponents(Messages.getError("Member not found")).useComponentsV2().setEphemeral(true).queue();
            return;
        }

        UserLevel userLevel = levelMgr.getUserLevel(target.getIdLong());

        if (userLevel == null) {
            event.replyComponents(Messages.getError("UserLevel == null")).useComponentsV2().setEphemeral(true).queue();
            logger.error("UserLevel Object is null for user ID: {}", target.getIdLong());
            return;
        }

        event.replyComponents(Messages.getRank(target, userLevel.getLevel(), userLevel.getXp(), userLevel.getXpNeeded(), levelMgr.getRank(target.getIdLong())))
                .setAllowedMentions(Collections.emptyList())
                .setEphemeral(true)
                .useComponentsV2().queue();
    }

    @Override
    public String getGuildId() {
        long id = bot.getBootstrap().getConfig().getBotConf().getGuildId();
        return String.valueOf(id);
    }
}
