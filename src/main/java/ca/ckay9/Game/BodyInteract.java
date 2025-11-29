package ca.ckay9.Game;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import ca.ckay9.Utils;

public class BodyInteract implements Listener {
    private Game game;

    public BodyInteract(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteraction(PlayerInteractAtEntityEvent event) {
        if (!this.game.isGameInProgress()) {
            return;
        }

        if (!(event.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        Player player = event.getPlayer();
        if (this.game.isPlayerDead(player)) {
            return;
        }

        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        if (!armorStand.getCustomName().contains(Utils.formatText("&c&lBODY&r | "))) {
            return;
        }

        event.setCancelled(true);

        // TODO: check if using ability

        // clear body and start meeting
        armorStand.teleport(new Location(armorStand.getLocation().getWorld(), 0, 0, 0));
        armorStand.setInvulnerable(false);
        armorStand.setInvisible(true);
        armorStand.setHealth(0);
        this.game.startDiscussion(player, "REPORT: " + armorStand.getCustomName());
    }
}
