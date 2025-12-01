package ca.ckay9.Game.Listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import ca.ckay9.Game.Game;
import ca.ckay9.Game.Villagers.UploadPart;

public class PlayerMove implements Listener {
    private Game game;

    public PlayerMove(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void uploadTaskMovement(PlayerMoveEvent event) {
        if (!this.game.isGameInProgress()) {
            return;
        }

        Player player = event.getPlayer();
        UploadPart part = this.game.getUploadParts().get(player.getUniqueId());
        if (part == null || part == UploadPart.COPIED || part == UploadPart.UPLOADED) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null)
            return;

        float yaw = to.getYaw();
        float pitch = to.getPitch();

        event.setTo(new Location(
                from.getWorld(),
                from.getX(),
                from.getY(),
                from.getZ(),
                yaw,
                pitch));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!this.game.isGameInProgress() || !this.game.inDiscussion()) {
            return;
        }

        Player player = event.getPlayer();
        if (this.game.isPlayerDead(player)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null)
            return;

        float yaw = to.getYaw();
        float pitch = to.getPitch();

        event.setTo(new Location(
                from.getWorld(),
                from.getX(),
                from.getY(),
                from.getZ(),
                yaw,
                pitch));
    }
}
