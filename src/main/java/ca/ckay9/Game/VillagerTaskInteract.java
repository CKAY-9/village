package ca.ckay9.Game;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class VillagerTaskInteract implements Listener {
    private Game game;

    public VillagerTaskInteract(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (!this.game.isGameInProgress()) {
            //return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (!this.game.isPlayerVillager(player)) {
            // return;
        }

        Block block = event.getClickedBlock();
        VillagerTask task = game.getTaskAtLocation(block.getLocation());
        if (task == null) {
            return;
        }

        if (!task.assignedToThis(player.getUniqueId())) {
            //return;
        }

        task.startTask(player, this.game);

        // players need to be able to open the crafting table after interacting with it.
        boolean isCrafting = task.getTaskType() == VillagerTaskType.CRAFT || (task.getTaskType() == VillagerTaskType.CUSTOM && block.getType() == Material.CRAFTING_TABLE);
        boolean hasInteracted = this.game.getCraftTaskExpectedResults().containsKey(player.getUniqueId());
        if (isCrafting) {
            if (hasInteracted) {
                event.setCancelled(false);
            } else {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!this.game.isGameInProgress()) {
            //return;
        }

        Player player = event.getPlayer();
        if (!this.game.isPlayerVillager(player)) {
            // return;
        }
        
        String message = event.getMessage().toLowerCase().strip();
        ChatTaskProgress taskProgress = this.game.getChatTaskExpectedResults().get(player.getUniqueId());
        if (taskProgress == null || !taskProgress.getTask().assignedToThis(player.getUniqueId())) {
            // return;
        }

        if (message.equals(taskProgress.getAnswer())) {
            taskProgress.getTask().completeTask(player);
        } else {
            taskProgress.getTask().failTask(player);
        }

        event.setMessage("");
        event.setCancelled(true);
    }
}
