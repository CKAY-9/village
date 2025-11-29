package ca.ckay9;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.bukkit.configuration.file.YamlConfiguration;

public class Storage {
    public static File configFile;
    public static YamlConfiguration config;

    public static File worldsFile;
    public static YamlConfiguration worldsData;

    public static void initializeConfig() {
        try {
            configFile = new File(Utils.getPlugin().getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                if (configFile.getParentFile().mkdirs()) {
                    Utils.getPlugin().getLogger().info("Created Villager folder!");
                }

                if (configFile.createNewFile()) {
                    Utils.getPlugin().getLogger().info("Created config file!");
                }
            }

            config = YamlConfiguration.loadConfiguration(configFile);

            if (!config.isSet("debug.developer")) {
                config.set("debug.developer", false);
                config.setInlineComments("debug.developer", Collections.singletonList("This will skip a lot of checks for roles, game status, etc."));
            }

            if (!config.isSet("debug.verboseLogging")) {
                config.set("debug.verboseLogging", false);
                config.setInlineComments("debug.verboseLogging", Collections.singletonList("Logs a lot. Recommend having disabled but can be useful."));
            }

            if (!config.isSet("tasks.doThemAll")) {
                config.set("tasks.doThemAll", true);
                config.setInlineComments("tasks.doThemAll", Collections.singletonList("Every villager will have to do every task. This overrides any other values."));
            }

            config.save(configFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static void initializeWorldsData() {
        try {
            worldsFile = new File(Utils.getPlugin().getDataFolder(), "worlds.yml");
            if (!worldsFile.exists()) {
                if (worldsFile.getParentFile().mkdirs()) {
                    Utils.getPlugin().getLogger().info("Created Villager folder!");
                }

                if (worldsFile.createNewFile()) {
                    Utils.getPlugin().getLogger().info("Created worlds file!");
                }
            }

            worldsData = YamlConfiguration.loadConfiguration(worldsFile);
            worldsData.save(worldsFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }
}
