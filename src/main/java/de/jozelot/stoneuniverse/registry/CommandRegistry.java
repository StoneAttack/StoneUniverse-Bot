package de.jozelot.stoneuniverse.registry;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.commands.IPCommand;
import de.jozelot.stoneuniverse.commands.LeaderboardCommand;
import de.jozelot.stoneuniverse.commands.RankCommand;
import de.jozelot.stoneuniverse.core.BotBootstrap;
import de.jozelot.stoneuniverse.interfaces.Command;
import de.jozelot.stoneuniverse.interfaces.Registry;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CommandRegistry extends ListenerAdapter implements Registry {

    private final StoneUniverse bot;

    public CommandRegistry(StoneUniverse bot) {
        this.bot = bot;
    }

    private static final Logger logger = LoggerFactory.getLogger(CommandRegistry.class);
    private final Map<String, Command> commands = new HashMap<>();

    public void registerCommand(Command command) {
        commands.put(command.getCommandData().getName(), command);

        for (CommandData alias : command.getAliases()) {
            commands.put(alias.getName(), command);
        }
    }

    @Override
    public boolean register() {
        registerCommand(new RankCommand(bot));
        registerCommand(new LeaderboardCommand(bot));
        registerCommand(new IPCommand(bot));
        return true;
    }

    // FRAMEWORK

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Command command = commands.get(event.getName());

        if (command != null) {
            command.execute(event);
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        String currentGuildId = event.getGuild().getId();
        List<CommandData> guildCommands = new ArrayList<>();

        Set<Command> uniqueCommands = new HashSet<>(commands.values());

        for (Command cmd : uniqueCommands) {
            if (currentGuildId.equals(cmd.getGuildId())) {
                guildCommands.add(cmd.getCommandData());
                guildCommands.addAll(cmd.getAliases());
            }
        }

        if (!guildCommands.isEmpty()) {
            event.getGuild().updateCommands().addCommands(guildCommands).queue();
            logger.info("Registered {} guild commands (including aliases) for guild {}", guildCommands.size(), currentGuildId);
        }
    }
}
