package de.jozelot.stoneuniverse.listener;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.mechanics.levelSystem.UserLevel;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class LevelListener extends ListenerAdapter {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(LevelListener.class);

    public LevelListener(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isFromType(ChannelType.PRIVATE)) return;

        long userId = event.getAuthor().getIdLong();
        long currentTime = System.currentTimeMillis();

        UserLevel ul = bot.getBootstrap().getLevelSystem().getUserLevel(userId);

        if (currentTime - ul.getLastXpGain() >= bot.getBootstrap().getConfig().getSystem().getLevel().getXpCooldown() * 1000L) {
            var levelConfig = bot.getBootstrap().getConfig().getSystem().getLevel();
            boolean hasLeveledUp = ul.addMessageXp(levelConfig.getMinMessageXp(), levelConfig.getMaxMessageXp());

            ul.setLastXpGain(currentTime);

            bot.getBootstrap().getLevelSystem().saveUserLevel(ul);

            if (hasLeveledUp) {
                Member member = event.getMember();
                var levelManager = bot.getBootstrap().getLevelSystem();
                int level = levelManager.getUserLevel(member.getIdLong()).getLevel();
                int xp = levelManager.getUserLevel(member.getIdLong()).getXp();
                event.getMessage().replyComponents(Messages.getLevelUp(member, level, xp, levelManager.getUserLevel(member.getIdLong()).getXpNeeded())).useComponentsV2().queue();
            }
        }
    }

    /**
     * Leaderboard button
     * @param event
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getCustomId();

        if (event.isAcknowledged()) return;

        if (buttonId.equalsIgnoreCase("level:leaderboard")) {
            if (event.getGuild() == null) return;

            var levelMgr = bot.getBootstrap().getLevelSystem();
            logger.info("{} clicked 'level:leaderboard' button", event.getMember().getEffectiveName());

            event.deferReply().setEphemeral(true).queue(hook -> {
                Messages.getLeaderboard(levelMgr.getTopLevel(10), event.getGuild()).thenAccept(container -> {
                    if (event.isAcknowledged()) {
                        hook.sendMessageComponents(container).useComponentsV2()
                                .setAllowedMentions(Collections.emptyList())
                                .queue();
                    }
                }).exceptionally(throwable -> {
                    hook.sendMessageComponents(Messages.getError("Leaderboard couldn't be loaded")).useComponentsV2().queue();
                    return null;
                });
            });

        } else if (buttonId.equalsIgnoreCase("level:info")) {
            logger.info("{} clicked 'level:info' button", event.getMember().getEffectiveName());

            event.replyComponents(Messages.getLevelInfo(bot.getBootstrap().getConfig())).useComponentsV2().setEphemeral(true).queue();
        }
    }
}
