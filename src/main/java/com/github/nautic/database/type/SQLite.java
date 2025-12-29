package com.github.nautic.database.type;

import com.github.nautic.ApoloLangs;
import com.github.nautic.database.Database;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class SQLite implements Database {

    private final ApoloLangs plugin;
    private HikariDataSource dataSource;

    public SQLite(ApoloLangs plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        File file = new File(plugin.getDataFolder(), "data.db");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        config.setPoolName("ApoloLangs-SQLite");
        config.setMaximumPoolSize(1);

        dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Connected to SQLite (HikariCP).");
    }

    @Override
    public void load() {
        String sql = """
                CREATE TABLE IF NOT EXISTS ApoloLangs (
                    uuid TEXT PRIMARY KEY,
                    language TEXT
                )
                """;

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create SQLite table: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private void ensurePlayer(Connection con, UUID uuid) throws SQLException {
        String sql = "INSERT OR IGNORE INTO ApoloLangs (uuid) VALUES (?)";
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