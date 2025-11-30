package ca.ckay9.Game.Interactions;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;

public class VoteInteract implements Listener {
    private Game game;

    public VoteInteract(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (!this.game.isGameInProgress() || !this.game.ableToVote()) {
            return;
        }

        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }

        Player player = event.getPlayer();
        if (this.game.isPlayerDead(player)) {
            return;
        }

        Player target = (Player) event.getRightClicked();
        if (this.game.isPlayerDead(target)) {
            return;
        }

        UUID playerUUID = player.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (this.game.hasAlreadyVotedForThisPlayer(targetUUID, playerUUID)) {
            this.game.clearPreviousPlayerVotes(playerUUID);
            Utils.verbosePlayerLog(player,
                    "Removed their vote from player " + target.getName() + " (" + targetUUID + ")");
            player.sendMessage(Utils.formatText("&e&l[MEETING]&r&e You removed your vote from &l" + target.getName()));
        } else {
            this.game.clearPreviousPlayerVotes(playerUUID);
            Utils.verbosePlayerLog(player, "Voted for player " + target.getName() + " (" + targetUUID + ")");
            this.game.addVote(targetUUID, playerUUID);
            player.sendMessage(Utils.formatText("&e&l[MEETING]&r&e You voted for &l" + target.getName()));
        }

        event.setCancelled(true);
    }
}
