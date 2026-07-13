package de.jozelot.stoneuniverse.core;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.core.heartbeat.HeartBeat;
import de.jozelot.stoneuniverse.core.statusUpdater.StatusUpdater;
import de.jozelot.stoneuniverse.data.config.ConfigLoader;
import de.jozelot.stoneuniverse.data.config.ConfigManager;
import de.jozelot.stoneuniverse.data.database.DatabaseLoader;
import de.jozelot.stoneuniverse.data.hosts.HostsLoader;
import de.jozelot.stoneuniverse.data.hosts.HostsManager;
import de.jozelot.stoneuniverse.interfaces.Bootstrap;
import de.jozelot.stoneuniverse.listener.tempChannel.TempChannelSettingsListener;
import de.jozelot.stoneuniverse.mechanics.CountingSystem;
import de.jozelot.stoneuniverse.mechanics.giveaway.GiveawayService;
import de.jozelot.stoneuniverse.mechanics.levelSystem.LevelSystem;
import de.jozelot.stoneuniverse.mechanics.tempChannels.TempChannelSystem;
import de.jozelot.stoneuniverse.messages.MessageManager;
import de.jozelot.stoneuniverse.registry.CommandRegistry;
import de.jozelot.stoneuniverse.registry.ListenerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BotBootstrap implements Bootstrap {

    private static final Logger logger = LoggerFactory.getLogger(BotBootstrap.class);
    private ScheduledExecutorService scheduler;

    private final StoneUniverse bot;
    private BotManager botManager;
    private ConfigLoader configLoader;
    private ConfigManager config;
    private HostsLoader hostsLoader;
    private HostsManager hostsManager;
    private DatabaseLoader databaseLoader;
    private HeartBeat heartBeat;
    private StatusUpdater statusUpdater;

    private CountingSystem countingSystem;
    private TempChannelSystem tempChannelSystem;
    private LevelSystem levelSystem;
    private GiveawayService giveawayService;

    private ListenerRegistry listenerRegistry;
    private CommandRegistry commandRegistry;

    private MessageManager messageManager;
    private BotSystem botSystem;

    public BotBootstrap(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public boolean register() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        botManager = new BotManager(bot);
        config = new ConfigManager(bot);
        configLoader = new ConfigLoader(bot);
        hostsLoader = new HostsLoader(bot);
        hostsManager = new HostsManager(bot);
        databaseLoader = new DatabaseLoader(bot);
        listenerRegistry = new ListenerRegistry(bot);
        commandRegistry = new CommandRegistry(bot);
        heartBeat = new HeartBeat(bot);
        statusUpdater = new StatusUpdater(bot);
        countingSystem = new CountingSystem(bot);
        tempChannelSystem = new TempChannelSystem(bot);
        levelSystem = new LevelSystem(bot);
        giveawayService = new GiveawayService(bot, scheduler);
        messageManager = new MessageManager(bot);
        botSystem = new BotSystem(bot);
        logger.info("Object registration finished!");
        return true;
    }

    @Override
    public boolean enable() {
        configLoader.load();
        hostsLoader.load();
        if (!botManager.start()) return false;
        if (!listenerRegistry.register()) return false;
        if (!databaseLoader.connect()) return false;
        if (!databaseLoader.createTables()) return false;
        statusUpdater.load();
        heartBeat.start();
        if (!countingSystem.initialize()) return false;
        if (!levelSystem.initialize()) return false;
        if (!giveawayService.initialize()) return false;
        if (!messageManager.initialize()) return false;
        logger.info("Object activation finished!");
        return true;
    }

    @Override
    public void shutdown() {
        if (botManager != null) botManager.shutdown();
        if (configLoader != null) configLoader.unload();
        heartBeat.shutdown();
        levelSystem.shutdown();
        giveawayService.save();
        if (scheduler != null) scheduler.shutdown();
        databaseLoader.close();
        logger.info("Application shutdown finished!");
    }

    @Override
    public void reload() {
        botManager.shutdown();
        heartBeat.stop();
        levelSystem.shutdown();
        giveawayService.save();
        databaseLoader.close();

        configLoader.reload();
        databaseLoader.connect();
        if (!botManager.start()) {
            logger.error("Bot couldn't be startet during reload!");
            System.exit(1);
            return;
        }

        if (!listenerRegistry.register()) {
            logger.error("Listener registry failed!");
            return;
        }

        heartBeat.start();

        levelSystem.initialize();
        giveawayService.initialize();
        messageManager.initialize();

        logger.info("Reload finished successfully!");
    }

    public BotManager getBotManager() {
        return botManager;
    }
    public ConfigManager getConfig() {
        return config;
    }
    public HostsManager getHosts() {
        return hostsManager;
    }
    public StatusUpdater getStatusUpdater() {
        return statusUpdater;
    }
    public CountingSystem getCountingSystem() {
        return countingSystem;
    }
    public DatabaseLoader getDatabaseLoader() {
        return databaseLoader;
    }
    public TempChannelSystem getTempChannelSystem() {
        return tempChannelSystem;
    }
    public LevelSystem getLevelSystem() {
        return levelSystem;
    }
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }
    public GiveawayService getGiveawayService() {
        return giveawayService;
    }
    public MessageManager getMessageManager() {
        return messageManager;
    }
    public BotSystem getBotSystem() {
        return botSystem;
    }
}
