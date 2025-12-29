package com.github.nautic.database;

import com.github.nautic.ApoloLangs;
import com.github.nautic.database.type.MySQL;
import com.github.nautic.database.type.SQLite;
import org.bukkit.configuration.ConfigurationSection;

public class DatabaseManager {

    private static Database database;

    public static void loadDatabase() {
        String typeName = ApoloLangs.getInstance().getMainConfig().getString("database.type");

        if (typeName == null) {
            ApoloLangs.getInstance().getLogger().warning("Missing 'database.type' in config.yml. Defaulting to SQLite.");
            database = new SQLite(ApoloLangs.getInstance());
            database.connect();
            database.load();
            return;
        }

        DatabaseType type;
        try {
            type = DatabaseType.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            ApoloLangs.getInstance().getLogger().warning("Invalid 'database.type'. Defaulting to SQLite.");
            database = new SQLite(ApoloLangs.getInstance());
            database.connect();
            database.load();
            return;
        }

        switch (type) {
            case MYSQL:
                ConfigurationSection config = ApoloLangs.getInstance().getMainConfig().getConfigurationSection("database.config");

                if (config == null) {
                    ApoloLangs.getInstance().getLogger().severe("Missing 'database.config' section for MySQL. Defaulting to SQLite.");
                    database = new SQLite(ApoloLangs.getInstance());
                    database.connect();
                    database.load();
                    return;
                }

                String user = config.getString("user");
                String password = config.getString("password");
                String host = config.getString("host");
                String dbName = config.getString("database");
                int port = config.getInt("port");

                if (user == null || password == null || host == null || dbName == null || port == 0) {
                    ApoloLangs.getInstance().getLogger().severe("Missing required MySQL configuration values. Defaulting to SQLite.");
                    database = new SQLite(ApoloLangs.getInstance());
                    database.connect();
                    database.load();
                    return;
                }

                database = new MySQL(ApoloLangs.getInstance(), host, port, dbName, user, password);
                try {
                    database.connect();
                    database.load();
                } catch (Exception e) {
                    ApoloLangs.getInstance().getLogger().severe("MySQL connection failed: " + e.getMessage());
                    e.printStackTrace();
                    ApoloLangs.getInstance().getLogger().severe("Falling back to SQLite for safety.");
                    database = new SQLite(ApoloLangs.getInstance());
                    database.connect();
                    database.load();
                }
                break;

            case SQLITE:
            default:
                database = new SQLite(ApoloLangs.getInstance());
                database.connect();
                database.load();
                break;
        }
    }

    public static Database getDatabase() {
        return database;
    }

    public static void close() {
        if (database != null) {
            database.close();
        }
    }
}