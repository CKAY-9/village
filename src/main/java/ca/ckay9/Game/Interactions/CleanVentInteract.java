package ca.ckay9.Game.Interactions;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Villagers.VillagerTask;

public class CleanVentInteract implements Listener {
    private Game game;

    public CleanVentInteract(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!this.game.isGameInProgress() || !this.game.isPlayerVillager(player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.contains(Utils.formatText("&e&lCLEAN"))) {
            return;
        }

        Block lookingAt = player.getTargetBlockExact(10);
        if (lookingAt == null || lookingAt.getType() != Material.IRON_TRAPDOOR) {
            return;
        }

        VillagerTask task = this.game.getTaskAtLocation(lookingAt.getLocation());
        if (task == null || !task.assignedToThis(player.getUniqueId())) {
            return;
        }

        Utils.verboseLog("Found clean vent task.");

        ArrayList<Integer> positions = task.getCleanVentPositionsForPlayer(player.getUniqueId());
        if (positions == null) {
            return;
        }

        Utils.verboseLog("Clean positions found.");

        event.setCancelled(true);
        int slot = event.getSlot();
        ItemStack clickedItem = event.getClickedInventory().getItem(slot);
        if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getAmount() <= 0) {
            return;
        }

        if (positions.contains(slot)) {
            clickedItem.setAmount(0);
            task.addCleanVentItemPositions(player.getUniqueId(), positions);

            Random random = new Random();
            player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_STEP, 5, (random.nextFloat() * 0.25f) + 1f);
        }

        if (event.getClickedInventory().isEmpty()) {
            // complete
            task.removeCleanVentPositionsForPlayer(player.getUniqueId());
            task.completeTask(player, game);
            player.closeInventory();
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!this.game.isGameInProgress() || !this.game.isPlayerVillager(player)) {
            return;
        }

        VillagerTask task = this.game.getCleanVentTaskByPlayer(player);
        if (task == null) {
            return;
        }

        task.removeCleanVentPositionsForPlayer(player.getUniqueId());
    }
}
