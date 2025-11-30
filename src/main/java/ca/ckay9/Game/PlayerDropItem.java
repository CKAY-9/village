package ca.ckay9.Game;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItem implements Listener {
    private Game game;
    
    public PlayerDropItem(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!this.game.isGameInProgress()) {
            return;
        }

        // prevent any items dropping
        event.setCancelled(true);
    }
}
