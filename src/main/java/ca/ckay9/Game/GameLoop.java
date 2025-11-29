package ca.ckay9.Game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

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

    private void villagerOnTick(Player player) {
        if (this.game.hasCompletedAllTasks()) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() != Material.COMPASS) {
                item = player.getInventory().getItemInOffHand();
            }

            if (item.getType() == Material.COMPASS) {
                CompassMeta meta = (CompassMeta) item.getItemMeta();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!this.game.isPlayerVillager(p)) {
                        meta.setLodestone(p.getLocation());
                        break;
                    }
                }

                item.setItemMeta(meta);
            }
        }
    }

    private void mobOnTick(Player player) {
        Long cooldown = this.game.getKillCooldowns().get(player.getUniqueId());
        if (cooldown == null) {
            cooldown = 0L;
            this.game.addKillCooldown(player.getUniqueId(), 0L);
        }

        if (cooldown > 0) {
            this.game.addKillCooldown(player.getUniqueId(), cooldown - 1);
        }
    }

    @Override
    public void run() {
        if (!this.game.isGameInProgress()) {
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            hud.drawHUD(p);

            if (this.game.isPlayerVillager(p)) {
                villagerOnTick(p);
            } else {
                mobOnTick(p);
            }
        }

        ticksSinceStart++;
    }
}
