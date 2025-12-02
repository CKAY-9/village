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
import org.bukkit.inventory.PlayerInventory;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Mobs.Sabotage;
import ca.ckay9.Game.Mobs.SabotageType;

public class SabotageEditor implements Listener {
    private Editor editor;
    private Game game;

    public SabotageEditor(Editor editor, Game game) {
        this.editor = editor;
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void placeSabotage(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        EditorState state = this.editor.getEditorStates().get(player.getUniqueId());
        if (state == null || state != EditorState.SABOTAGE) {
            return;
        }

        PlayerInventory inv = player.getInventory();
        Material currentMat = inv.getItemInMainHand().getType();

        if (currentMat != Material.REDSTONE_LAMP && currentMat != Material.TNT) {
            if (currentMat == Material.BARRIER) {
                this.editor.exitEditor(player);
            }
            event.setCancelled(true);
            return;
        }

        Block sabotageBlock = event.getBlock();

        SabotageType type = SabotageType.REACTOR;
        Sabotage sabotage;
        if (sabotageBlock.getType() == Material.REDSTONE_LAMP) {
            type = SabotageType.STABILIZER;
            sabotage = this.game.getSabotageByType(type);
        } else {
            type = SabotageType.REACTOR;
            sabotage = this.game.getSabotageByType(type);
        }

        if (sabotage != null) {
            event.setCancelled(true);
            Utils.verbosePlayerLog(player, "Sabotage already exists.");
            player.sendMessage(Utils
                    .formatText("&a&l[VILLAGE]&r&a Sabotage already exists!"));
            return;
        }

        Location location = sabotageBlock.getLocation();
        sabotage = new Sabotage(sabotageBlock);
        sabotage.setSabotageType(type);
        if (!sabotage.canBuild()) {
            Utils.verbosePlayerLog(player, "Can't build sabotage at " + location.getBlockX() + ", "
                    + location.getBlockY() + ", " + location.getBlockZ());
            player.sendMessage(Utils
                    .formatText("&a&l[VILLAGE]&r&a Can't build sabotage here!"));
            event.setCancelled(true);
            return;
        }

        sabotage.build();
        this.game.addSabotage(sabotage);
        Utils.verbosePlayerLog(player, "Created new sabotage at position " + location.getBlockX() + ", "
                + location.getBlockY() + ", " + location.getBlockZ());
        player.sendMessage(Utils
                .formatText("&a&l[VILLAGE]&r&a Created new sabotage!"));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void breakSabotage(BlockBreakEvent event) {
        Block block = event.getBlock();
        Sabotage sabotage = game.getSabotageByStructure(block);
        if (sabotage == null) {
            return;
        }

        Player player = event.getPlayer();
        EditorState state = this.editor.getEditorStates().get(player.getUniqueId());
        if (state == null || state != EditorState.SABOTAGE) {
            event.setCancelled(true);
            return;
        }

        sabotage.destroy();
        this.game.removeSabotage(sabotage);
        Location location = block.getLocation();
        Utils.verbosePlayerLog(player,
                "Removed sabotage at position " + location.getBlockX() + ", " + location.getBlockY()
                        + ", " + location.getBlockZ());
        player.sendMessage(Utils
                .formatText("&a&l[VILLAGE]&r&a Removed sabotage."));
    }
}
