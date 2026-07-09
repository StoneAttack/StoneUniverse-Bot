package de.jozelot.stoneuniverse.mechanics.giveaway;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.data.config.ConfigManager;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GiveawayService {

    private static final Logger logger = LoggerFactory.getLogger(GiveawayService.class);
    private final ScheduledExecutorService scheduler;

    private final StoneUniverse bot;
    private final GiveawayUI giveawayUI;

    private final List<Giveaway> giveaways = new ArrayList<>();

    public GiveawayService(StoneUniverse bot, ScheduledExecutorService scheduler) {
        this.bot = bot;
        this.scheduler = scheduler;
        this.giveawayUI = new GiveawayUI(bot);
    }

    public Giveaway createGiveaway(long creatorId, String titel, String description, int entryLimit, int winnerCount, long drawDate, long channelId) {
        String uniqueId = UUID.randomUUID().toString().split("-")[0];

        Giveaway giveaway = new Giveaway(uniqueId, creatorId, titel, description, entryLimit, winnerCount, drawDate, channelId);
        giveaways.add(giveaway);
        return giveaway;
    }

    public boolean initialize() {
        // 0. Bereinigung: Lösche Giveaways, die älter als 30 Tage und beendet sind
        String sqlCleanupWinners = "DELETE FROM giveaway_winners WHERE giveaway_id IN (SELECT id FROM giveaways WHERE ended = true AND draw_date < ?)";
        String sqlCleanupEntries = "DELETE FROM giveaway_entries WHERE giveaway_id IN (SELECT id FROM giveaways WHERE ended = true AND draw_date < ?)";
        String sqlCleanupGiveaways = "DELETE FROM giveaways WHERE ended = true AND draw_date < ?";

        long threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);

        try (Connection conn = bot.getBootstrap().getDatabaseLoader().getConnection()) {
            try (PreparedStatement stmtW = conn.prepareStatement(sqlCleanupWinners);
                 PreparedStatement stmtE = conn.prepareStatement(sqlCleanupEntries);
                 PreparedStatement stmtG = conn.prepareStatement(sqlCleanupGiveaways)) {

                stmtW.setLong(1, threshold);
                stmtE.setLong(1, threshold);
                stmtG.setLong(1, threshold);

                stmtW.executeUpdate();
                stmtE.executeUpdate();
                int deletedGiveaways = stmtG.executeUpdate();

                if (deletedGiveaways > 0) {
                    logger.info("Cleanup: Removed " + deletedGiveaways + " old expired giveaways from database.");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to run giveaway database cleanup!", e);
        }

        // 1. GIVEAWAYS AUS DATENBANK LADEN
        String sqlGiveaways = "SELECT id, creator_id, titel, description, entry_limit, winner_count, draw_date, channel_id, message_id, ended FROM giveaways";
        String sqlEntries = "SELECT giveaway_id, user_id FROM giveaway_entries";
        String sqlWinners = "SELECT giveaway_id, user_id FROM giveaway_winners";

        try (Connection conn = bot.getBootstrap().getDatabaseLoader().getConnection();
             PreparedStatement stmtG = conn.prepareStatement(sqlGiveaways);
             PreparedStatement stmtE = conn.prepareStatement(sqlEntries);
             PreparedStatement stmtW = conn.prepareStatement(sqlWinners)) {

            try (ResultSet rs = stmtG.executeQuery()) {
                while (rs.next()) {
                    Giveaway giveaway = new Giveaway(
                            rs.getString("id"),
                            rs.getLong("creator_id"),
                            rs.getString("titel"),
                            rs.getString("description"),
                            rs.getInt("entry_limit"),
                            rs.getInt("winner_count"),
                            rs.getLong("draw_date"),
                            rs.getLong("channel_id")
                    );
                    giveaway.setMessageId(rs.getLong("message_id"));
                    giveaway.setEnded(rs.getBoolean("ended"));

                    this.giveaways.add(giveaway);
                }
            }

            try (ResultSet rs = stmtE.executeQuery()) {
                while (rs.next()) {
                    Giveaway giveaway = getGiveawayById(rs.getString("giveaway_id"));
                    if (giveaway != null) {
                        giveaway.addEntry(rs.getLong("user_id"));
                    }
                }
            }

            try (ResultSet rs = stmtW.executeQuery()) {
                while (rs.next()) {
                    Giveaway giveaway = getGiveawayById(rs.getString("giveaway_id"));
                    if (giveaway != null) {
                        giveaway.loadWinner(rs.getLong("user_id"));
                    }
                }
            }

            for (Giveaway giveaway : giveaways) {
                if (!giveaway.hasEnded()) {
                    long restzeit = giveaway.getDrawDate() - System.currentTimeMillis();
                    if (restzeit > 0) {
                        scheduleGiveawayEnd(giveaway);
                    }
                }
            }

            logger.info("Successfully loaded " + giveaways.size() + " giveaways from database.");
            return true;
        } catch (Exception e) {
            logger.error("Failed to initialize giveaways from database!", e);
            return false;
        }
    }

    public void save() {
        if (giveaways.isEmpty()) {
            logger.info("No giveaway found to save");
            return;
        }
        String sqlGiveaway = "INSERT INTO giveaways (id, creator_id, titel, description, entry_limit, winner_count, draw_date, channel_id, message_id, ended) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET message_id = EXCLUDED.message_id, ended = EXCLUDED.ended";
        String sqlEntries = "INSERT INTO giveaway_entries (giveaway_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        String sqlWinners = "INSERT INTO giveaway_winners (giveaway_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = bot.getBootstrap().getDatabaseLoader().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtG = conn.prepareStatement(sqlGiveaway);
                 PreparedStatement stmtE = conn.prepareStatement(sqlEntries);
                 PreparedStatement stmtW = conn.prepareStatement(sqlWinners)) {
                for (Giveaway g : giveaways) {
                    stmtG.setString(1, g.getId());
                    stmtG.setLong(2, g.getCreatorId());
                    stmtG.setString(3, g.getTitel());
                    stmtG.setString(4, g.getDescription());
                    stmtG.setInt(5, g.getEntryLimit());
                    stmtG.setInt(6, g.getWinnerCount());
                    stmtG.setLong(7, g.getDrawDate());
                    stmtG.setLong(8, g.getChannelId());
                    stmtG.setLong(9, g.getMessageId());
                    stmtG.setBoolean(10, g.hasEnded());
                    stmtG.addBatch();
                    for (long userId : g.getEntries()) {
                        stmtE.setString(1, g.getId());
                        stmtE.setLong(2, userId);
                        stmtE.addBatch();
                    }
                    for (long winnerId : g.getWinner()) {
                        stmtW.setString(1, g.getId());
                        stmtW.setLong(2, winnerId);
                        stmtW.addBatch();
                    }
                }
                stmtG.executeBatch();
                stmtE.executeBatch();
                stmtW.executeBatch();
                conn.commit();
                logger.info("Successfully saved all giveaways to database!");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Failed to save giveaways to database!", e);
        }
    }

    public void saveSingleAsync(Giveaway g) {
        String sqlGiveaway = "INSERT INTO giveaways (id, creator_id, titel, description, entry_limit, winner_count, draw_date, channel_id, message_id, ended) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET message_id = EXCLUDED.message_id, ended = EXCLUDED.ended";
        try (Connection conn = bot.getBootstrap().getDatabaseLoader().getConnection();
             PreparedStatement stmtG = conn.prepareStatement(sqlGiveaway)) {
            stmtG.setString(1, g.getId());
            stmtG.setLong(2, g.getCreatorId());
            stmtG.setString(3, g.getTitel());
            stmtG.setString(4, g.getDescription());
            stmtG.setInt(5, g.getEntryLimit());
            stmtG.setInt(6, g.getWinnerCount());
            stmtG.setLong(7, g.getDrawDate());
            stmtG.setLong(8, g.getChannelId());
            stmtG.setLong(9, g.getMessageId());
            stmtG.setBoolean(10, g.hasEnded());
            stmtG.executeUpdate();
            logger.info("Successfully saved single giveaway ({}) to database!", g.getId());
        } catch (SQLException e) {
            logger.error("Failed to save single giveaway to database!", e);
        }
    }

    public Giveaway getGiveawayById(String id) {
        return giveaways.stream()
                .filter(g -> g.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public GiveawayUI getGiveawayUI() {
        return giveawayUI;
    }
    public List<Giveaway> getGiveaways() {
        return Collections.unmodifiableList(giveaways);
    }

    public void scheduleGiveawayEnd(Giveaway giveaway) {
        long restzeit = giveaway.getDrawDate() - System.currentTimeMillis();

        if (restzeit <= 0) {
            if (!giveaway.hasEnded()) {
                executeGiveawayEnd(giveaway);
            }
            return;
        }

        scheduler.schedule(() -> {
            executeGiveawayEnd(giveaway);
        }, restzeit, TimeUnit.MILLISECONDS);
    }

    private void executeGiveawayEnd(Giveaway giveaway) {
        if (!giveaway.roll()) {
            logger.warn("Giveaway {} couldnt be rolled: maybe no participants?.", giveaway.getId());
            return;
        }

        saveSingleAsync(giveaway);

        var shardManager = bot.getBootstrap().getBotManager().getShardManager();
        TextChannel textChannel = shardManager.getTextChannelById(giveaway.getChannelId());

        if (textChannel == null) {
            logger.error("Giveaway {} couldnt be rolled: Channel {} wasnt found in the cache!",
                    giveaway.getId(), giveaway.getChannelId());
            return;
        }

        textChannel.retrieveMessageById(giveaway.getMessageId()).queue(message -> {
            message.editMessageComponents(getGiveawayUI().getGiveawayMessageRolled(giveaway))
                    .setAllowedMentions(Collections.emptyList())
                    .useComponentsV2()
                    .queue();

            message.replyComponents(getGiveawayUI().getGiveawayRollSuccess(giveaway))
                    .useComponentsV2()
                    .queue();

            logger.info("Giveaway {} rolled successfully", giveaway.getId());
        }, throwable -> {
            logger.error("Giveaway Message {} in Channel {} was not found.",
                    giveaway.getMessageId(), giveaway.getChannelId(), throwable);
        });
    }

    public void checkExpiredGiveaways() {
        logger.info("Checking for expired giveaways during downtime...");
        for (Giveaway giveaway : giveaways) {
            if (!giveaway.hasEnded()) {
                long restzeit = giveaway.getDrawDate() - System.currentTimeMillis();
                if (restzeit <= 0) {
                    logger.info("Giveaway {} should've been rolled in the downtime. Starting automatic roll...", giveaway.getId());
                    executeGiveawayEnd(giveaway);
                }
            }
        }
    }
}
