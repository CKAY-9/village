package ca.ckay9.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import ca.ckay9.Game.Game;
import ca.ckay9.Game.Status;

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
                if (this.game.getGameStatus() == Status.NO_GAME) {
                    options.add("vent");
                    options.add("task");
                    options.add("meeting");
                    options.add("spawn");
                    options.add("start");
                    options.add("no-edit");
                    options.add("save");
                    options.add("load");
                    options.add("tasks-needed");
                    options.add("kill-cooldown");
                    options.add("discussion-time");
                    options.add("voting-time");
                    options.add("button-time");
                    options.add("max-button");
                    options.add("task-win");
                } else {
                    options.add("end");
                    options.add("force-villager");
                    options.add("force-mob");
                }
                break;
            default:
                break;
        }

        return options;
    }
}
