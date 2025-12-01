package ca.ckay9.Game.Interactions;

import org.bukkit.ChatColor;
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
import org.bukkit.inventory.meta.ItemMeta;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Villagers.VillagerTask;

public class ManifoldTaskInteract implements Listener {
    private Game game;

    public ManifoldTaskInteract(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!this.game.isGameInProgress() || !this.game.isPlayerVillager(player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.contains(Utils.formatText("&6&lMANIFOLD"))) {
            return;
        }

        Block lookingAt = player.getTargetBlockExact(10);
        if (lookingAt == null || lookingAt.getType() != Material.DISPENSER) {
            return;
        }

        VillagerTask task = this.game.getTaskAtLocation(lookingAt.getLocation());
        if (task == null || !task.assignedToThis(player.getUniqueId())) {
            return;
        }

        Utils.verboseLog("Found manifold task.");

        Integer next = this.game.getManifoldTaskExpectedNexts().get(player.getUniqueId());
        if (next == null) {
            next = 1;
        }

        Utils.verboseLog("Manifold task next expected found.");

        event.setCancelled(true);
        int slot = event.getSlot();
        ItemStack clickedItem = event.getClickedInventory().getItem(slot);
        if (clickedItem == null || clickedItem.getType() != Material.LIGHT) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        String name = ChatColor.stripColor(meta.getDisplayName());
        int clickedValue = Integer.parseInt(name);
        Utils.verboseLog("Interacted with light.\n  -> Expected = " + next + "\n  -> Got = " + clickedValue);
        if (clickedValue != next) {
            // invalid
            this.game.removeManifoldTaskExpectedNext(player.getUniqueId());
            task.failTask(player, game);
            player.closeInventory();
            return;
        }

        if (clickedValue == 9) {
            // complete
            this.game.addManifoldTaskExpectedNext(player.getUniqueId(), 10);
            task.completeTask(player, game);
            player.closeInventory();
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 5, 1 + (float)(next * 0.05));
        clickedItem.setType(Material.GREEN_STAINED_GLASS_PANE);
        meta.setDisplayName(Utils.formatText("&a&lCORRECT"));
        clickedItem.setItemMeta(meta);
        this.game.addManifoldTaskExpectedNext(player.getUniqueId(), next + 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!this.game.isGameInProgress() || !this.game.isPlayerVillager(player)) {
            return;
        }

        Integer next = this.game.getManifoldTaskExpectedNexts().get(player.getUniqueId());
        if (next == null || next < 10) {
            this.game.removeManifoldTaskExpectedNext(player.getUniqueId());
            return;
        }
    }
}
