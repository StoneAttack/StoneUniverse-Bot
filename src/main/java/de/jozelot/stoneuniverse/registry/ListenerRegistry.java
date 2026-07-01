package de.jozelot.stoneuniverse.registry;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.data.config.ConfigManager;
import de.jozelot.stoneuniverse.interfaces.Registry;
import de.jozelot.stoneuniverse.listener.CountingListener;
import de.jozelot.stoneuniverse.listener.MessageListener;
import de.jozelot.stoneuniverse.listener.ReadyListener;
import de.jozelot.stoneuniverse.listener.TempChannelListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListenerRegistry implements Registry {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private final StoneUniverse bot;

    public ListenerRegistry(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public boolean register() {
        var shardManager = bot.getBootstrap().getBotManager().getShardManager();

        shardManager.addEventListener(new MessageListener(bot));
        shardManager.addEventListener(new CountingListener(bot));
        shardManager.addEventListener(new ReadyListener(bot));
        shardManager.addEventListener(new TempChannelListener(bot));
        logger.info("All Event Listener registered!");
        return true;
    }
}
