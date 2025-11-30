package ca.ckay9.Game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import ca.ckay9.Editor.Editor;

public class PlayerLeave implements Listener {
    private Game game;
    private Editor editor;

    public PlayerLeave(Game game, Editor editor) {
        this.game = game;
        this.editor = editor;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (!this.game.isGameInProgress()) {
            return;
        }

        Player player = event.getPlayer();
        this.editor.removeEditor(player.getUniqueId());

        if (this.game.isPlayerVillager(player)) {
            for (VillagerTask task : this.game.getVillagerTasks()) {
                task.unassignPlayer(player.getUniqueId());
            }
        } else {
            for (Vent vent : this.game.getMobVents()) {
                vent.removeMobInside(player.getUniqueId());
            }
        }
    }
}
