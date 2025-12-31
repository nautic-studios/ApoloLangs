package com.github.nautic.handler;

import com.github.nautic.manager.FileManager;
import com.github.nautic.manager.LanguageManager;
import com.github.nautic.utils.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class LangHandler {

    private final FileManager fileManager;
    private final LanguageManager languageManager;

    public LangHandler(FileManager fileManager, LanguageManager languageManager) {
        this.fileManager = fileManager;
        this.languageManager = languageManager;
    }

    public String get(String langInput, String filePath, String path) {
        return get(null, langInput, filePath, path);
    }

    public String get(Player player, String langInput, String filePath, String path) {

        String langFolder = languageManager.resolveLanguageStrict(langInput);
        if (langFolder == null) {
            langFolder = languageManager.getDefaultLang();
        }

        String fileId = langFolder + ":" + filePath.toLowerCase();

        if (!fileManager.isLoaded(fileId)) {
            fileManager.loadByLangAndPath(langFolder, filePath + ".yml");
        }

        if (!fileManager.isLoaded(fileId)) {
            return ColorUtils.SetPlaceholders(player,
                    getSystemMessageOrDefault(
                            langFolder,
                            "file_not_found",
                            "&cFile not found &7[" + filePath + "]"
                    ).replace("{file}", filePath)
            );
        }

        FileConfiguration cfg = fileManager.getConfig(fileId);
        if (cfg == null) {
            return ColorUtils.SetPlaceholders(player,
                    getSystemMessage(langFolder, "invalid_lang_format")
            );
        }

        String result = null;

        if (cfg.isString(path)) {
            result = cfg.getString(path);
        }

        else if (cfg.isList(path)) {
            List<String> list = cfg.getStringList(path);
            if (!list.isEmpty()) {
                result = String.join("\n", list);
            }
        }

        if (result == null) {
            result = getSystemMessageOrDefault(
                    langFolder,
                    "not_translated",
                    "&fNot translated &7» &a" + path
            ).replace("{path}", path);
        }

        return ColorUtils.SetPlaceholders(player, result.trim());
    }

    public String getSystemMessage(String langFolder, String key) {
        String systemId = langFolder.toLowerCase() + ":" + langFolder.toLowerCase();

        if (!fileManager.isLoaded(systemId)) {
            return ColorUtils.Set("&cSystem file missing");
        }

        String msg = fileManager.get(systemId, key);
        if (msg == null) {
            return ColorUtils.Set("&cSystem message missing");
        }

        return ColorUtils.Set(msg.trim());
    }

    private String getSystemMessageOrDefault(String langFolder, String key, String defaultMsg) {
        String systemId = langFolder.toLowerCase() + ":" + langFolder.toLowerCase();

        if (!fileManager.isLoaded(systemId)) {
            return ColorUtils.Set(defaultMsg);
        }

        String msg = fileManager.get(systemId, key);
        if (msg == null) {
            return ColorUtils.Set(defaultMsg);
        }

        return ColorUtils.Set(msg.trim());
    }
}