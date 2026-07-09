package de.jozelot.stoneuniverse.interfaces;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public interface Command {
    CommandData getCommandData();
    default List<CommandData> getAliases() {
        return List.of();
    }
    void execute(SlashCommandInteractionEvent event);
    default void autoComplete(CommandAutoCompleteInteractionEvent event) {};
    default String getGuildId() {
        return null;
    }
    default Permission getPermission() { return null; }
}
