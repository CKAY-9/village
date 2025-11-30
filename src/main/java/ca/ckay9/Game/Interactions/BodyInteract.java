package ca.ckay9.Game.Interactions;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Role;

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
        // Utils.formatText("&c&lBODY&r | &c&l" + damaged.getName())
        String prefix = Utils.formatText("&c&lBODY&r | &c&l");
        String bodyPlayerName = armorStand.getCustomName().substring(prefix.length());
        Player bodyPlayer = Bukkit.getPlayer(bodyPlayerName);

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null) {
            itemInHand = player.getInventory().getItemInOffHand();
        }

        Role role = this.game.getPlayerRole(player.getUniqueId());
        Material mat = itemInHand.getType();
        boolean isValidItem = itemInHand != null && itemInHand.getType() != null;
        boolean isSpecialTool = (mat == Material.NETHERITE_SHOVEL || mat == Material.CLOCK || mat == Material.GOLDEN_CARROT);
        boolean specialRole = (role != Role.VILLAGER || role != Role.MOB);
        if (isValidItem && isSpecialTool && specialRole) {
            if (this.game.canUseAbility(player.getUniqueId())) {
                // abilities that can be used on bodies
                if (role == Role.SWEEPER) {
                    this.game.addAbilityCooldown(player.getUniqueId(), this.game.getAbilityCooldown());
                    armorStand.teleport(new Location(armorStand.getLocation().getWorld(), 0, 0, 0));
                    armorStand.setInvulnerable(false);
                    armorStand.setInvisible(true);
                    armorStand.setHealth(0);
                    return;
                }

                if (role == Role.DETECTIVE && bodyPlayer != null) {
                    this.game.addAbilityCooldown(player.getUniqueId(), this.game.getAbilityCooldown());
                    player.sendMessage(Utils.formatText("&e&l[T.O.D. CLOCK]&r&e Body has been dead for &e&l"
                            + Utils.ticksToSeconds(
                                    this.game.timeSinceDeath(this.game.getTimeOfDeathOfPlayer(bodyPlayer)))
                            + "s"));
                    return;
                }

                if (role == Role.MEDIC && bodyPlayer != null) {
                    this.game.addAbilityCooldown(player.getUniqueId(), this.game.getAbilityCooldown());
                    player.sendMessage(Utils.formatText("&b&l[MAGIC CARROT]&r&b Revived &a&lVillager."));
                    bodyPlayer.teleport(armorStand.getLocation());
                    bodyPlayer.setGameMode(GameMode.ADVENTURE);
                    armorStand.remove();
                    return;
                }
            } else {
                player.sendMessage(Utils.formatText("&c&l[ABILITY]&r&c Your ability is on cooldown."));
            }
        } else {
            // clear body and start meeting
            armorStand.teleport(new Location(armorStand.getLocation().getWorld(), 0, 0, 0));
            armorStand.setInvulnerable(false);
            armorStand.setInvisible(true);
            armorStand.setHealth(0);
            this.game.startDiscussion(player, "REPORT: " + bodyPlayerName);
        }
    }
}
