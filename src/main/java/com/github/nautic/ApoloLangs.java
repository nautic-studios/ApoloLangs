package com.github.nautic;

import com.github.nautic.api.ApoloAPI;
import com.github.nautic.commands.ALCommands;
import com.github.nautic.commands.ALTabCompleter;
import com.github.nautic.commands.customs.LangsLoader;
import com.github.nautic.database.DatabaseManager;
import com.github.nautic.expansion.ALExpansion;
import com.github.nautic.expansion.ApoloLangsExpansion;
import com.github.nautic.handler.LangHandler;
import com.github.nautic.manager.FileManager;
import com.github.nautic.manager.LanguageManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ApoloLangs extends JavaPlugin {

    private static ApoloLangs instance;

    private FileManager fileManager;
    private LanguageManager languageManager;
    private LangHandler langHandler;

    @Override
    public void onEnable() {

        if (instance != null) return;
        instance = this;

        int pluginId = 28633;
        Metrics metrics = new Metrics(this, pluginId);

        saveDefaultConfig();

        File baseLangFolder = new File(getDataFolder(), "languages");
        if (!baseLangFolder.exists()) {
            baseLangFolder.mkdirs();
        }

        fileManager = new FileManager(baseLangFolder);
        languageManager = new LanguageManager(fileManager);
        langHandler = new LangHandler(fileManager, languageManager);

        languageManager.loadLanguagesFromConfig(getConfig());

        DatabaseManager.loadDatabase();

        ApoloAPI.initialize(this);

        LangsLoader.registerLanguageCommands(this);

        getCommand("apololangs").setExecutor(new ALCommands(this));
        getCommand("apololangs").setTabCompleter(new ALTabCompleter(this));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ALExpansion(this).register();
            new ApoloLangsExpansion(this).register();
            getLogger().info("[ApoloLangs] PlaceholderAPI expansion registered.");
        } else {
            getLogger().warning("[ApoloLangs] PlaceholderAPI not found.");
        }

        getLogger().info("[ApoloLangs] Enabled successfully.");
    }

    @Override
    public void onDisable() {
        DatabaseManager.close();
    }
    
    public static ApoloLangs getInstance() {
        return instance;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public FileConfiguration getMainConfig() {
        return getConfig();
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public LangHandler getLangHandler() {
        return langHandler;
    }
}