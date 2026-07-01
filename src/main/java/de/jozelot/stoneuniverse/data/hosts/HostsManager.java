package de.jozelot.stoneuniverse.data.hosts;

import de.jozelot.stoneuniverse.StoneUniverse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HostsManager {

    private static final Logger logger = LoggerFactory.getLogger(HostsManager.class);
    private final StoneUniverse bot;

    private Map<String, Object> configurationData;

    private Host java;
    private Host bedrock;
    private String stand;

    public HostsManager(StoneUniverse bot) {
        this.bot = bot;
    }

    public class Host {

        private String hostname;
        private int port;

        public Host(String hostname, int port) {
            this.hostname = hostname;
            this.port = port;
        }

        public String getHostname() {
            return hostname;
        }
        public int getPort() {
            return port;
        }
    }

    public void load() {
        java = new Host(getString("java.hostname"), getInt("java.port"));
        bedrock = new Host(getString("bedrock.hostname"), getInt("bedrock.port"));

        stand = getString("stand");
        logger.info("Hosts Config was fully loaded");
    }

    public Host getJava() {
        return java;
    }
    public Host getBedrock() {
        return bedrock;
    }
    public String getStand() {
        return stand;
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

    public void setConfigurationData(Map<String, Object> newConfigurationData) {
        configurationData = newConfigurationData;
    }
}
