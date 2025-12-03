package ca.ckay9.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import ca.ckay9.Game.Game;

public class VillageCompletor implements TabCompleter {
    private Game game;

    public VillageCompletor(Game game) {
        this.game = game;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            return Collections.emptyList();
        }

        ArrayList<String> options = new ArrayList<>();
        switch (args.length) {
            case 1:
                if (!this.game.isGameInProgress()) {
                    options.add("meeting");
                    options.add("spawn");
                    options.add("start");
                    if (sender instanceof Player) {
                        options.add("no-edit");
                        options.add("vent");
                        options.add("task");
                        options.add("sabotage");
                    }

                    options.add("save");
                    options.add("load");
                    options.add("tasks-needed");
                    options.add("kill-cooldown");
                    options.add("discussion-time");
                    options.add("voting-time");
                    options.add("button-time");
                    options.add("max-button");
                    options.add("task-win");
                    options.add("ability-cooldown");
                    options.add("mob-count");
                    options.add("blind");
                    options.add("table-radius");
                } else {
                    options.add("end");
                    options.add("force-villager");
                    options.add("force-mob");
                }
                break;
            case 2:
                String subCommand = args[0].strip().toLowerCase();
                if (subCommand.equals("force-villager") || subCommand.equals("force-mob")) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        options.add(player.getName());
                    }
                }
                break;
            default:
                break;
        }

        return options;
    }
}
