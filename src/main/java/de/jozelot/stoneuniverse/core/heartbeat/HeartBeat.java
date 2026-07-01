package de.jozelot.stoneuniverse.core.heartbeat;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.core.BotBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class HeartBeat {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeat.class);
    private final StoneUniverse bot;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> runningTask;

    private long secondsPassed = 0;

    public HeartBeat(StoneUniverse bot) {
        this.bot = bot;
    }

    public void start() {
        if (runningTask != null && !runningTask.isDone()) {
            return;
        }
        logger.info("Starting scheduler...");
        secondsPassed = 0;
        int intervalStatus = bot.getBootstrap().getConfig().getBotConf().getStatus().getUpdateInterval();
        triggerStatusUpdateAsync();

        runningTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                secondsPassed++;

                if (secondsPassed % intervalStatus == 0) {
                    triggerStatusUpdateAsync();
                }

            } catch (Exception e) {
                logger.error("Scheduler task run into an error", e);
            }
        }, 0, 1, TimeUnit.SECONDS);
        logger.info("Scheduler started");
    }

    private void triggerStatusUpdateAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                bot.getBootstrap().getStatusUpdater().update();
            } catch (Exception e) {
                logger.error("Error during async status update", e);
            }
        });
    }

    public void stop() {
        if (runningTask != null) {
            runningTask.cancel(false);

            runningTask = null;
            logger.info("Scheduler stopped");
        }
    }

    public void shutdown() {
        stop();
        scheduler.shutdown();
        logger.info("Scheduler shutdown");
    }
}
