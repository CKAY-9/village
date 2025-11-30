package ca.ckay9.Game.Listeners;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;

public class PlayerJoin implements Listener {
    private Game game;
    
    public PlayerJoin(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!this.game.isGameInProgress()) {
            return;
        }

        event.getPlayer().setGameMode(GameMode.SPECTATOR);
        event.getPlayer().teleport(this.game.getSpawnLocation());
        event.getPlayer().sendTitle(Utils.formatText("&a&lVILLAGE"), Utils.formatText("Spectating on-going &a&lVillage&r match"), 20, 80, 20);
    }
}
