package ca.ckay9.Game;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import ca.ckay9.Utils;

public class PlayerDamage implements Listener {
    private Game game;

    public PlayerDamage(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
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
        boolean villagerTriedToKill = this.game.isPlayerVillager(damager);
        boolean onCooldown = existingCooldown != null && existingCooldown > 0;
        boolean mobTriedToKillMob = !this.game.isPlayerVillager(damaged);
        if (villagerTriedToKill || onCooldown || mobTriedToKillMob) {
            return;
        }

        // Mob
        Location damagedLocation = damaged.getLocation().add(0, 1, 0);
        ArmorStand armorStand = (ArmorStand) damaged.getWorld().spawnEntity(damagedLocation, EntityType.ARMOR_STAND);
        armorStand.setInvulnerable(true);
        armorStand.setCustomName(Utils.formatText("&c&lBODY&r | &c&l" + damaged.getName()));
        armorStand.setCustomNameVisible(true);

        EntityEquipment equipment = armorStand.getEquipment();
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwnerProfile(damaged.getPlayerProfile());
        head.setItemMeta(headMeta);

        equipment.setHelmet(head);
        equipment.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        equipment.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        equipment.setBoots(new ItemStack(Material.LEATHER_BOOTS));

        damaged.setGameMode(GameMode.SPECTATOR);
        damaged.sendTitle(Utils.formatText("&c&lKILLED"),
                Utils.formatText("You have been killed by &c&l" + damager.getName()), 20, 80, 20);

        this.game.addKillCooldown(damager.getUniqueId(), this.game.getKillCooldown());
        this.game.checkWinCondition();
    }
}
