package io.github.lukeeff.minecraft17;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import static org.bukkit.ChatColor.translateAlternateColorCodes;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class Logger {

    public static void log(String msg) {
        msg = translateAlternateColorCodes('&', "&3[&d" + getPlugin(Minecraft17.class).getName() + "&3]&r " + msg);
        if (!Config.COLOR_LOGS.getBoolean()) {
            msg = ChatColor.stripColor(msg);
        }
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public static void debug(String msg) {
        if (Config.DEBUG_MODE.getBoolean()) {
            log("&7[&eDEBUG&7]&r " + msg);
        }
    }
}
