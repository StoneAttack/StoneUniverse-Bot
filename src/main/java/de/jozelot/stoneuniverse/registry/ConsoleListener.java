package de.jozelot.stoneuniverse.registry;

import de.jozelot.stoneuniverse.StoneUniverse;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleListener {

    private final StoneUniverse bot;

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
                LoggerFactory.getLogger(this.getClass()).error("Error while reading the console!", e);
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
            case "stop", "shutdown" -> {
                System.out.println("Shutdown registered! Shutting down..");
                System.exit(0);
            }
            case "stats" -> {
                var shardManager = bot.getBootstrap().getBotManager().getShardManager();
                if (shardManager != null) {
                    System.out.println("=== BOT STATS ===");
                    System.out.println("Ping: " + shardManager.getAverageGatewayPing() + "ms");
                }
            }
            case "reload" -> {
                System.out.println("Reloading...");
                bot.getBootstrap().reload();
            }
            case "help" -> {
                System.out.println("Use theese commands: 'stop', 'stats', 'reload'");
            }
            default -> System.out.println("Unknown command: '" + command + "'. Use 'stop', 'reload' or 'stats'.");
        }
    }
}
