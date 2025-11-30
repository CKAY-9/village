package ca.ckay9.Editor;

import java.util.HashMap;
import java.util.UUID;

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

public class VentEditor implements Listener {
    private Editor editor;
    private Game game;
    private HashMap<UUID, Vent> ventLinks;

    public VentEditor(Editor editor, Game game) {
        this.editor = editor;
        this.game = game;
        this.ventLinks = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        EditorState state = this.editor.getEditorStates().get(player.getUniqueId());
        if (state == null || state != EditorState.VENT) {
            return;
        }

        PlayerInventory inv = player.getInventory();
        Material currentMat = inv.getItemInMainHand().getType();
        if (currentMat != Material.IRON_TRAPDOOR) {
            event.setCancelled(true);
            return;
        }

        Block ventBlock = event.getBlock();
        Vent newVent = new Vent(ventBlock);
        this.game.addMobVent(newVent);

        Location location = ventBlock.getLocation();
        Utils.verbosePlayerLog(player, "Created new vent at position " + location.getBlockX() + ", "
                + location.getBlockY() + ", " + location.getBlockZ());
        player.sendMessage(Utils
                .formatText("&a&l[Village]&r&a Created new vent! Use can now use the link and destroy tool on it."));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Vent vent = game.getVentAtLocation(block.getLocation());
        if (vent == null) {
            return;
        }

        Player player = event.getPlayer();
        EditorState state = this.editor.getEditorStates().get(player.getUniqueId());
        if (state == null || state != EditorState.VENT) {
            event.setCancelled(true);
            return;
        }

        for (Vent linkedVent : vent.getConnectedVents()) {
            linkedVent.removeConnectedVent(vent);
        }

        this.game.removeMobVent(vent);

        Location location = block.getLocation();
        Utils.verbosePlayerLog(player, "Removed vent at position " + location.getBlockX() + ", " + location.getBlockY()
                + ", " + location.getBlockZ());
        player.sendMessage(Utils
                .formatText("&a&l[Village]&r&a Removed vent! All connected vents have been delinked."));
    }

    /**
     * Attempts to initialize or complete a link.
     * If the player has already started a link, it will link the selected vents
     * together.
     * If they haven't, it will start a new link.
     * 
     * @param player The player to create/complete the link
     * @param block  The target block. If it's not a vent, this function will just
     *               return.
     */
    private void handleLink(Player player, Block block) {
        Vent existingLinkAttempt = this.ventLinks.get(player.getUniqueId());
        Vent target = this.game.getVentAtLocation(block.getLocation());
        if (target == null) {
            return;
        }

        if (existingLinkAttempt != null) {
            target.addConnectedVent(existingLinkAttempt);
            existingLinkAttempt.addConnectedVent(target);
            this.ventLinks.remove(player.getUniqueId());
            Utils.verbosePlayerLog(player, "Linked two vents together.");
            player.sendMessage(Utils.formatText("&a&l[Village]&r&a Linked vents!"));
        } else {
            // start
            this.ventLinks.put(player.getUniqueId(), target);
            Utils.verbosePlayerLog(player, "Initialized vent link.");
            player.sendMessage(Utils
                    .formatText("&a&l[Village]&r&a Initialized link! Right click another vent with the tool to link."));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockInteraction(PlayerInteractEvent event) {
        // check if right click
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // is in editor
        Player player = event.getPlayer();
        EditorState state = this.editor.getEditorStates().get(player.getUniqueId());
        if (state == null || state != EditorState.VENT) {
            return;
        }

        PlayerInventory inv = player.getInventory();
        Material currentMat = inv.getItemInMainHand().getType();

        // exit editor
        if (currentMat == Material.BARRIER) {
            this.editor.exitEditor(player);
            this.ventLinks.remove(player.getUniqueId());
            event.setCancelled(true);
            return;
        }

        // check if it's actually a vent
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock.getType() != Material.IRON_TRAPDOOR) {
            return;
        }

        // link vents
        if (currentMat == Material.TRIPWIRE_HOOK) {
            event.setCancelled(true);
            handleLink(player, clickedBlock);
        }
    }
}
