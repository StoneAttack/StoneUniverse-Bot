package de.jozelot.stoneuniverse.commands;

import de.jozelot.stoneuniverse.StoneUniverse;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CountingCommand extends ListenerAdapter {

    private final StoneUniverse bot;

    public CountingCommand(StoneUniverse bot) {
        this.bot = bot;
    }
}