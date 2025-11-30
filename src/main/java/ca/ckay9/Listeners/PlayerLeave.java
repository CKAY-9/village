package ca.ckay9.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import ca.ckay9.Game.Game;

public class PlayerLeave implements Listener {
    private Game game;
    
    public PlayerLeave(Game game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event) {
        
    }
}
