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

        event.setCancelled(true);

        if (!task.assignedToThis(player.getUniqueId())) {
            player.sendMessage(Utils.formatText("&c&l[TASK]&r&c You aren't assigned to this task."));
            return;
        }

        if (task.getTaskType() == VillagerTaskType.UPLOAD) {
            // keeps track of state with this
            task.startTask(player, this.game);
            return;
        }

        boolean isCrafting = task.getTaskType() == VillagerTaskType.CRAFT
                || (task.getTaskType() == VillagerTaskType.CUSTOM && block.getType() == Material.CRAFTING_TABLE);
        boolean interactedWithCraftingTable = this.game.getCraftTaskExpectedResults().containsKey(player.getUniqueId());
        boolean interactedWithOtherTask = this.game.getChatTaskExpectedResults().containsKey(player.getUniqueId());

        // players need to be able to open the crafting table after interacting with it.
        if (isCrafting) {
            if (interactedWithCraftingTable) {
                event.setCancelled(false);
                return;
            } else {
                task.startTask(player, this.game);
            }
        } else if (!interactedWithOtherTask) {
            task.startTask(player, this.game);
        }
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
            progress.getTask().completeTask(player, this.game);
        } else {
            progress.getTask().failTask(player, this.game);
        }

        this.game.getCraftTaskExpectedResults().remove(player.getUniqueId());
        player.closeInventory();
        game.clearCraftingMaterials(player);
        this.game.clearCraftingMaterials(player);
        event.setCancelled(true);
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
            game.removeChatTaskExpectedResult(player.getUniqueId());
            taskProgress.getTask().completeTask(player, this.game);
        } else {
            taskProgress.getTask().failTask(player, this.game);
        }

        event.setMessage("");
        event.setCancelled(true);
    }
}
