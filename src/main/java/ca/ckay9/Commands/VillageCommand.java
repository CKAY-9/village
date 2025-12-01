package ca.ckay9.Commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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

    public void handleMeeting(CommandSender sender, String[] args) {
        if (args.length < 5) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(
                        Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village meeting world x y z."));
                return;
            }

            sender.sendMessage(
                    Utils.formatText("&a&l[VILLAGE]&r&a Invalid arguments, using your position as meeting location."));
            this.village.getGame().setMeetingLocation(((Player) sender).getLocation());
        } else {
            String worldName = args[1].strip();
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage(
                        Utils.formatText("&c&l[VILLAGE]&r&c Failed to set world."));
                return;
            }
            double x = Double.valueOf(args[2].strip());
            double y = Double.valueOf(args[3].strip());
            double z = Double.valueOf(args[4].strip());

            this.village.getGame().setMeetingLocation(new Location(world, x, y, z));
        }

        sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Updated meeting location."));
    }

    public void handleSpawn(CommandSender sender, String[] args) {
        if (args.length < 5) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(
                        Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village spawn world x y z."));
                return;
            }

            sender.sendMessage(
                    Utils.formatText("&a&l[VILLAGE]&r&a Invalid arguments, using your position as spawn location."));
            this.village.getGame().setSpawnLocation(((Player) sender).getLocation());
        } else {
            String worldName = args[1].strip();
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage(
                        Utils.formatText("&c&l[VILLAGE]&r&c Failed to set world."));
                return;
            }

            double x = Double.valueOf(args[2].strip());
            double y = Double.valueOf(args[3].strip());
            double z = Double.valueOf(args[4].strip());

            this.village.getGame().setSpawnLocation(new Location(world, x, y, z));
        }

        sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Updated spawn location."));
    }

    private void handleForceVillager(CommandSender sender, String[] args) {
        if (!this.village.getGame().isGameInProgress()) {
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village force-villager player"));
            return;
        }

        String targetVillagerName = args[1].strip();
        Player targetVillagerPlayer = Bukkit.getPlayer(targetVillagerName);
        if (targetVillagerPlayer == null) {
            return;
        }

        sender.sendMessage(
                Utils.formatText("&a&l[VILLAGE]&r&a Forced " + targetVillagerName + " to Villagers."));
        this.village.getGame().setPlayerToVillager(targetVillagerPlayer);
    }

    private void handleForceMob(CommandSender sender, String[] args) {
        if (!this.village.getGame().isGameInProgress()) {
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village force-mob player"));
            return;
        }

        String targetMobName = args[1].strip();
        Player targetMobPlayer = Bukkit.getPlayer(targetMobName);
        if (targetMobPlayer == null) {
            return;
        }

        targetMobPlayer
                .sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Forced " + targetMobName + " to Mobs."));
        this.village.getGame().setPlayerToMob(targetMobPlayer);
    }

    private void handleSave(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village save id"));
            return;
        }

        String saveTargetID = args[1].strip();
        sender.sendMessage(Utils.formatText("&e&l[VILLAGE]&r&e Saving " + saveTargetID + " config..."));

        if (this.village.getGame().saveCurrentGameConfig(saveTargetID)) {
            sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Saved " + saveTargetID + " config."));
        } else {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Failed to save " + saveTargetID + " config!"));
        }
    }

    private void handleLoad(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village load world id"));
            return;
        }

        String loadTargetID = args[2].strip();
        sender.sendMessage(Utils.formatText("&e&l[VILLAGE]&r&e Loading " + loadTargetID + " config..."));
        String worldNameToLoad = args[1].strip();

        World worldToLoad = Bukkit.getWorld(worldNameToLoad);
        if (worldToLoad == null) {
            sender.sendMessage(Utils.formatText("&c&l[VILLAGE]&r&c Failed to load world!"));
            return;
        }

        if (this.village.getGame().loadFromSaveID(worldToLoad, loadTargetID)) {
            sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Loaded " + loadTargetID + " config."));
        } else {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Failed to load " + loadTargetID + " config!"));
        }
    }

    private void handleTasksNeeded(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village " + args[0].strip() + " number"));
            return;
        }

        int tasksNeeded = Integer.valueOf(args[1].strip());
        this.village.getGame().setTasksPerVillager(tasksNeeded);
        sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Updated tasks per villager."));
    }

    private void handleMobCount(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village " + args[0].strip() + " number"));
            return;
        }

        int mobCount = Integer.valueOf(args[1].strip());
        this.village.getGame().setMobCount(mobCount);
        sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Updated mob count."));
    }

    private void handleKillCooldown(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village " + args[0].strip() + " number"));
            return;
        }

        long killCooldown = Long.valueOf(args[1].strip());
        this.village.getGame().setKillCooldown(killCooldown);
        sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Updated kill cooldown."));
    }

    private void handleDiscussionTime(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village " + args[0].strip() + " number"));
            return;
        }

        long discussionTime = Long.valueOf(args[1].strip());
        this.village.getGame().setDiscussionTime(discussionTime);
        sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Updated discussion time."));
    }

    private void handleVotingTime(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village tasks-needed number"));
            return;
        }

        long votingTime = Long.valueOf(args[1].strip());
        this.village.getGame().setVotingTime(votingTime);
        sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Updated voting time."));
    }

    private void handleButtonTime(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village " + args[0].strip() + " number"));
            return;
        }

        long buttonTime = Long.valueOf(args[1].strip());
        this.village.getGame().setMeetingButtonCooldown(buttonTime);
        sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Updated button cooldown."));
    }

    private void handleMaxButtons(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village " + args[0].strip() + " number"));
            return;
        }

        int uses = Integer.valueOf(args[1].strip());
        this.village.getGame().setMaxMeetingButtonUses(uses);
        sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Updated max button uses."));
    }

    private void handleTaskWin(CommandSender sender) {
        if (!this.village.getGame().canWinOnTasks()) {
            this.village.getGame().setAllowTaskWin(true);
            sender.sendMessage(
                    Utils.formatText("&a&l[VILLAGE]&r&a Allowing Task Completion win condition."));
        } else {
            this.village.getGame().setAllowTaskWin(false);
            sender.sendMessage(
                    Utils.formatText("&a&l[VILLAGE]&r&a Ignoring Task Completion win condition."));
        }
    }

    private void handleAbilityCooldown(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village " + args[0].strip() + " number"));
            return;
        }

        long abilityCooldown = Long.valueOf(args[1].strip());
        this.village.getGame().setAbilityCooldown(abilityCooldown);
        sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Updated ability cooldown."));
    }

    private void handleBlind(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c Usage: /village " + args[0].strip() + " number"));
            return;
        }

        int blindAmount = Integer.valueOf(args[1].strip());
        this.village.getGame().setBlindAmount(blindAmount);
        sender.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Updated blind amount."));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(
                    Utils.formatText("&c&l[VILLAGE]&r&c You need to be a server operator to execute this command!"));
            return false;
        }

        if (args.length <= 0) {
            sender.sendMessage(Utils.formatText(
                    "&a&l[VILLAGE]&r&a Command usage: /village [subcommand] [arguments]"));
            if (sender instanceof Player) {
                sender.sendMessage(Utils.formatText("&a - vent: Activate the Vent Editor"));
                sender.sendMessage(Utils.formatText("&a - task: Activate the Task Editor"));
                sender.sendMessage(Utils.formatText("&a - no-edit: Exit any editor"));
            }
            sender.sendMessage(Utils.formatText("&a - meeting world x y z: Set the center of the meeting table"));
            sender.sendMessage(Utils.formatText("&a - spawn world x y z: Set the center of the spawn"));
            sender.sendMessage(Utils.formatText("&a - start: Start a game of Village"));
            sender.sendMessage(Utils.formatText("&a - end: End a game of Village"));
            sender.sendMessage(Utils.formatText("&a - force-mob player: Force player to Mobs"));
            sender.sendMessage(Utils.formatText("&a - force-villager player: Force player to Villagers"));
            sender.sendMessage(Utils.formatText(
                    "&a - save id: Will save current setup under the ID"));
            sender.sendMessage(Utils
                    .formatText(
                            "&a - load world id: Will load setup given the ID into the specified world"));
            sender.sendMessage(
                    Utils.formatText("&a - tasks-needed number: Set the amount of tasks per Villager"));
            sender.sendMessage(
                    Utils.formatText("&a - kill-cooldown ticks: Set the kill cooldown in ticks"));
            sender.sendMessage(
                    Utils.formatText(
                            "&a - discussion-time ticks: Set the discussion time in ticks"));
            sender.sendMessage(
                    Utils.formatText("&a - voting-time ticks: Set the voting time in ticks"));
            sender.sendMessage(
                    Utils.formatText(
                            "&a - button-time ticks: Set the cooldown on button in ticks"));
            sender.sendMessage(Utils.formatText(
                    "&a - max-buttons number: Set the max amount of button pressed per Villager"));
            sender.sendMessage(Utils.formatText(
                    "&a - task-win: Toggle allowing Villagers to win on task compleition"));
            sender.sendMessage(Utils.formatText(
                    "&a - ability-cooldown ticks: Set the abilitiy cooldown in ticks"));
            sender.sendMessage(Utils.formatText(
                    "&a - mob-count number: Set the amount of mobs"));
            sender.sendMessage(Utils.formatText(
                    "&a - blind number: Set how blind Villagers are. Zero will give no blindness."));
            return false;
        }

        String subCommand = args[0].toLowerCase().strip();
        if (!(sender instanceof Player)
                && (subCommand.equals("vent") || subCommand.equals("task") || subCommand.equals("no-edit"))) {
            sender.sendMessage(Utils.formatText("&c&l[VILLAGE]&r&c You must be a player to use this command"));
            return false;
        }

        switch (subCommand) {
            case "vent":
                this.village.getEditor().enableVentEditorForPlayer((Player) sender);
                break;
            case "task":
                this.village.getEditor().enableTaskEditorForPlayer((Player) sender);
                break;
            case "meeting":
                handleMeeting(sender, args);
                break;
            case "spawn":
                handleSpawn(sender, args);
                break;
            case "start":
                this.village.getGame().start(sender);
                break;
            case "end":
                this.village.getGame().end(sender);
                break;
            case "no-edit":
                this.village.getEditor().exitEditor((Player) sender);
                break;
            case "force-villager":
                handleForceVillager(sender, args);
                break;
            case "force-mob":
                handleForceMob(sender, args);
                break;
            case "save":
                handleSave(sender, args);
                break;
            case "load":
                handleLoad(sender, args);
                break;
            case "tasks-needed":
                handleTasksNeeded(sender, args);
                break;
            case "mob-count":
                handleMobCount(sender, args);
                break;
            case "kill-cooldown":
                handleKillCooldown(sender, args);
                break;
            case "discussion-time":
                handleDiscussionTime(sender, args);
                break;
            case "voting-time":
                handleVotingTime(sender, args);
                break;
            case "button-time":
                handleButtonTime(sender, args);
                break;
            case "max-buttons":
                handleMaxButtons(sender, args);
                break;
            case "task-win":
                handleTaskWin(sender);
                break;
            case "ability-cooldown":
                handleAbilityCooldown(sender, args);
                break;
            case "blind":
                handleBlind(sender, args);
            default:
                break;
        }

        return false;
    }
}
