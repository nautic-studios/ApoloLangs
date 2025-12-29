package com.github.nautic.database.type;

import com.github.nautic.ApoloLangs;
import com.github.nautic.database.Database;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.UUID;

public class MySQL implements Database {

    private final ApoloLangs plugin;
    private final String host, database, username, password;
    private final int port;

    private HikariDataSource dataSource;

    public MySQL(ApoloLangs plugin, String host, int port, String database, String username, String password) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public void connect() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://" + host + ":" + port + "/" + database +
                        "?useSSL=false&characterEncoding=utf8"
        );
        config.setUsername(username);
        config.setPassword(password);

        config.setPoolName("ApoloLangs-MySQL");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10_000);

        dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Connected to MySQL (HikariCP).");
    }

    @Override
    public void load() {
        String sql = """
                CREATE TABLE IF NOT EXISTS ApoloLangs (
                    uuid VARCHAR(36) PRIMARY KEY,
                    language VARCHAR(255)
                )
                """;

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create MySQL table: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private void ensurePlayer(Connection con, UUID uuid) throws SQLException {
        String sql = "INSERT IGNORE INTO ApoloLangs (uuid) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }

    private void updateField(UUID uuid, String column, String value) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            ensurePlayer(con, uuid);

            String sql = "UPDATE ApoloLangs SET " + column + "=? WHERE uuid=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, value);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            }
        }
    }

    private String getField(UUID uuid, String column) throws SQLException {
        String sql = "SELECT " + column + " FROM ApoloLangs WHERE uuid=?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString(column) : null;
        }
    }

    @Override
    public void setLanguagePlayer(UUID uuid, String language) {
        try {
            updateField(uuid, "language", language);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set language: " + e.getMessage());
        }
    }

    @Override
    public String getLanguagePlayer(UUID uuid) {
        try {
            return getField(uuid, "language");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get language: " + e.getMessage());
            return null;
        }
    }
}
