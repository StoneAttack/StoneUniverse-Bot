package de.jozelot.stoneuniverse.mechanics;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.data.database.DatabaseLoader;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class CountingSystem {

    private static final Logger logger = LoggerFactory.getLogger(CountingSystem.class);
    private final StoneUniverse bot;

    private int currentCount = 0;
    private int roundsPlayed = 0;
    private long lastCounterId = 0;

    private int currentHighscore = 0;

    public CountingSystem(StoneUniverse bot) {
        this.bot = bot;
    }

    public void sendRestartMessage() {
        var shardManager = bot.getBootstrap().getBotManager().getShardManager();
        long channelId = bot.getBootstrap().getConfig().getMinigames().getCounting().getChannelId();

        TextChannel channel = shardManager.getTextChannelById(channelId);
        if (channel == null) {
            logger.error("Could not send restart message: TextChannel with ID {} not found in cache!", channelId);
            return;
        }

        channel.getHistory().retrievePast(1).queue(messages -> {
            if (!messages.isEmpty()) {
                Message lastMessage = messages.get(0);

                long botId = shardManager.getShardCache().stream()
                        .findFirst()
                        .map(jda -> jda.getSelfUser().getIdLong())
                        .orElse(0L);

                if (lastMessage.getAuthor().getIdLong() == botId) {
                    logger.info("Restart message skipped: Last message in counting channel was already sent by the bot.");
                    return;
                }
            }

            if (lastCounterId == 0) {
                channel.sendMessageComponents(Messages.getCountingInfo(currentCount + 1, null))
                        .useComponentsV2()
                        .queue();
                return;
            }

            shardManager.retrieveUserById(lastCounterId).queue(user -> {
                channel.sendMessageComponents(Messages.getCountingInfo(currentCount + 1, user))
                        .useComponentsV2()
                        .queue();
            }, throwable -> {
                logger.warn("Could not retrieve user with ID {} from Discord API. Sending without user mention.", lastCounterId);
                channel.sendMessageComponents(Messages.getCountingInfo(currentCount + 1, null))
                        .useComponentsV2()
                        .queue();
            });

        }, throwable -> {
            logger.error("Failed to check channel history for counting channel!", throwable);
        });
    }

    public boolean checkHighscoreCurrent(int count) {
        return count > currentHighscore;
    }

    public boolean checkHighscore(int count) {
        return count == currentHighscore;
    }

    public boolean checkCountDouble(long userId, int count) {
        return lastCounterId == userId;
    }

    public boolean checkCount(long userId, int count) {
        return currentCount + 1 == count;
    }

    public boolean wasRoundAHighscore() {
        return currentCount >= currentHighscore && currentCount > 0;
    }

    public void count(long userId) {
        currentCount++;
        lastCounterId = userId;
        if (currentHighscore < currentCount) currentHighscore = currentCount;
        saveToDatabase();
    }

    public void fail() {
        var shardmanager = bot.getBootstrap().getBotManager().getShardManager();

        currentCount = 0;
        roundsPlayed++;
        lastCounterId = 0;
        saveToDatabase();
    }

    public boolean initialize() {
        logger.info("Loading counting system data from database...");
        try (Connection conn = bot.getBootstrap().getDatabaseLoader().getConnection()) {
            String selectSql = "SELECT current_count, last_counter_id, rounds_played, highscore FROM counting WHERE id = 1;";
            try (PreparedStatement stmt = conn.prepareStatement(selectSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    this.currentCount = rs.getInt("current_count");
                    this.roundsPlayed = rs.getInt("rounds_played");
                    this.currentHighscore = rs.getInt("highscore");
                    this.lastCounterId = Long.parseLong(rs.getString("last_counter_id"));
                    logger.info("Counting system loaded! Count: {}, Highscore: {}", currentCount, currentHighscore);
                    return true;
                } else {
                    logger.warn("No existing counting data found. System starts at 0.");
                    return true;
                }
            }
        } catch (SQLException | NumberFormatException e) {
            logger.error("Failed to initialize counting system!", e);
            return false;
        }
    }

    public void saveToDatabase() {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = bot.getBootstrap().getDatabaseLoader().getConnection()) {
                String sql = "INSERT INTO counting (id, current_count, last_counter_id, rounds_played, highscore) " +
                        "VALUES (1, ?, ?, ?, ?) " +
                        "ON CONFLICT (id) DO UPDATE SET " +
                        "current_count = EXCLUDED.current_count, " +
                        "last_counter_id = EXCLUDED.last_counter_id, " +
                        "rounds_played = EXCLUDED.rounds_played, " +
                        "highscore = EXCLUDED.highscore;";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, currentCount);
                    stmt.setString(2, String.valueOf(lastCounterId));
                    stmt.setInt(3, roundsPlayed);
                    stmt.setInt(4, currentHighscore);
                    stmt.executeUpdate();
                }
            } catch (Exception e) {
                logger.error("Failed to save counting data to database", e);
            }
        });
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public long getLastCounterId() {
        return lastCounterId;
    }

    public int getCurrentHighscore() {
        return currentHighscore;
    }
}
