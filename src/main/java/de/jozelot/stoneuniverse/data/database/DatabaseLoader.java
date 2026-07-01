package de.jozelot.stoneuniverse.data.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.jozelot.stoneuniverse.StoneUniverse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseLoader {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseLoader.class);
    private final StoneUniverse bot;
    private HikariDataSource dataSource;

    public DatabaseLoader(StoneUniverse bot) {
        this.bot = bot;
    }

    public boolean connect() {
        String host = bot.getBootstrap().getConfig().getDatabase().getHost();
        int port = bot.getBootstrap().getConfig().getDatabase().getPort();
        String database = bot.getBootstrap().getConfig().getDatabase().getDatabase();
        String user = bot.getBootstrap().getConfig().getDatabase().getUser();
        String password = bot.getBootstrap().getConfig().getDatabase().getPassword();

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
            config.setUsername(user);
            config.setPassword(password);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(10000);

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            this.dataSource = new HikariDataSource(config);
            logger.info("HikariCP Connection Pool to PostgreSQL was successfully initialized!");
            return true;
        } catch (Exception e) {
            logger.error("HikariCP Connection Pool could not be established!", e);
            return false;
        }
    }

    public boolean createTables() {
        List<String> queries = List.of(
                "CREATE TABLE IF NOT EXISTS counting (" +
                        "    id INT PRIMARY KEY DEFAULT 1, " +
                        "    current_count INT DEFAULT 0, " +
                        "    last_counter_id VARCHAR(20) DEFAULT '0', " +
                        "    rounds_played INT DEFAULT 0, " +
                        "    highscore INT DEFAULT 0" +
                        ");",

                "CREATE TABLE IF NOT EXISTS temp_channels (" +
                        "    channel_id BIGINT PRIMARY KEY," +
                        "    owner_id BIGINT NOT NULL" +
                        ");"
        );

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement()) {
                for (String query : queries) {
                    stmt.execute(query);
                }
                connection.commit();
                logger.info("All database tables created successfully!");
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Creating database tables failed!", e);
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized!");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}