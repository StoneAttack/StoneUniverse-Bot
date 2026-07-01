package de.jozelot.stoneuniverse.core;

import de.jozelot.stoneuniverse.StoneUniverse;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotManager {

    private static final Logger logger = LoggerFactory.getLogger(BotManager.class);

    private final StoneUniverse bot;
    private ShardManager shardManager;

    public BotManager(StoneUniverse bot) {
        this.bot = bot;
    }

    public boolean start() {
        try {
            String token = bot.getBootstrap().getConfig().getBotConf().getToken();

            shardManager = DefaultShardManagerBuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .setStatus(OnlineStatus.ONLINE)
                    .addEventListeners()
                    .build();
            logger.info("Bot created and bot is online!");
            return true;
        } catch (Exception e) {
            logger.error("Bot start failed" , e);
            return false;
        }
    }

    public void shutdown() {
        logger.info("Bot shutting down...");
        if (shardManager != null) shardManager.shutdown();
    }

    public ShardManager getShardManager() {
        return shardManager;
    }
}
