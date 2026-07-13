package de.jozelot.stoneuniverse.listener;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

public class MessageListener extends ListenerAdapter {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    public MessageListener(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Member sender = event.getMember();
        Message message = event.getMessage();
        String messageString = message.getContentStripped();

        String regex = ".*\\b(ip|adresse|adrese|addrese|address|ip-?adresse|ip-?adrese|ip-?address)\\b.*";

        if (messageString.matches(regex)) {
            if (event.getChannel().getIdLong() == bot.getBootstrap().getConfig().getMinigames().getCounting().getChannelId()) return;
            logger.info(event.getMember().getEffectiveName() + " has been send the ip message");
            event.getMessage().replyComponents(Messages.getConnectionInfo(bot.getBootstrap().getHosts())).useComponentsV2().queue();

        } else if (messageString.equalsIgnoreCase("java")) {
            logger.info(event.getMember().getEffectiveName() + " has been send the java message");
            event.getMessage().reply("Java? Da bin ich ganz in meinem Element! ☕");

        } else if (messageString.toLowerCase().contains("media") && messageString.toLowerCase().contains("anforderung")) {
            logger.info(event.getMember().getEffectiveName() + " has been send the media message");
            event.getMessage().replyComponents(Messages.getMediaAnf(bot.getBootstrap().getConfig())).useComponentsV2().queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getCustomId();

        if (buttonId.equalsIgnoreCase("regeln:discord")) {
            event.replyComponents(bot.getBootstrap().getMessageManager().getRegelwerk().getDiscordRegeln()).useComponentsV2().setEphemeral(true).queue();
        } else if (buttonId.equalsIgnoreCase("regeln:stoneattack")) {
            event.replyComponents(bot.getBootstrap().getMessageManager().getRegelwerk().getStoneRegeln()).useComponentsV2().setEphemeral(true).queue();
        }
    }
}
