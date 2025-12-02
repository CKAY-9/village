package ca.ckay9.Game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import ca.ckay9.Utils;
import ca.ckay9.Game.Mobs.Sabotage;
import ca.ckay9.Game.Villagers.UploadPart;
import ca.ckay9.Game.Villagers.VillagerTask;
import ca.ckay9.Game.Villagers.VillagerTaskType;

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

    /**
     * Uses the ticks to determine when a second shouldve pasts
     * 
     * @param seconds How many seconds should have passed
     * @return True if that many seconds have passed
     */
    public boolean onSecond(float seconds) {
        return this.getTicksSinceStart() % Math.round(20 * seconds) == 0;
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
                    if (!this.game.isPlayerVillager(p) && !this.game.isPlayerDead(player)) {
                        meta.setLodestone(p.getLocation());
                        break;
                    }
                }

                item.setItemMeta(meta);
            }
        }

        if (this.game.shouldBlind()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (this.game.isPlayerDead(player)) {
                    continue;
                }

                double distance = player.getLocation().distance(p.getLocation());
                if (distance > this.game.getBlindAmount()) {
                    player.hidePlayer(Utils.getPlugin(), p);
                } else {
                    player.showPlayer(Utils.getPlugin(), p);
                }
            }

            for (Entity e : player.getWorld().getEntities()) {
                if (e.getType() != EntityType.ARMOR_STAND || !e.getCustomName().contains(Utils.formatText("&c&lBODY"))) {
                    continue;
                }

                double distance = player.getLocation().distance(e.getLocation());
                if (distance > this.game.getBlindAmount()) {
                    player.hideEntity(Utils.getPlugin(), e);
                } else {
                    player.showEntity(Utils.getPlugin(), e);
                }
            }
        }

        if (onSecond(1 / 2)) {
            for (VillagerTask task : this.game.getVillagerTasks()) {
                if (!task.assignedToThis(player.getUniqueId())) {
                    continue;
                }

                if (task.hasCompleted(player.getUniqueId())) {
                    continue;
                }

                UploadPart part = this.game.getUploadParts().get(player.getUniqueId());
                if (task.getTaskType() == VillagerTaskType.UPLOAD) {
                    if (this.game.isFirstPartUpload(task.getBlock().getLocation()) && part != null) {
                        continue;
                    }

                    if (!this.game.isFirstPartUpload(task.getBlock().getLocation()) && part != UploadPart.COPIED) {
                        continue;
                    }
                }

                Location loc = task.getBlock().getLocation();
                for (double y = 0; y < 2.5; y += 0.2) {
                    player.spawnParticle(Particle.END_ROD,
                            loc.clone().add(0.5, y, 0.5),
                            1, 0, 0, 0, 0);
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

        if (onSecond(1)) {
            for (VillagerTask task : this.game.getVillagerTasks()) {
                task.showEffectCloud(this.game);
            }
        }

        // sabotage
        Sabotage activeSabotage = this.game.getActiveSabotage();
        if (activeSabotage != null) {
            if (activeSabotage.shouldEndGame(this.getTicksSinceStart())
                    && this.game.getGameStatus() == Status.PLAYING) {
                this.game.checkWinCondition();
            }

            if (onSecond(0.85f)) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(activeSabotage.getBlock().getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.45f, 1.1f);
                }
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
