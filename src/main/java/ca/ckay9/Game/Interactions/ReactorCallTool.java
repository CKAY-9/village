package ca.ckay9.Game.Interactions;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Status;
import ca.ckay9.Game.Mobs.Sabotage;
import ca.ckay9.Game.Mobs.SabotageType;

public class ReactorCallTool implements Listener {
    private Game game;

    public ReactorCallTool(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onReactorCall(PlayerInteractEvent event) {
        if (!this.game.isGameInProgress() || this.game.getGameStatus() != Status.PLAYING) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (this.game.isPlayerVillager(player)) {
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null) {
            itemInHand = player.getInventory().getItemInOffHand();
        }

        if (itemInHand == null || itemInHand.getType() != Material.TNT) {
            return;
        }

        event.setCancelled(true);
        Sabotage active = this.game.getActiveSabotage();
        if (active != null) {
            player.sendMessage(Utils.formatText("&c&l[SABOTAGE]&r&c There is already an active sabotage."));
            return;
        }

        Sabotage reactor = this.game.getSabotageByType(SabotageType.REACTOR);
        if (reactor == null) {
            player.sendMessage(Utils.formatText("&c&l[SABOTAGE]&r&c Reactor sabotage doesn't exist."));
            return;
        }

        if (!this.game.canActivateSabotage(this.game.getGameLoop().getTicksSinceStart())) {
            player.sendMessage(Utils.formatText("&c&l[SABOTAGE]&r&c Must wait before activating another sabotage."));
            return;
        }

        reactor.activate(this.game);
    }
}
