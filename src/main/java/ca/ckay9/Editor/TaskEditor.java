package ca.ckay9.Editor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Mobs.Vent;
import ca.ckay9.Game.Villagers.VillagerTask;
import ca.ckay9.Game.Villagers.VillagerTaskType;

public class TaskEditor implements Listener {
    private Editor editor;
    private Game game;

    public TaskEditor(Editor editor, Game game) {
        this.editor = editor;
        this.game = game;
    }

    // used for setting vent to possible vent clean task
    @EventHandler(priority = EventPriority.LOWEST)
    public void setVentToCleanVentTask(PlayerInteractEvent event) {
        // check if right click
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // is in editor
        Player player = event.getPlayer();
        EditorState state = this.editor.getEditorStates().get(player.getUniqueId());
        if (state == null || state != EditorState.TASK) {
            return;
        }

        PlayerInventory inv = player.getInventory();
        Material currentItemType = inv.getItemInMainHand().getType();
        if (currentItemType != Material.DRIED_KELP) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block.getType() != Material.IRON_TRAPDOOR) {
            player.sendMessage(Utils.formatText("&c&l[VILLAGE]&r&c Not looking at vent."));
            return;
        }

        Location location = block.getLocation();
        Vent vent = this.game.getVentAtLocation(location);
        if (vent == null) {
            player.sendMessage(Utils.formatText("&c&l[VILLAGE]&r&c Not looking at vent."));
            return;
        }

        VillagerTask task = this.game.getTaskAtLocation(location);
        if (task != null) {
            // remove clean task
            task.destroy();
            this.game.removeVillagerTask(task);
            Utils.verbosePlayerLog(player, "Removed new clean vent task at position " + location.getBlockX() + ", "
                    + location.getBlockY() + ", " + location.getBlockZ());
            player.sendMessage(Utils
                    .formatText(
                            "&a&l[VILLAGE]&r&a Removed clean vent task! Right click to enable."));
        } else {
            task = new VillagerTask(block);
            task.setTaskType(VillagerTaskType.CLEAN_VENT);
            this.game.addVillagerTask(task);
            Utils.verbosePlayerLog(player, "Created new clean vent task at position " + location.getBlockX() + ", "
                    + location.getBlockY() + ", " + location.getBlockZ());
            player.sendMessage(Utils
                    .formatText(
                            "&a&l[VILLAGE]&r&a Set vent to clean vent task! Right click to disable."));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void breakTaskEvent(BlockBreakEvent event) {
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

        task.destroy();
        this.game.removeVillagerTask(task);
        Location location = block.getLocation();
        Utils.verbosePlayerLog(player, "Removed task at position " + location.getBlockX() + ", " + location.getBlockY()
                + ", " + location.getBlockZ());
        player.sendMessage(Utils
                .formatText("&a&l[VILLAGE]&r&a Removed task."));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void placeNewTask(BlockPlaceEvent event) {
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

        if (blockMat == Material.ENCHANTING_TABLE) {
            event.setCancelled(true);
            Utils.verbosePlayerLog(player, "Tried to create custom task. Non-implemented");
            player.sendMessage(
                    Utils.formatText("&c&l[VILLAGE] Custom tasks are currently not implemented into Village."));
            return;
        }

        if (blockMat == Material.OBSERVER && this.game.uploadTaskCreated()) {
            event.setCancelled(true);
            Utils.verbosePlayerLog(player, "Tried to create upload task twice");
            player.sendMessage(Utils
                    .formatText("&c&l[VILLAGE]&r&c Two upload task blocks already exist. Break one to move."));
            return;
        }

        if (blockMat == Material.REDSTONE_BLOCK) {
            // check
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    if (i == 0 && j == 0) {
                        continue;
                    }

                    Block b = block.getRelative(i, 0, j);
                    if (b != null && b.getType() != Material.AIR) {
                        Utils.verbosePlayerLog(player, "Invalid medical scan position");
                        event.setCancelled(true);
                        player.sendMessage(Utils
                                .formatText(
                                        "&c&l[VILLAGE]&r&c Invalid medical scan location. Please try somewhere else."));
                        return;
                    }
                }
            }
        }

        VillagerTask task = new VillagerTask(block);
        if (blockMat == Material.SMITHING_TABLE) {
            task.setTaskType(VillagerTaskType.MATH);
        } else if (blockMat == Material.CRAFTING_TABLE) {
            task.setTaskType(VillagerTaskType.CRAFT);
        } else if (blockMat == Material.LECTERN) {
            task.setTaskType(VillagerTaskType.TRIVIA);
        } else if (blockMat == Material.ENCHANTING_TABLE) {
            task.setTaskType(VillagerTaskType.CUSTOM);
        } else if (blockMat == Material.OBSERVER) {
            task.setTaskType(VillagerTaskType.UPLOAD);
        } else if (blockMat == Material.DISPENSER) {
            task.setTaskType(VillagerTaskType.MANIFOLD);
        } else if (blockMat == Material.REDSTONE_BLOCK) {
            task.setTaskType(VillagerTaskType.MEDICAL_SCAN);
        } else {
            event.setCancelled(true);
            return;
        }

        this.game.addVillagerTask(task);
        Location location = block.getLocation();
        Utils.verbosePlayerLog(player, "Created new task at position " + location.getBlockX() + ", "
                + location.getBlockY() + ", " + location.getBlockZ());
        player.sendMessage(Utils
                .formatText("&a&l[VILLAGE]&r&a Created new task! Villagers will now be able to complete this"));
    }
}
