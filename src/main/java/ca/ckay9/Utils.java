package ca.ckay9;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Utils {
    public static String formatText(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("Village");
    }

    public static void verbosePlayerLog(Player player, String message) {
        if (!Village.verboseLogging()) {
            return;
        }

        Utils.getPlugin().getLogger()
                .info("Player Log: " + player.getName() + " (" + player.getUniqueId().toString() + ") -> " + message);
    }

    public static void verboseLog(String message) {
        if (!Village.verboseLogging()) {
            return;
        }

        Utils.getPlugin().getLogger().info("Log -> " + message);
    }
}
