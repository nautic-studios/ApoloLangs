package com.github.nautic.commands;

import com.github.nautic.ApoloLangs;
import com.github.nautic.database.DatabaseManager;
import com.github.nautic.handler.LangHandler;
import com.github.nautic.manager.LanguageManager;
import com.github.nautic.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ALCommands implements CommandExecutor {

    private final ApoloLangs plugin;
    private final LanguageManager languageManager;
    private final LangHandler lang;

    public ALCommands(ApoloLangs plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.lang = plugin.getLangHandler();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String userLang = resolveUserLang(sender);

        if (args.length == 0) {
            sender.sendMessage("");

            sender.sendMessage(ColorUtils.Set(
                    "     &#35ADFF&l ApoloLangs &#CDCDCD| &fVersion: &#38FF35"
            ) + plugin.getDescription().getVersion());

            sender.sendMessage(ColorUtils.Set(
                    "       &fPowered by &#3F92FFNautic Studios"
            ));

            sender.sendMessage("");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "help": {
                sendHelp(sender, userLang);
                return true;
            }

            case "list": {
                if (!sender.hasPermission("apololangs.list") && !sender.hasPermission("apololangs.admin")) {
                    noPerm(sender, userLang);
                    return true;
                }

                sender.sendMessage(ColorUtils.Set(
                        lang.get(userLang, userLang, "list.header")
                ));

                for (Map.Entry<String, String> entry : languageManager.getLanguageMap().entrySet()) {
                    sender.sendMessage(ColorUtils.Set(
                            lang.get(userLang, userLang, "list.format")
                                    .replace("{locale}", entry.getKey())
                                    .replace("{language}", entry.getValue())
                    ));
                }
                return true;
            }

            case "aliases": {
                if (!sender.hasPermission("apololangs.aliases")
                        && !sender.hasPermission("apololangs.admin")) {
                    noPerm(sender, userLang);
                    return true;
                }

                sender.sendMessage(ColorUtils.Set(
                        lang.get(userLang, userLang, "aliases.header")
                ));

                for (String cmd : plugin.getConfig().getStringList("commands")) {
                    sender.sendMessage(ColorUtils.Set(
                            lang.get(userLang, userLang, "aliases.format")
                                    .replace("{command}", "/" + cmd)
                    ));
                }
                return true;
            }

            case "set": {
                if (!sender.hasPermission("apololangs.set") && !sender.hasPermission("apololangs.admin")) {
                    noPerm(sender, userLang);
                    return true;
                }

                if (args.length != 3) {
                    sender.sendMessage(ColorUtils.Set(
                            lang.get(userLang, userLang, "usage.set")
                    ));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                UUID uuid = target.getUniqueId();

                String resolved = languageManager.resolveLanguageStrict(args[2]);
                if (resolved == null) {
                    sender.sendMessage(ColorUtils.Set(
                            lang.get(userLang, userLang, "errors.language-not-found")
                                    .replace("{input}", args[2])
                    ));
                    return true;
                }

                DatabaseManager.getDatabase().setLanguagePlayer(uuid, resolved);

                sender.sendMessage(ColorUtils.Set(
                        lang.get(userLang, userLang, "success.other-language-set")
                                .replace("{player}", target.getName())
                                .replace("{language}", resolved)
                ));
                return true;
            }

            case "reset": {
                if (!sender.hasPermission("apololangs.reset") && !sender.hasPermission("apololangs.admin")) {
                    noPerm(sender, userLang);
                    return true;
                }

                if (args.length != 2) {
                    sender.sendMessage(ColorUtils.Set(
                            lang.get(userLang, userLang, "usage.reset")
                    ));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                DatabaseManager.getDatabase().setLanguagePlayer(
                        target.getUniqueId(),
                        languageManager.getDefaultLang()
                );

                sender.sendMessage(ColorUtils.Set(
                        lang.get(userLang, userLang, "success.reset")
                                .replace("{player}", target.getName())
                ));
                return true;
            }

            case "info": {
                if (!sender.hasPermission("apololangs.info") && !sender.hasPermission("apololangs.admin")) {
                    noPerm(sender, userLang);
                    return true;
                }

                if (args.length != 2) {
                    sender.sendMessage(ColorUtils.Set(
                            lang.get(userLang, userLang, "usage.info")
                    ));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                String targetLang = DatabaseManager.getDatabase()
                        .getLanguagePlayer(target.getUniqueId());

                if (targetLang == null || !languageManager.isRegisteredLanguage(targetLang)) {
                    targetLang = languageManager.getDefaultLang();
                }

                sender.sendMessage(ColorUtils.Set(
                        lang.get(userLang, userLang, "info.format")
                                .replace("{player}", target.getName())
                                .replace("{language}", targetLang)
                ));
                return true;
            }

            case "reload": {
                if (!sender.hasPermission("apololangs.reload") && !sender.hasPermission("apololangs.admin")) {
                    noPerm(sender, userLang);
                    return true;
                }

                plugin.reloadConfig();
                languageManager.reloadLanguages(plugin.getConfig());

                sender.sendMessage(ColorUtils.Set(
                        lang.get(userLang, userLang, "success.reload")
                ));
                return true;
            }

            default: {
                sendHelp(sender, userLang);
                return true;
            }
        }
    }

    private String resolveUserLang(CommandSender sender) {
        if (sender instanceof Player player) {
            String langCode = DatabaseManager.getDatabase()
                    .getLanguagePlayer(player.getUniqueId());

            if (langCode != null && languageManager.isRegisteredLanguage(langCode)) {
                return langCode;
            }
        }
        return languageManager.getDefaultLang();
    }

    private void noPerm(CommandSender sender, String langCode) {
        sender.sendMessage(ColorUtils.Set(
                lang.get(langCode, langCode, "errors.no-permission")
        ));
    }

    private void sendHelp(CommandSender sender, String userLang) {

        String helpText = lang.get(userLang, userLang, "help");

        for (String line : helpText.split("\n")) {
            if (line.trim().equalsIgnoreCase("<empty>")) {
                sender.sendMessage("");
                continue;
            }

            sender.sendMessage(
                    ColorUtils.Set(line)
                            .replace("{version}", plugin.getDescription().getVersion())
            );
        }
    }
}
