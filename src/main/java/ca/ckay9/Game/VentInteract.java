package ca.ckay9.Game;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ca.ckay9.Utils;
import ca.ckay9.Village;

public class VentInteract implements Listener {
    private Game game;
    private int EXIT_VENT_SLOT = 18;

    public VentInteract(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!this.game.isGameInProgress() && !Village.inDeveloperDebug()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (this.game.isPlayerVillager(player) && !Village.inDeveloperDebug()) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.contains(Utils.formatText("&c&lVENT SYSTEM"))) {
            return;
        }

        event.setCancelled(true);
        Vent currentVent = this.game.getVentMobIsIn(player);
        if (currentVent == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getSlot();
        if (currentVent.getConnectedVents().size() <= slot) {
            return;
        }

        // exit vent
        if (slot == EXIT_VENT_SLOT) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType() == PotionEffectType.INVISIBILITY) {
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    break;
                }
            }

            currentVent.removeMobInside(player.getUniqueId());
            player.closeInventory();
            return;
        }

        Vent nextVent = currentVent.getConnectedVents().get(slot);
        if (nextVent == null) {
            return;
        }

        currentVent.removeMobInside(player.getUniqueId());

        player.teleport(nextVent.getBlock().getLocation());
        nextVent.addMobInside(player.getUniqueId());
        nextVent.openVentConnectionsMenu(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!this.game.isGameInProgress() && !Village.inDeveloperDebug()) {
            return;
        }

        Player player = (Player) event.getPlayer();
        if (this.game.isPlayerVillager(player) && !Village.inDeveloperDebug()) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.contains(Utils.formatText("&c&lVENT SYSTEM"))) {
            return;
        }

        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType() == PotionEffectType.INVISIBILITY) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                break;
            }
        }
        player.playSound(player.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 5, 0);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (!this.game.isGameInProgress() && !Village.inDeveloperDebug()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (this.game.isPlayerVillager(player)) {
            return;
        }

        Block block = event.getClickedBlock();
        Vent vent = game.getVentAtLocation(block.getLocation());
        if (vent == null) {
            return;
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10_000_000, 255, false, false));

        vent.addMobInside(player.getUniqueId());
        vent.openVentConnectionsMenu(player);

        event.setCancelled(true);
    }
}
