package com.github.nautic.commands.customs;

import com.github.nautic.ApoloLangs;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public class LangsLoader {

    public static void registerLanguageCommands(ApoloLangs plugin) {
        List<String> aliases = plugin.getMainConfig().getStringList("commands");

        if (aliases == null || aliases.isEmpty()) {
            plugin.getLogger().warning("No language commands registered (commands list is empty)");
            return;
        }

        try {
            Field commandMapField = Bukkit.getServer()
                    .getClass()
                    .getDeclaredField("commandMap");

            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            for (String alias : aliases) {
                alias = alias.toLowerCase();

                PluginCommand cmd = createPluginCommand(alias, plugin);
                if (cmd == null) continue;

                cmd.setExecutor(new AliasLangExecutor(plugin));
                cmd.setTabCompleter(new AliasLangTabCompleter(plugin));

                commandMap.register(plugin.getName(), cmd);
                plugin.getLogger().info("Registered language alias: /" + alias);
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register language aliases");
            e.printStackTrace();
        }
    }

    private static PluginCommand createPluginCommand(String name, Plugin plugin) {
        try {
            Constructor<PluginCommand> constructor =
                    PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, plugin);
        } catch (Exception e) {
            return null;
        }
    }
}