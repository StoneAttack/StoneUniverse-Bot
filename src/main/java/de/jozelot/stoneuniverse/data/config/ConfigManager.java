package de.jozelot.stoneuniverse.data.config;

import de.jozelot.stoneuniverse.StoneUniverse;

import kotlin.jvm.internal.SourceDebugExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private final StoneUniverse bot;

    private BotConf botConf;
    private Database database;
    private Minigames minigames;
    private System system;

    private Map<String, Object> configurationData;

    public ConfigManager(StoneUniverse bot) {
        this.bot = bot;
        this.botConf = new BotConf();
        this.database = new Database();
        this.minigames = new Minigames();
        this.system = new System();
    }

    public class BotConf {
        private String token;
        private Status status = new Status();

        public static class Status {
            private String server;
            private int port;
            private int updateInterval;

            private final Map<String, ActivityConfig> activities = new HashMap<>();

            public String getServer() { return server; }
            public int getPort() { return port; }
            public int getUpdateInterval() { return updateInterval; }

            public ActivityConfig getActivity(ActivityState state) {
                return activities.get(state.name().toLowerCase());
            }

            public void addActivity(String key, ActivityConfig config) {
                this.activities.put(key.toLowerCase(), config);
            }
        }

        public static class ActivityConfig {
            private String type;
            private String format;
            private String status;

            public ActivityConfig(String type, String format, String status) {
                this.type = type;
                this.format = format;
                this.status = status;
            }

            public String getType() { return type; }
            public String getFormat() { return format; }
            public String getStatus() { return status; }
        }

        public String getToken() { return token; }
        public Status getStatus() { return status; }
    }

    public class Database {
        private String host;
        private int port;
        private String user;
        private String password;
        private String database;

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public String getDatabase() {
            return database;
        }
    }

    public class Minigames {
        public class Counting {
            private long channelId;

            public long getChannelId() {
                return channelId;
            }
        }

        private final Counting counting = new Counting();

        public Counting getCounting() {
            return counting;
        }
    }

    public class System {
        public class TempChannel {
            private long channelId;
            private long categoryId;
            private String defaultFormat;

            public long getChannelId() {
                return channelId;
            }
            public long getCategoryId() {
                return categoryId;
            }
            public String getDefaultFormat() {
                return defaultFormat;
            }
        }

        public class Level {
            private int xpCooldown;

            public int getXpCooldown() {
                return xpCooldown;
            }
        }

        private final TempChannel tempChannel = new TempChannel();
        private final Level level = new Level();

        public TempChannel getTempChannel() {
            return tempChannel;
        }
        public Level getLevel() {
            return level;
        }
    }

    public void load() {
        if (configurationData == null) {
            logger.error("Configuration data is null! Cannot load config.");
            return;
        }

        botConf.token = getString("bot.token");


        // LOAD STATUS
        botConf.status.server = getString("bot.status.server");
        botConf.status.port = getInt("bot.status.port", 25565);
        botConf.status.updateInterval = getInt("bot.status.update-interval", 180);

        Object rawActivities = getObject("bot.status.activities");

        if (rawActivities instanceof Map) {
            Map<?, ?> activitiesSection = (Map<?, ?>) rawActivities;

            for (Object rawKey : activitiesSection.keySet()) {
                String key = rawKey.toString();

                String type = getString("bot.status.activities." + key + ".type");
                String format = getString("bot.status.activities." + key + ".format");
                String status = getString("bot.status.activities." + key + ".status");

                BotConf.ActivityConfig activity = new BotConf.ActivityConfig(type, format, status);
                botConf.status.addActivity(key, activity);
            }
        } else {
            logger.warn("Could not find 'bot.status.activities' section or it is not a valid configuration block.");
        }

        logger.info("Config was fully loaded");

        database.host = getString("database.host");
        database.port = getInt("database.port");
        database.user = getString("database.username");
        database.password = getString("database.password");
        database.database = getString("database.database");

        minigames.counting.channelId = getLong("minigames.counting.channel-id");

        system.tempChannel.channelId = getLong("system.temp-channel.channel-id");
        system.tempChannel.defaultFormat = getString("system.temp-channel.default-format");
        system.tempChannel.categoryId = getLong("system.temp-channel.category-id");

        system.level.xpCooldown = getInt("system.level.xp-cooldown");
    }


    public BotConf getBotConf() {
        return botConf;
    }
    public Database getDatabase() {
        return database;
    }
    public Minigames getMinigames() {
        return minigames;
    }
    public System getSystem() {
        return system;
    }
    // BEREICH FÜR DIE GENERELLEN GETTER METHODEN

    // STRING
    public String getString(String path) {
        Object obj = getObject(path);
        return obj != null ? obj.toString() : null;
    }

    public String getString(String path, String def) {
        String val = getString(path);
        return val != null ? val : def;
    }

    // INT
    public int getInt(String path) {
        return getInt(path, 0);
    }

    public int getInt(String path, int def) {
        Object obj = getObject(path);
        return obj instanceof Number ? ((Number) obj).intValue() : def;
    }

    // BOOLEAN
    public boolean getBoolean(String path, boolean def) {
        Object obj = getObject(path);
        return obj instanceof Boolean ? (Boolean) obj : def;
    }

    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    // DOUBLE
    public double getDouble(String path, double def) {
        Object obj = getObject(path);
        return obj instanceof Number ? ((Number) obj).doubleValue() : def;
    }

    public double getDouble(String path) {
        return getDouble(path, 0.0);
    }

    public long getLong(String path) {
        return getLong(path, 0L);
    }

    public long getLong(String path, long def) {
        Object obj = getObject(path);
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return def;
            }
        }
        return def;
    }


    // LISTEN

    public List<?> getList(String path) {
        Object obj = getObject(path);
        return obj instanceof List ? (List<?>) obj : new ArrayList<>();
    }

    public List<String> getStringList(String path) {
        List<?> rawList = getList(path);
        List<String> list = new ArrayList<>();

        for (Object obj : rawList) {
            if (obj != null) {
                list.add(obj.toString());
            }
        }
        return list;
    }

    public List<Integer> getIntList(String path) {
        List<?> rawList = getList(path);
        List<Integer> list = new ArrayList<>();

        for (Object obj : rawList) {
            if (obj instanceof Number) {
                list.add(((Number) obj).intValue());
            }
        }
        return list;
    }

    public List<Double> getDoubleList(String path) {
        List<?> rawList = getList(path);
        List<Double> list = new ArrayList<>();

        for (Object obj : rawList) {
            if (obj instanceof Number) {
                list.add(((Number) obj).doubleValue());
            }
        }
        return list;
    }

    private Object getObject(String path) {
        String[] keys = path.split("\\.");
        Map<String, Object> currentMap = configurationData;

        for (int i = 0; i < keys.length; i++) {
            Object obj = currentMap.get(keys[i]);
            if (i == keys.length - 1) {
                return obj;
            }
            if (obj instanceof Map) {
                currentMap = (Map<String, Object>) obj;
            } else {
                break;
            }
        }
        return null;
    }

    public List<Long> getLongList(String path) {
        List<?> rawList = getList(path);
        List<Long> list = new ArrayList<>();

        for (Object obj : rawList) {
            if (obj instanceof Number) {
                list.add(((Number) obj).longValue());
            } else if (obj != null) {
                try {
                    list.add(Long.parseLong(obj.toString()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return list;
    }

    public void setConfigurationData(Map<String, Object> newConfigurationData) {
        configurationData = newConfigurationData;
    }
}
