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
import ca.ckay9.Game.Mobs.ReactorProgress;
import ca.ckay9.Game.Mobs.Sabotage;

public class ReactorInteract implements Listener {
    private Game game;

    public ReactorInteract(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onReactorSolveClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!this.game.isGameInProgress()) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.contains(Utils.formatText("&e&lREACTOR"))) {
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

        ReactorProgress progress = sabotage.getReactorProgressForPlayer(player.getUniqueId());
        if (progress == null) {
            // this should be initialized on open
            return;
        }

        int clickedSlot = event.getSlot();
        if (clickedSlot == 13) {
            return;
        }

        ItemStack clickedItem = event.getClickedInventory().getItem(clickedSlot);
        if (clickedItem == null) {
            return;
        }

        Material clickedType = clickedItem.getType();
        if (!clickedType.equals(progress.getMaterial())) {
            // incorrect type
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 3, 1f);
            return;
        }

        int completed = progress.getCompleted() + 1;
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 3, 1f + (completed * 0.3f));
        if (completed >= 5) {
            // finished
            sabotage.addReactorProgress(player.getUniqueId(), null);
            sabotage.deactivate(this.game);
            player.closeInventory();
        } else {
            progress.setCompleted(completed);
            sabotage.addReactorProgress(player.getUniqueId(), progress);
            
            sabotage.solve(player);
        }
    }
}
