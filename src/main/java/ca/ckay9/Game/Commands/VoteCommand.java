package ca.ckay9.Game.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;

public class VoteCommand implements CommandExecutor {
    private Game game;

    public VoteCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.formatText("&c&l[VILLAGE]&r&c You need to be a player to use this command."));
            return false;
        }

        Player player = (Player) sender;
        if (!this.game.ableToVote() || this.game.isPlayerDead(player)) {
            player.sendMessage(Utils.formatText("&c&l[VILLAGE]&r&c Can't vote right now."));
            return false;
        }

        if (args.length <= 0) {
            player.sendMessage(Utils.formatText("&c&l[MEETING]&r&c Invalid player name."));
            return false;
        }

        String targetName = args[0].strip();
        if (targetName.equals("skip")) {
            this.game.voteForSkip(player);
            return false;
        }

        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer == null || this.game.isPlayerDead(targetPlayer)) {
            player.sendMessage(Utils.formatText("&c&l[MEETING]&r&c Invalid player."));
            return false;
        }

        this.game.voteForPlayer(targetPlayer, player);
        return false;
    }

}
