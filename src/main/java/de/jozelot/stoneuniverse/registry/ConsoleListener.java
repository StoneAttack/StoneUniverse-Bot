package de.jozelot.stoneuniverse.registry;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.commands.GiveawayCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleListener {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(ConsoleListener.class);

    public ConsoleListener(StoneUniverse bot) {
        this.bot = bot;
    }

    public void startConsoleListener() {
        Thread consoleThread = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    executeConsoleCommand(line.trim());
                }
            } catch (Exception e) {
                logger.error("Error while reading the console!", e);
            }
        }, "Console-Reader");

        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    private void executeConsoleCommand(String input) {
        if (input.isEmpty()) return;

        String[] args = input.split(" ", 2);
        String command = args[0].toLowerCase();

        switch (command) {
            case "stop", "shutdown", "exit" -> {
                logger.info("Shutdown sequence initiated via console.");
                System.exit(0);
            }
            case "stats" -> {
                var shardManager = bot.getBootstrap().getBotManager().getShardManager();
                if (shardManager != null) {
                    logger.info("Bot Statistics - Average Gateway Ping: {}ms", shardManager.getAverageGatewayPing());
                } else {
                    logger.warn("Stats failed: ShardManager is not initialized yet.");
                }
            }
            case "reload" -> {
                logger.info("Reloading configuration and systems...");
                bot.getBootstrap().reload();
                logger.info("Reload finished.");
            }
            case "help" -> {
                logger.info("Available console commands: 'stop', 'shutdown', 'exit', 'stats', 'reload', 'help', 'info'");
            }
            case "info", "version" -> {
                Runtime runtime = Runtime.getRuntime();
                long maxMemory = runtime.maxMemory() / 1024 / 1024;
                long allocatedMemory = runtime.totalMemory() / 1024 / 1024;
                long freeMemory = runtime.freeMemory() / 1024 / 1024;
                long usedMemory = allocatedMemory - freeMemory;

                logger.info("--- System Info ---");
                logger.info("Java Version: {}", System.getProperty("java.version"));
                logger.info("OS: {}", System.getProperty("os.name"));
                logger.info("Memory: {}MB / {}MB (Max: {}MB)", usedMemory, allocatedMemory, maxMemory);
            }
            default -> logger.info("Unknown command: '{}'. Use 'help' to see all available commands.", command);
        }
    }
}
