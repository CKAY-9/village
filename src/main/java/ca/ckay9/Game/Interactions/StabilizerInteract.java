package ca.ckay9.Game.Interactions;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Mobs.Sabotage;
import ca.ckay9.Game.Mobs.StabilizerProgress;

public class StabilizerInteract implements Listener {
    private Game game;

    public StabilizerInteract(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onStabilizerSolveClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!this.game.isGameInProgress()) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.contains(Utils.formatText("&e&lSTABILIZER"))) {
            return;
        }

        event.setCancelled(true);
        Block lookingAt = player.getTargetBlockExact(10);
        if (lookingAt == null) {
            return;
        }

        Sabotage sabotage = this.game.getSabotageByStructure(lookingAt);
        if (sabotage == null || !sabotage.isActive()) {
            return;
        }

        StabilizerProgress progress = sabotage.getStabilizerProgressForPlayer(player.getUniqueId());
        if (progress == null) {
            // this should be initialized on open
            return;
        }

        int clickedSlot = event.getSlot();
        ItemStack clickedItem = event.getClickedInventory().getItem(clickedSlot);
        if (clickedItem == null) {
            return;
        }

        Material clickedType = clickedItem.getType();
        if (clickedType != Material.RED_STAINED_GLASS_PANE) {
            // incorrect type
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 3, 1f);
            return;
        } else {
            clickedItem.setAmount(clickedItem.getAmount() - 1);
            progress.setInventory(event.getClickedInventory());
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 3,
                1f + (clickedItem.getAmount() * 0.075f));
        for (ItemStack stack : progress.getInventory().getContents()) {
            if (stack != null && stack.getType() == Material.RED_STAINED_GLASS_PANE && stack.getAmount() > 0) {
                return;
            }
        }

        progress.setCompleted(true);
        player.closeInventory();

        if (sabotage.solvedStabilizer()) {
            sabotage.deactivate(game);
        } else {
            player.sendMessage(Utils.formatText("&a&l[SABOTAGE]&r&a Waiting for secondary confirmation..."));
        }
    }
}
