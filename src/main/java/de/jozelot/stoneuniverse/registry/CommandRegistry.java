package de.jozelot.stoneuniverse.registry;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.commands.CountingCommand;
import de.jozelot.stoneuniverse.interfaces.Registry;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class CommandRegistry implements Registry {

    private final StoneUniverse bot;

    public CommandRegistry(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public boolean register() {
        var shardmanager = bot.getBootstrap().getBotManager().getShardManager();

        shardmanager.addEventListener(new CountingCommand(bot));
        return true;
    }
}
