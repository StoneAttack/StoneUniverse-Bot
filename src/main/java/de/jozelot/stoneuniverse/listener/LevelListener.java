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

import java.util.Collections;

public class LevelListener extends ListenerAdapter {

    private final StoneUniverse bot;

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
        if (!event.getButton().getCustomId().equalsIgnoreCase("level:leaderboard")) {
            return;
        }
        var levelMgr = bot.getBootstrap().getLevelSystem();
        Container container = Messages.getLeaderboard(levelMgr.getTopLevel(10));
        event.replyComponents(container).useComponentsV2()
                .mentionRepliedUser(false)
                .setAllowedMentions(Collections.emptyList()).setEphemeral(true).queue();
    }
}
