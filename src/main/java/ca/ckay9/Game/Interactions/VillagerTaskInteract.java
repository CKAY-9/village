package ca.ckay9.Game.Interactions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import ca.ckay9.Utils;
import ca.ckay9.Village;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Villagers.ChatTaskProgress;
import ca.ckay9.Game.Villagers.CraftTaskProgress;
import ca.ckay9.Game.Villagers.VillagerTask;
import ca.ckay9.Game.Villagers.VillagerTaskType;

public class VillagerTaskInteract implements Listener {
    private Game game;

    public VillagerTaskInteract(Game game) {
        this.game = game;
    }

    /**
     * This handles starting a task and making sure you interact properly with it.
     * For example, you should only interact with a crafting table once you know
     * what to make, etc.
     * 
     * @param event Default event passed
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (!this.game.isGameInProgress() && !Village.inDeveloperDebug()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (!this.game.isPlayerVillager(player) && !Village.inDeveloperDebug()) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getClickedBlock();
        VillagerTask task = game.getTaskAtLocation(block.getLocation());
        if (task == null) {
            return;
        }

        if (!task.assignedToThis(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        // players need to be able to open the crafting table after interacting with it.
        boolean isCrafting = task.getTaskType() == VillagerTaskType.CRAFT
                || (task.getTaskType() == VillagerTaskType.CUSTOM && block.getType() == Material.CRAFTING_TABLE);
        boolean hasInteracted = this.game.getCraftTaskExpectedResults().containsKey(player.getUniqueId());
        if (isCrafting) {
            if (hasInteracted) {
                event.setCancelled(false);
                return;
            } else {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }

        ChatTaskProgress progress = this.game.getChatTaskExpectedResults().get(player.getUniqueId());
        boolean previousInteractionWasWithThisTask = progress != null
                && task.getBlock().getLocation().equals(progress.getTask().getBlock().getLocation());
        if (previousInteractionWasWithThisTask) {
            return;
        }

        task.startTask(player, this.game);
    }

    /**
     * Handles the crafting task.
     * 
     * @param event Default event passed
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraft(CraftItemEvent event) {
        if (!this.game.isGameInProgress() && !Village.inDeveloperDebug()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (!this.game.isPlayerVillager(player) && !Village.inDeveloperDebug()) {
            return;
        }

        CraftTaskProgress progress = this.game.getCraftTaskExpectedResults().get(player.getUniqueId());
        if (progress == null) {
            return;
        }

        Material receivedMaterial = event.getCurrentItem().getType();
        Utils.verbosePlayerLog(player,
                "Attempted to complete craft task.\n  -> Expected = " + progress.getMaterial().name()
                        + "\n  -> Received = " + receivedMaterial.name());

        if (receivedMaterial.equals(progress.getMaterial())) {
            this.game.getCraftTaskExpectedResults().remove(player.getUniqueId());
            progress.getTask().completeTask(player);
        } else {
            progress.getTask().failTask(player);
        }

        player.closeInventory();
        event.setCancelled(true);

        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) {
                continue;
            }

            /**
             * compasses are given after task completion
             * wooden swords and clocks are used by detectives
             * golden carrots are used by medics
             */
            if (stack.getType() != Material.COMPASS &&
                    stack.getType() != Material.NETHERITE_SWORD &&
                    stack.getType() != Material.CLOCK &&
                    stack.getType() != Material.NETHERITE_HOE &&
                    stack.getType() != Material.GOLDEN_CARROT) {
                stack.setAmount(0);
            }
        }
    }

    /**
     * This handles trivia and math questions since they need to be typed out. Could
     * have a multiple choice menu but this is fine for now
     * 
     * @param event The default event passed
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!this.game.isGameInProgress() && !Village.inDeveloperDebug()) {
            return;
        }

        // allow players to type in chat
        if (this.game.inDiscussion() || this.game.ableToVote()) {
            return;
        }

        Player player = event.getPlayer();
        if (!this.game.isPlayerVillager(player) && !Village.inDeveloperDebug()) {
            return;
        }

        String message = event.getMessage().toLowerCase().strip();
        ChatTaskProgress taskProgress = this.game.getChatTaskExpectedResults().get(player.getUniqueId());
        if (taskProgress == null
                || !taskProgress.getTask().assignedToThis(player.getUniqueId()) || !Village.inDeveloperDebug()) {
            return;
        }

        Utils.verbosePlayerLog(player, "Attempted to answer chat task.\n  -> Expected = " + taskProgress.getAnswer()
                + "\n  -> Received = " + message);

        if (message.equals(taskProgress.getAnswer())) {
            taskProgress.getTask().completeTask(player);
        } else {
            taskProgress.getTask().failTask(player);
        }

        event.setMessage("");
        event.setCancelled(true);
    }
}
