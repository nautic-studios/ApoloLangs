package com.github.nautic.github;

import com.github.nautic.ApoloLangs;
import com.github.nautic.manager.LanguageManager;

public class GitHubSyncManager {

    private final ApoloLangs plugin;
    private final LanguageManager languageManager;

    public GitHubSyncManager(ApoloLangs plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }

    public GitHubSyncResult sync() {

        try {
            GitHubConfig cfg = GitHubConfig.load(plugin.getConfig());

            GitHubZipSynchronizer synchronizer =
                    new GitHubZipSynchronizer(cfg, plugin.getDataFolder());

            GitHubSyncResult result = synchronizer.execute();

            if (result == GitHubSyncResult.SUCCESS && cfg.reloadAfterSync) {
                languageManager.reloadLanguages(plugin.getConfig());
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return GitHubSyncResult.FAILED;
        }
    }
}
