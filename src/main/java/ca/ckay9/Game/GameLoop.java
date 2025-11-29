package ca.ckay9.Game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/*

    This is responsible for the main game loop of Village. Keeps track of timings and everything related to the gameplay.

*/
public class GameLoop implements Runnable {
    private Game game;
    private HUD hud;
    private long ticksSinceStart;

    public GameLoop(Game game) {
        this.game = game;
        this.hud = new HUD(this, game);
        this.ticksSinceStart = 0;
    }

    public long getTicksSinceStart() {
        return this.ticksSinceStart;
    }

    public void setTicksSinceStart(int value) {
        this.ticksSinceStart = value;
    }

    @Override
    public void run() {
        if (!this.game.isGameInProgress()) {
            return;
        }
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            hud.drawHUD(p);
        }

        ticksSinceStart++;
    }
}
