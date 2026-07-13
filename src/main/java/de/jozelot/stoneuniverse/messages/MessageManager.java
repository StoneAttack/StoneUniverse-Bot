package de.jozelot.stoneuniverse.messages;

import de.jozelot.stoneuniverse.StoneUniverse;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;

public class MessageManager {

    private final StoneUniverse bot;
    private final Regelwerk regelwerk;

    public MessageManager(StoneUniverse bot) {
        this.bot = bot;
        this.regelwerk = new Regelwerk(bot);
    }

    public boolean initialize() {
        return regelwerk.initialize();
    }

    public Container getSendSuccess(String preset, TextChannel textChannel) {
        return Container.of(
                TextDisplay.of("# ✅ Preset gesendet"),
                TextDisplay.of("Du hast das Preset `" + preset + "` erfolgreich in " + textChannel.getAsMention() + " gesendet!")
        );
    }

    public Regelwerk getRegelwerk() {
        return regelwerk;
    }
}
