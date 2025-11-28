package ca.ckay9;

import org.bukkit.plugin.java.JavaPlugin;

import ca.ckay9.Commands.VillageCommand;
import ca.ckay9.Commands.VillageCompletor;
import ca.ckay9.Editor.Editor;
import ca.ckay9.Game.Game;

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

    @Override
    public void onEnable() {
        this.game = new Game(this);
        this.editor = new Editor(this);

        this.getCommand("village").setExecutor(new VillageCommand(this));
        this.getCommand("village").setTabCompleter(new VillageCompletor(this.getGame()));
    }

    @Override
    public void onDisable() {

    }
}