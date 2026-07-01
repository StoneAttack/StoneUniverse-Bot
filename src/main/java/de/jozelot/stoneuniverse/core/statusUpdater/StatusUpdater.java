package de.jozelot.stoneuniverse.core.statusUpdater;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.data.config.ActivityState;
import de.jozelot.stoneuniverse.data.config.ConfigManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusUpdater {

    private static final Logger logger = LoggerFactory.getLogger(StatusUpdater.class);
    private final StoneUniverse bot;
    private MinecraftServerPinger msp;
    private ConfigManager config;

    public StatusUpdater(StoneUniverse bot) {
        this.bot = bot;
    }

    public void load() {
        config = bot.getBootstrap().getConfig();
        msp = new MinecraftServerPinger(
                config.getBotConf().getStatus().getServer(),
                config.getBotConf().getStatus().getPort(),
                5000);
    }

    public void update() {
        msp.fetchStatus();
        ConfigManager.BotConf.ActivityConfig activity;

        if (msp.isOnline()) {
            if (msp.isMaintenance()) {
                activity = config.getBotConf().getStatus().getActivity(ActivityState.MAINTENANCE);
            } else {
                activity = config.getBotConf().getStatus().getActivity(ActivityState.ONLINE);
            }
        } else {
            activity = config.getBotConf().getStatus().getActivity(ActivityState.OFFLINE);
        }

        String messageRaw = activity.getFormat();

        String finalMessage = messageRaw
                .replace("{player_count}", String.valueOf(msp.getOnlinePlayers()))
                .replace("{max_players}", String.valueOf(msp.getMaxPlayers()));

        var shardManager = bot.getBootstrap().getBotManager().getShardManager();
        shardManager.setActivity(Activity.of(Activity.ActivityType.valueOf(activity.getType().toUpperCase()), finalMessage));
        shardManager.setStatus(OnlineStatus.fromKey(activity.getStatus()));

        logger.debug("Status updated: " + finalMessage);
    }
}
