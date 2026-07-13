package de.jozelot.stoneuniverse.listener;

import de.jozelot.stoneuniverse.StoneUniverse;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadyListener extends ListenerAdapter {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(ReadyListener.class);

    public ReadyListener(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("Bot ready!");
        /*long guildId = bot.getBootstrap().getConfig().getBotConf().getGuildId();
        var guild = event.getJDA().getGuildById(guildId);

        if (guild != null) {
            guild.updateCommands().queue(success -> {
            });
        }

        event.getJDA().updateCommands().queue(success -> {
        });
        if (true) return;*/
        bot.getBootstrap().getCountingSystem().sendRestartMessage();
        bot.getBootstrap().getTempChannelSystem().initialize();
        bot.getBootstrap().getGiveawayService().checkExpiredGiveaways();
    }
}
