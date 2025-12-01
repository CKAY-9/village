package ca.ckay9.Game.Interactions;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Status;

public class MeetingButtonInteract implements Listener {
    private Game game;

    public MeetingButtonInteract(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        if (!armorStand.getCustomName().contains(Utils.formatText("&c&lEMERGENCY MEETING"))) {
            return;
        }

        event.setCancelled(true);
        if (!this.game.isGameInProgress()) {
            return;
        }

        if (this.game.getGameStatus() != Status.PLAYING && !this.game.ableToVote()) {
            return;
        }

        Player player = event.getPlayer();
        if (this.game.isPlayerDead(player)) {
            return;
        }

        if (this.game.ableToVote()) {
            this.game.voteForSkip(player);
            return;
        }

        // check cooldown
        long tics = this.game.getGameLoop().getTicksInCurrentState();
        long cooldown = this.game.getMeetingButtonCooldown();
        if (tics < cooldown) {
            long waitTime = Math.max(0, Utils.ticksToSeconds(cooldown - tics));
            armorStand.setCustomName(Utils.formatText("&c&lEMERGENCY MEETING - " + waitTime + "s"));
            return;
        } else {
            armorStand.setCustomName(Utils.formatText("&c&lEMERGENCY MEETING - READY"));
        }

        // check usage
        Integer uses = this.game.getMeetingUses().get(player.getUniqueId());
        if (uses != null && uses >= this.game.getMaxMeetingButtonUses()) {
            return;
        }

        if (uses == null) {
            uses = 0;
        }
        
        this.game.getMeetingUses().put(player.getUniqueId(), uses++);
        this.game.startDiscussion(player, "Emergency Button");
    }
}
