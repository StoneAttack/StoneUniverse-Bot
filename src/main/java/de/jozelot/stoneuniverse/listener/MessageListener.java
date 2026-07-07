package de.jozelot.stoneuniverse.listener;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

public class MessageListener extends ListenerAdapter {

    private final StoneUniverse bot;

    public MessageListener(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Member sender = event.getMember();
        Message message = event.getMessage();
        String messageString = message.getContentStripped();

        Collection<String> keywordsIp = List.of("addresse", "ip", "ipaddresse", "adrese", "addrese", "adresse", "ip-addresse", "address", "ipaddress", "ip-address");

        if (keywordsIp.stream().anyMatch(keyword -> messageString.toLowerCase().contains(keyword))) {
            if (event.getChannel().getIdLong() == bot.getBootstrap().getConfig().getMinigames().getCounting().getChannelId()) return;

            event.getMessage().replyComponents(Messages.getConnectionInfo(bot.getBootstrap().getHosts())).useComponentsV2().queue();

        } else if (messageString.equalsIgnoreCase("java")) {
            event.getMessage().reply("Java? Da bin ich ganz in meinem Element! ☕");

        } else if (messageString.toLowerCase().contains("media") && messageString.toLowerCase().contains("anforderung")) {
            event.getMessage().replyComponents(Messages.getMediaAnf(bot.getBootstrap().getConfig())).useComponentsV2().queue();
        }
    }
}
