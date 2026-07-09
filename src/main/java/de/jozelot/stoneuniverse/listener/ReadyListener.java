package de.jozelot.stoneuniverse.listener;

import de.jozelot.stoneuniverse.StoneUniverse;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ReadyListener extends ListenerAdapter {

    private final StoneUniverse bot;

    public ReadyListener(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
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
