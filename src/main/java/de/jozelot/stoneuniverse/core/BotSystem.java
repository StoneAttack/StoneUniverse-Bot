package de.jozelot.stoneuniverse.core;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.core.heartbeat.HeartBeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotSystem {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(BotSystem.class);

    public BotSystem(StoneUniverse bot) {
        this.bot = bot;
    }

    public void cleanUpMemory() {
        Runtime runtime = Runtime.getRuntime();
        long before = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        System.gc();
        long after = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;

        logger.info("Garbage Collector triggered. RAM altered from {}MB to {}MB.", before, after);
    }
}
