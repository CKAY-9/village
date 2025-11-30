package ca.ckay9.Game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import ca.ckay9.Utils;
import ca.ckay9.Village;

public class HUD {
    private GameLoop gameLoop;
    private Game game;
    ScoreboardManager scoreboardManager;

    public HUD(GameLoop gameLoop, Game game) {
        this.game = game;
        this.gameLoop = gameLoop;
        this.scoreboardManager = Bukkit.getScoreboardManager();
    }

    /**
     * Will draw the appropriate HUD for the given player
     * 
     * @param player Who to draw the HUD for
     */
    public void drawHUD(Player player) {
        // create the side HUD
        Scoreboard board = this.scoreboardManager.getNewScoreboard();
        Objective objective = board.registerNewObjective("Village Primary HUD", "dummy",
                Utils.formatText("&a&l! VILLAGE !"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = 100;

        // border for the title text
        Score titleBorder = objective.getScore(Utils.formatText("&l=-=-=-=-=-=-=-=-=-=-=-"));
        titleBorder.setScore(score--);

        if (Village.inDeveloperDebug()) {
            Score statusText = objective.getScore(Utils.formatText("&8Status: &a&l" + this.game.getGameStatus()));
            statusText.setScore(score--);

            Score eTickText = objective.getScore(Utils.formatText("&8TSS: &a&l" + this.gameLoop.getTicksSinceStart()
                    + " &r&8(" + Utils.ticksToSeconds(this.gameLoop.getTicksSinceStart()) + "s)"));
            eTickText.setScore(score--);

            Score stateTickText = objective
                    .getScore(Utils.formatText("&8TICS: &a&l" + this.gameLoop.getTicksInCurrentState() + " &r&8("
                            + Utils.ticksToSeconds(this.gameLoop.getTicksInCurrentState()) + "s)"));
            stateTickText.setScore(score--);

            Score mobCount = objective
                    .getScore(Utils.formatText("&8Mobs: &c&l" + this.game.getAliveMobCount() + "/"
                            + this.game.getMobCount()));
            mobCount.setScore(score--);

            Score villagerCount = objective
                    .getScore(Utils.formatText("&8Villagers: &a&l" + this.game.getAliveVillagerCount() + "/"
                            + this.game.getVillagerCount()));
            villagerCount.setScore(score--);

            Score devBorder = objective.getScore(Utils.formatText("&l=-=-= ^^^ DEV ^^^ =-=-="));
            devBorder.setScore(score--);
        }

        Role role = this.game.getPlayerRole(player.getUniqueId());
        Score roleText = objective.getScore(Utils.formatText("&8Role: &a&l" + role.toString()));
        if (!this.game.isPlayerVillager(player)) {
            roleText = objective.getScore(Utils.formatText("&8Role: &c&l" + role.toString()));
        }

        roleText.setScore(score--);

        if (role == Role.DETECTIVE || role == Role.DARK_WIZARD || role == Role.MEDIC || role == Role.SWEEPER) {
            Long cooldown = this.game.getAbilityCooldowns().get(player.getUniqueId());
            if (cooldown == null) {
                cooldown = this.game.getAbilityCooldown();
                this.game.addAbilityCooldown(player.getUniqueId(), cooldown);
            }

            long inSeconds = Math.max(0, Math.round(cooldown / 20));
            Score cooldownText = objective
                    .getScore(Utils.formatText("&8Ability Cooldown: &e&l" + inSeconds + "s"));
            if (inSeconds <= 0) {
                cooldownText = objective
                        .getScore(Utils.formatText("&e&lABILITY READY!"));
            }

            cooldownText.setScore(score--);
        }

        if (!this.game.isPlayerVillager(player) || role == Role.DETECTIVE) {
            Long cooldown = this.game.getKillCooldowns().get(player.getUniqueId());
            if (cooldown == null) {
                cooldown = this.game.getKillCooldown();
                this.game.addKillCooldown(player.getUniqueId(), cooldown);
            }

            long inSeconds = Math.max(0, Math.round(cooldown / 20));
            Score cooldownText = objective
                    .getScore(Utils.formatText("&8Kill Cooldown: &c&l" + inSeconds + "s"));
            if (inSeconds <= 0) {
                cooldownText = objective
                        .getScore(Utils.formatText("&c&lKILL READY!"));
            }

            cooldownText.setScore(score--);
        }

        Score completionText = objective
                .getScore(Utils.formatText("&8Tasks: &a&l" + this.game.getCompletedTaskPercent() + "%"));
        completionText.setScore(score--);

        player.setScoreboard(board);
    }
}
