package de.jozelot.stoneuniverse;

import de.jozelot.stoneuniverse.core.BotBootstrap;
import de.jozelot.stoneuniverse.interfaces.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoneUniverse implements Bot {

    private static final Logger logger = LoggerFactory.getLogger(StoneUniverse.class);
    private BotBootstrap bootstrap;

    /**
     * Bot Startup
     * Register all Dependencies and Listeners
     */
    @Override
    public boolean onEnable() {
        bootstrap = new BotBootstrap(this);

        boolean register = bootstrap.register();
        if (!register) {
            logger.error("Object register failed! Look above for further information.");
            return false;
        }

        boolean enable = bootstrap.enable();
        if (!enable) {
            logger.error("Object activation failed! Look above for further information.");
            return false;
        }
        return true;
    }

    /**
     * Bot Shutdown
     */
    @Override
    public void onDisable() {
        bootstrap.shutdown();
    }

    public BotBootstrap getBootstrap() {
        return bootstrap;
    }
}
