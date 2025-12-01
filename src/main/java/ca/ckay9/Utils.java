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

        String log = "Player Log: " + player.getName() + " (" + player.getUniqueId().toString() + ") -> " + message;
        if (Village.verboseLoggingInGame()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.isOp()) {
                    continue;
                }

                p.sendMessage(Utils.formatText("&9&l[VILLAGE]&r ") + log);
            }
        }

        Utils.getPlugin().getLogger().info(log);
    }

    public static void verboseLog(String message) {
        if (!Village.verboseLogging()) {
            return;
        }

        String log = "Log -> " + message;
        if (Village.verboseLoggingInGame()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.isOp()) {
                    continue;
                }

                p.sendMessage(Utils.formatText("&9&l[VILLAGE]&r ") + log);
            }
        }

        Utils.getPlugin().getLogger().info(log);
    }

    public static long ticksToSeconds(long ticks) {
        return ticks / 20;
    }

    public static long secondsToTicks(long seconds) {
        return seconds * 20;
    }
}
