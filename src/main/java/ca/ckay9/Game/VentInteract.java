package ca.ckay9.Game;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import ca.ckay9.Village;

public class VentInteract implements Listener {
    private Game game;

    public VentInteract(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!this.game.isGameInProgress() && !Village.inDeveloperDebug()) {
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (!this.game.isGameInProgress() && !Village.inDeveloperDebug()) {
            return;
        }
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (this.game.isPlayerVillager(player) && !Village.inDeveloperDebug()) {
            return;
        }

        Block block = event.getClickedBlock();
        Vent vent = game.getVentAtLocation(block.getLocation());
        if (vent == null) {
            return;
        }

        vent.openVentConnectionsMenu(player);
        event.setCancelled(true);
    }
}
