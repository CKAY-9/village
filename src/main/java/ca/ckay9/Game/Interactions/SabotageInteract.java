package ca.ckay9.Game.Interactions;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import ca.ckay9.Utils;
import ca.ckay9.Village;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Mobs.Sabotage;

public class SabotageInteract implements Listener {
    private Game game;

    public SabotageInteract(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void interactWithSabotageBlock(PlayerInteractEvent event) {
        if (!this.game.isGameInProgress() && !Village.inDeveloperDebug()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (this.game.isPlayerDead(player)) {
            return;
        }

        Block block = event.getClickedBlock();
        Sabotage sabotage = this.game.getSabotageByStructure(block);
        if (sabotage == null) {
            return;
        }

        event.setCancelled(true);
        Sabotage activeSabotage = this.game.getActiveSabotage();
        if (activeSabotage != null && activeSabotage.getSabotageType() != sabotage.getSabotageType()) {
            player.sendMessage(Utils.formatText("&c&l[SABOTAGE]&r&c Another sabotage is going on."));
            return;
        }
        
        if (!sabotage.isActive()) {
            player.sendMessage(Utils.formatText("&c&l[SABOTAGE]&r&c There is no sabotage going on."));
            return;
        }

        sabotage.solve(player);
    }
}
