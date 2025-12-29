package com.github.nautic.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static String Set(String message) {
        return TranslateColor(TranslateHex(SafeMiniMessage(message)));
    }

    public static String TranslateColor(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String TranslateHex(String message) {
        final char colorChar = ChatColor.COLOR_CHAR;
        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            final String group = matcher.group(1);
            matcher.appendReplacement(buffer, colorChar + "x"
                    + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                    + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                    + colorChar + group.charAt(4) + colorChar + group.charAt(5));
        }

        return matcher.appendTail(buffer).toString();
    }

    public static String SafeMiniMessage(String message) {
        try {
            Component component = MINI_MESSAGE.deserialize(message);
            return LegacyComponentSerializer.legacySection().serialize(component);
        } catch (Exception e) {
            return message;
        }
    }
}