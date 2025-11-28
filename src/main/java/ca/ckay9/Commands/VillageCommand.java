package ca.ckay9.Commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ca.ckay9.Utils;
import ca.ckay9.Village;

public class VillageCommand implements CommandExecutor {
    private Village village;

    public VillageCommand(Village village) {
        this.village = village;
    }

    private void handleGameStart() {

    }

    private void handleGameEnd() {

    }

    private void handleSpawnLocation(Player player) {
        this.village.getGame().setSpawnLocation(player.getLocation());
        player.sendMessage(Utils
                .formatText(
                        "&a&l[Village]&r&a Updated spawn location. Players will now spawn here at the start of a Village match."));
    }

    private void handleMeetingLocation(Player player) {
        this.village.getGame().setMeetingLocation(player.getLocation());
        player.sendMessage(Utils
                .formatText(
                        "&a&l[Village]&r&a Updated meeting location. Players will now spawn around this location when a meeting starts."));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("[Village] You need to be a player to execute this command!");
            return false;
        }

        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage(
                    Utils.formatText("&c&l[Village]&r&c You need to be a server operator to execute this command!"));
            return false;
        }

        if (args.length <= 0) {
            player.sendMessage(
                    Utils.formatText(
                            "&a&l[Village]&r&a Command usage: /village [vent/task/meeting/spawn/start/end/no-edit]"));
            return false;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            player.sendMessage(
                    Utils.formatText("&c&l[Village]&r&c You need to be in creative to use this command"));
            return false;
        }

        String subCommand = args[0].toLowerCase().strip();
        switch (subCommand) {
            case "vent":
                this.village.getEditor().enableVentEditorForPlayer(player);
                break;
            case "task":
                this.village.getEditor().enableTaskEditorForPlayer(player);
                break;
            case "meeting":
                handleMeetingLocation(player);
                break;
            case "spawn":
                handleSpawnLocation(player);
                break;
            case "start":
                handleGameStart();
                break;
            case "end":
                handleGameEnd();
                break;
            case "no-edit":
                break;
            default:
                break;
        }

        return false;
    }
}
