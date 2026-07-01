package de.jozelot.stoneuniverse.data.hosts;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.interfaces.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

public class HostsLoader implements FileConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(HostsLoader.class);
    private final StoneUniverse bot;

    public HostsLoader(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public boolean load() {
        logger.info("Loading configuration (hosts.yml)...");

        File configFile = new File("hosts.yml");

        try {
            if (!configFile.exists()) {
                logger.info("config.yml not found. Creating default configuration file.");
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("hosts.yml")) {
                    if (in == null) {
                        logger.error("Default hosts.yml could not be found inside the jar resources!");
                        return false;
                    }
                    Files.copy(in, configFile.toPath());
                }
            }

            Yaml yaml = new Yaml();
            try (InputStream inputStream = new FileInputStream(configFile)) {
                // LÄDT DIE DATEN IN DEN HOST MANGER
                var hosts = bot.getBootstrap().getHosts();
                hosts.setConfigurationData(yaml.load(inputStream));
                hosts.load();

            }

            logger.info("Hosts successfully loaded.");
            return true;

        } catch (Exception e) {
            logger.error("Error while trying to load 'hosts.yml'", e);
            return false;
        }
    }

    @Override
    public void unload() {
        bot.getBootstrap().getHosts().setConfigurationData(null);
    }


    @Override
    public void reload() {
        unload();
        load();
    }
}
