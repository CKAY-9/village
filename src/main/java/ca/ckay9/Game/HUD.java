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
        Score titleBorder = objective.getScore(Utils.formatText("&l=-=-=-=-=-=-=-=-=-=-="));
        titleBorder.setScore(score--);

        if (Village.inDeveloperDebug()) {
            Score statusText = objective.getScore(Utils.formatText("&8Status: &a&l" + this.game.getGameStatus()));
            statusText.setScore(score--);

            Score tickText = objective.getScore(Utils.formatText("&8TSS: &a&l" + this.gameLoop.getTicksSinceStart()));
            tickText.setScore(score--);

            Score devBorder = objective.getScore(Utils.formatText("&l=-=-= ^^^ DEV ^^^ =-=-="));
            devBorder.setScore(score--);
        }

        Score roleText = objective.getScore(Utils.formatText("&8Role: &a&lVillager"));
        if (!this.game.isPlayerVillager(player)) {
            roleText = objective.getScore(Utils.formatText("&8Role: &c&lMob"));
        }

        roleText.setScore(score--);

        Score completionText = objective.getScore(Utils.formatText("&8Tasks: &a&l" + this.game.getCompletedTaskPercent() + "%"));
        completionText.setScore(score--);

        player.setScoreboard(board);
    }
}
