package ca.ckay9.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import ca.ckay9.Utils;

public class MagicWandInteract implements Listener {
    private Game game;

    public MagicWandInteract(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWandUse(PlayerInteractEvent event) {
        if (!this.game.isGameInProgress() || this.game.getGameStatus() != Status.PLAYING) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (this.game.isPlayerDead(player)) {
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand != null) {
            itemInHand = player.getInventory().getItemInOffHand();
        }

        if (itemInHand == null || itemInHand.getType() != Material.NETHERITE_HOE) {
            return;
        }

        Role role = this.game.getPlayerRole(player.getUniqueId());
        if (role == null || role != Role.DARK_WIZARD || !this.game.canUseAbility(player.getUniqueId())) {
            return;
        }

        List<Player> shuffled = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(shuffled);
        int i = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(shuffled.get(i).getLocation());
            i++;
        }

        Bukkit.broadcastMessage(Utils.formatText("&c&l[DARK WIZARD]&r&c SWAP!"));
        this.game.addAbilityCooldown(player.getUniqueId(), this.game.getAbilityCooldown());
    }
}
