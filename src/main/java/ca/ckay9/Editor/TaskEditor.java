package ca.ckay9.Editor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Villagers.VillagerTask;
import ca.ckay9.Game.Villagers.VillagerTaskType;

public class TaskEditor implements Listener {
    private Editor editor;
    private Game game;

    public TaskEditor(Editor editor, Game game) {
        this.editor = editor;
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        VillagerTask task = game.getTaskAtLocation(block.getLocation());
        if (task == null) {
            return;
        }

        Player player = event.getPlayer();
        EditorState state = this.editor.getEditorStates().get(player.getUniqueId());
        if (state == null || state != EditorState.TASK) {
            event.setCancelled(true);
            return;
        }

        this.game.removeVillagerTask(task);
        Location location = block.getLocation();
        Utils.verbosePlayerLog(player, "Removed task at position " + location.getBlockX() + ", " + location.getBlockY()
                + ", " + location.getBlockZ());
        player.sendMessage(Utils
                .formatText("&a&l[Village]&r&a Removed task."));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        // is in editor
        Player player = event.getPlayer();
        EditorState state = this.editor.getEditorStates().get(player.getUniqueId());
        if (state == null || state != EditorState.TASK) {
            return;
        }

        Block block = event.getBlock();
        Material blockMat = block.getType();
        // exit editor
        if (blockMat == Material.BARRIER) {
            this.editor.exitEditor(player);
            event.setCancelled(true);
            return;
        }

        if (blockMat == Material.OBSERVER && this.game.uploadTaskCreated()) {
            event.setCancelled(true);
            Utils.verbosePlayerLog(player, "Tried to create upload task twice");
            player.sendMessage(Utils
                    .formatText("&a&l[Village]&r&a Two upload task blocks already exist. Break one to move."));
            return;
        }

        VillagerTask task = new VillagerTask(block);
        if (blockMat == Material.SMITHING_TABLE) {
            // math task
            task.setTaskType(VillagerTaskType.MATH);
        } else if (blockMat == Material.CRAFTING_TABLE) {
            // craft task
            task.setTaskType(VillagerTaskType.CRAFT);
        } else if (blockMat == Material.LECTERN) {
            // trivia task
            task.setTaskType(VillagerTaskType.TRIVIA);
        } else if (blockMat == Material.ENCHANTING_TABLE) {
            // custom task
            task.setTaskType(VillagerTaskType.CUSTOM);
        } else if (blockMat == Material.OBSERVER) {
            // uplaod task
            task.setTaskType(VillagerTaskType.UPLOAD);
        } else {
            event.setCancelled(true);
            return;
        }

        this.game.addVillagerTask(task);
        Location location = block.getLocation();
        Utils.verbosePlayerLog(player, "Created new task at position " + location.getBlockX() + ", "
                + location.getBlockY() + ", " + location.getBlockZ());
        player.sendMessage(Utils
                .formatText("&a&l[Village]&r&a Created new task! Villagers will now be able to complete this"));
    }
}
