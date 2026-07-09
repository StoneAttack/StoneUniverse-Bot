package de.jozelot.stoneuniverse.listener;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.mechanics.CountingSystem;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountingListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CountingListener.class);
    private final StoneUniverse bot;
    private final CountingSystem countingSystem;

    public CountingListener(StoneUniverse bot) {
        this.bot = bot;
        this.countingSystem = bot.getBootstrap().getCountingSystem();
    }

    /**
     * Counting Logik
     * @param event
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot()) return;

        var channel = event.getChannel();
        long countingChannelId = bot.getBootstrap().getConfig().getMinigames().getCounting().getChannelId();

        if (channel.getIdLong() != countingChannelId) return;

        String rawMessage = event.getMessage().getContentRaw();

        int count;
        try {
            count = Integer.parseInt(rawMessage);
        } catch (NumberFormatException e) {
            return;
        }
        long userId = event.getMember().getIdLong();

        // CHECK COUNT
        if (countingSystem.checkCountDouble(userId, count)) {
            // MEMBER HAT 2 MAL GECOUNTET
            event.getMessage().replyComponents(Messages.getCountingFailed(event.getAuthor(), countingSystem.getCurrentCount(), true, countingSystem.wasRoundAHighscore())).useComponentsV2().queue();
            countingSystem.fail();
            event.getMessage().addReaction(Emoji.fromUnicode("❌")).queue();
            return;
        } else if (!countingSystem.checkCount(userId, count)) {
            // MEMBER HAT FALSCHE ZAHL
            event.getMessage().replyComponents(Messages.getCountingFailed(event.getAuthor(), countingSystem.getCurrentCount(), false, countingSystem.wasRoundAHighscore())).useComponentsV2().queue();
            countingSystem.fail();
            event.getMessage().addReaction(Emoji.fromUnicode("❌")).queue();
            return;
        }
        if (countingSystem.checkHighscore(count)) {
            // AKTUELLER REKORD
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83C\uDFC6")).queue();
        } else if (countingSystem.checkHighscoreCurrent(count)) {
            // REKORD RUN
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83D\uDD25")).queue();
        }else {
            // RICHTIG
            event.getMessage().addReaction(Emoji.fromUnicode("☑")).queue();
        }
        countingSystem.count(userId);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getCustomId();

        if (buttonId.equalsIgnoreCase("counting:stats")) {
            event.replyComponents(Messages.getCountingStats(countingSystem)).useComponentsV2().setEphemeral(true).queue();
            return;
        }
    }
}
