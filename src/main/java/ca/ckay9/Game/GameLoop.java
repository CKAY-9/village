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
    private long ticksSinceStart; // this counts up from game start until game end
    private long ticksInCurrentState; // counts up since state change

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

    public long getTicksInCurrentState() {
        return this.ticksInCurrentState;
    }

    public void setTicksInCurrentState(long value) {
        this.ticksInCurrentState = value;
    }

    private void villagerOnTick(Player player) {
        if (this.game.hasCompletedAllTasks()) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() != Material.COMPASS) {
                item = player.getInventory().getItemInOffHand();
            }

            if (item.getType() == Material.COMPASS) {
                CompassMeta meta = (CompassMeta) item.getItemMeta();
                double distance = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!this.game.isPlayerVillager(p)) {
                        distance = meta.getLodestone().distance(p.getLocation());
                        meta.setLodestone(p.getLocation());
                        break;
                    }
                }

                if (distance < 3) {
                    item.setItemMeta(meta);
                }
            }
        }
    }

    private void decreaseKillCooldownOnTick(Player player) {
        Long cooldown = this.game.getKillCooldowns().get(player.getUniqueId());
        if (cooldown == null) {
            cooldown = this.game.getKillCooldown();
            this.game.addKillCooldown(player.getUniqueId(), cooldown);
        }

        if (cooldown > 0 && this.game.getGameStatus() == Status.PLAYING) {
            this.game.addKillCooldown(player.getUniqueId(), Math.max(0, cooldown - 1));
        }
    }

    @Override
    public void run() {
        if (!this.game.isGameInProgress()) {
            return;
        }

        if (this.game.getGameStatus() == Status.DISCUSSION) {
            if (this.getTicksInCurrentState() > this.game.getDiscussionTime()) {
                this.game.startVoting();
            }
        }

        if (this.game.getGameStatus() == Status.VOTING) {
            if (this.getTicksInCurrentState() > this.game.getVotingTime()
                    && this.game.getGameStatus() != Status.ENDING_MEETING) {
                this.game.endMeeting();
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (this.game.isPlayerVillager(p)) {
                villagerOnTick(p);
            } else {
                decreaseKillCooldownOnTick(p);
            }

            Role role = this.game.getPlayerRole(p.getUniqueId());
            if (role != null && (role != Role.VILLAGER || role != Role.MOB)) {
                if (role == Role.DETECTIVE) {
                    decreaseKillCooldownOnTick(p);
                }

                Long cooldown = this.game.getAbilityCooldowns().get(p.getUniqueId());
                if (cooldown == null) {
                    cooldown = this.game.getAbilityCooldown();
                    this.game.addAbilityCooldown(p.getUniqueId(), cooldown);
                }

                if (cooldown > 0 && this.game.getGameStatus() == Status.PLAYING) {
                    this.game.addAbilityCooldown(p.getUniqueId(), Math.max(0, cooldown - 1));
                }
            }

            hud.drawHUD(p);
        }

        ticksInCurrentState++;
        ticksSinceStart++;
    }
}
