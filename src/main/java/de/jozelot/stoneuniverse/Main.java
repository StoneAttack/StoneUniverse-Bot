package de.jozelot.stoneuniverse;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Raus aus der static Umgebung
     * Einfach dafür, dass wir ein simples Bot Objekt haben.
     */
    void main() {
        logger.info("Application started");
        logger.info("Creating bot...");
        StoneUniverse bot = new StoneUniverse();

        boolean startSuccess = bot.onEnable();
        if (!startSuccess) {
            bot.onDisable();
            logger.warn("Bot crashed unexpected! Please review, why this happend!");
            System.exit(1);
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            logger.info("Shutdown-Hook triggered. Preparing for shutdown...");
            bot.onDisable();

            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.stop();
        }));
        logger.info("Startup finished! Done!");
    }
}
