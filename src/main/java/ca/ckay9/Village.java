package ca.ckay9;

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
    }

    @Override
    public void onDisable() {
        if (this.game.isGameInProgress()) {
            this.game.end();
        }

        if (this.game.getMeetingButton() != null) {
            this.game.getMeetingButton().remove();
        }
    }
}