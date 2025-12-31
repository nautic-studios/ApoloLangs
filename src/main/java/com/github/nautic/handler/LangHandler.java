package com.github.nautic.handler;

import com.github.nautic.manager.FileManager;
import com.github.nautic.manager.LanguageManager;
import com.github.nautic.utils.ColorUtils;

import java.util.List;

public class LangHandler {

    private final FileManager fileManager;
    private final LanguageManager languageManager;

    public LangHandler(FileManager fileManager, LanguageManager languageManager) {
        this.fileManager = fileManager;
        this.languageManager = languageManager;
    }

    public String get(String langInput, String filePath, String path) {

        String langFolder = languageManager.resolveLanguageStrict(langInput);
        if (langFolder == null) {
            langFolder = languageManager.getDefaultLang();
        }

        String fileId = langFolder + ":" + filePath.toLowerCase();

        if (!fileManager.isLoaded(fileId)) {
            fileManager.loadByLangAndPath(langFolder, filePath + ".yml");

            if (!fileManager.isLoaded(fileId)) {
                return getSystemMessageOrDefault(
                        langFolder,
                        "file_not_found",
                        "&cFile not found &7[" + filePath + "]"
                ).replace("{file}", filePath);
            }
        }

        String text = fileManager.get(fileId, path);
        if (text == null) {
            return getSystemMessageOrDefault(
                    langFolder,
                    "not_translated",
                    "&fNot translated &7» &a" + path
            ).replace("{path}", path);
        }

        return ColorUtils.Set(text.trim());
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

    public List<String> getList(String langInput, String filePath, String path) {

        String langFolder = languageManager.resolveLanguageStrict(langInput);
        if (langFolder == null) {
            langFolder = languageManager.getDefaultLang();
        }

        String fileId = langFolder + ":" + filePath.toLowerCase();

        if (!fileManager.isLoaded(fileId)) {
            fileManager.loadByLangAndPath(langFolder, filePath + ".yml");
        }

        if (fileManager.isLoaded(fileId)) {
            var cfg = fileManager.getConfig(fileId);
            if (cfg != null && cfg.isList(path)) {
                return cfg.getStringList(path);
            }
        }

        String defLang = languageManager.getDefaultLang();
        String defFileId = defLang + ":" + filePath.toLowerCase();

        if (!fileManager.isLoaded(defFileId)) {
            fileManager.loadByLangAndPath(defLang, filePath + ".yml");
        }

        if (fileManager.isLoaded(defFileId)) {
            var cfg = fileManager.getConfig(defFileId);
            if (cfg != null && cfg.isList(path)) {
                return cfg.getStringList(path);
            }
        }

        return java.util.Collections.emptyList();
    }
}