package ca.ckay9.Game.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import ca.ckay9.Game.Game;
import ca.ckay9.Game.Role;
import ca.ckay9.Game.Status;

public class PlayerDamage implements Listener {
    private Game game;

    public PlayerDamage(Game game) {
        this.game = game;
    }

    /**
     * prevent players from dying to sufficating or whatever
     * @param event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!this.game.isGameInProgress()) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        event.setCancelled(true);
        event.setDamage(0);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageByPlayer(EntityDamageByEntityEvent event) {
        if (!this.game.isGameInProgress()) {
            return;
        }

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        event.setCancelled(true);
        event.setDamage(0);

        if (this.game.getGameStatus() != Status.PLAYING) {
            return;
        }

        Player damager = (Player) event.getDamager();
        Player damaged = (Player) event.getEntity();

        Long existingCooldown = this.game.getKillCooldowns().get(damager.getUniqueId());
        Role damagerRole = this.game.getPlayerRole(damager.getUniqueId());
        boolean villagerTriedToKill = this.game.isPlayerVillager(damager) && damagerRole != Role.DETECTIVE;
        boolean onCooldown = existingCooldown != null && existingCooldown > 0;
        boolean mobTriedToKillMob = !this.game.isPlayerVillager(damaged) && damagerRole != Role.DETECTIVE;
        if (villagerTriedToKill || onCooldown || mobTriedToKillMob) {
            return;
        }

        this.game.killPlayer(damaged, false, true, damager);
    }
}
