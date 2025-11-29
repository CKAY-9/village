package ca.ckay9.Game;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMove implements Listener {
    private Game game;

    public PlayerMove(Game game) {
        this.game = game;
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

        player.setAllowFlight(true);
        player.setFlying(true);

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
