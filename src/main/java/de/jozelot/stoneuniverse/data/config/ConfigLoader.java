package de.jozelot.stoneuniverse.data.config;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.core.BotBootstrap;
import de.jozelot.stoneuniverse.interfaces.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

public class ConfigLoader implements FileConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private final StoneUniverse bot;

    public ConfigLoader(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public boolean load() {
        logger.info("Loading configuration (config.yml)...");

        File configFile = new File("config.yml");

        try {
            if (!configFile.exists()) {
                logger.info("config.yml not found. Creating default configuration file.");
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (in == null) {
                        logger.error("Default config.yml could not be found inside the jar resources!");
                        return false;
                    }
                    Files.copy(in, configFile.toPath());
                }
            }

            Yaml yaml = new Yaml();
            try (InputStream inputStream = new FileInputStream(configFile)) {
                // LÄDT DIE DATEN IN DEN CONFIG MANGER
                var config = bot.getBootstrap().getConfig();
                config.setConfigurationData(yaml.load(inputStream));
                config.load();

            }

            logger.info("Configuration successfully loaded.");
            return true;

        } catch (Exception e) {
            logger.error("Error while trying to load 'config.yml'", e);
            return false;
        }
    }

    @Override
    public void unload() {
        bot.getBootstrap().getConfig().setConfigurationData(null);
    }


    @Override
    public void reload() {
        unload();
        load();
    }
}
