package ca.ckay9;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

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

    /**
     * @param start Where the ray starts
     * @param end Where it ends
     * @param maxDistance How far should the ray go
     * @param step How much should the ray travel each iteration
     * @return A location on the ray that is either maxDistance away or the first block hit, null if failed
     */
    public static Location validPointerLocation(Location start, Location end, double maxDistance, double step) {
        if (!start.getWorld().getUID().equals(end.getWorld().getUID())) {
            return null;
        }

        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double distance = Math.min(start.distance(end), maxDistance);

        Location validLocation = null;
        for (double d = 0; d < distance; d += step) {
            Location point = start.clone().add(direction.clone().multiply(d));

            if (point.getBlock().getType().isSolid()) {
                validLocation = point.subtract(direction.clone().multiply(0.1));
                break;
            }
        }

        if (validLocation == null) {
            validLocation = start.clone().add(direction.multiply(distance));
        }

        validLocation.add(0, 1, 0);

        return validLocation;
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
