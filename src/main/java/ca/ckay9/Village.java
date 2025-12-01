package ca.ckay9;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ca.ckay9.Commands.VillageCommand;
import ca.ckay9.Commands.VillageCompletor;
import ca.ckay9.Editor.Editor;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Listeners.PlayerLeave;

public class Village extends JavaPlugin {
    private Game game;
    private Editor editor;

    /**
     * @return Returns the instance of the game
     */
    public Game getGame() {
        return this.game;
    }

    /**
     * @return Returns the instance of the editor
     */
    public Editor getEditor() {
        return this.editor;
    }

    public static boolean inDeveloperDebug() {
        return Storage.config.getBoolean("debug.developer", false);
    }

    public static boolean verboseLogging() {
        return Storage.config.getBoolean("debug.verboseLogging", false);
    }

    public static boolean verboseLoggingInGame() {
        return Storage.config.getBoolean("debug.verboseLoggingInGame", false);
    }

    private void checkVersion() {
        String lv = "0.0.0";
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=130487");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            lv = in.readLine();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String currentVersion = getDescription().getVersion();
        if (lv != null && !currentVersion.equals(lv)) {
            getLogger().warning("\n\nThis version of Village is out of date. The most recent version is " + lv
                    + " (installed: " + currentVersion
                    + ").\nDo I need to update? No, but it probably has more features and bug patches.\n"
                    + "If you want to update, go here\n - https://github.com/CKAY-9/village\n - https://www.spigotmc.org/resources/village.130487/\n");
        } else {
            getLogger().info("Passed version check. Village is running on the latest version.");
        }
    }

    @Override
    public void onEnable() {
        Storage.initializeConfig();
        Storage.initializeWorldsData();
        Storage.initializeCustomTasksData();

        this.game = new Game(this);
        this.editor = new Editor(this);

        this.getCommand("village").setExecutor(new VillageCommand(this));
        this.getCommand("village").setTabCompleter(new VillageCompletor(this.getGame()));

        PluginManager manager = this.getServer().getPluginManager();
        manager.registerEvents(new PlayerLeave(this.game, this.editor), this);

        checkVersion();
    }

    @Override
    public void onDisable() {
        if (this.game.isGameInProgress()) {
            this.game.end(null);
        }

        if (this.game.getMeetingButton() != null) {
            this.game.getMeetingButton().remove();
        }
    }
}