package ca.ckay9.Commands;

import org.bukkit.Bukkit;
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

    private void handleForceVillager(Player player, String targetName) {
        if (!this.village.getGame().isGameInProgress()) {
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer == null) {
            return;
        }

        player.sendMessage(Utils.formatText("&a&l[Village]&r&a Forced " + targetName + " to Villagers."));
        this.village.getGame().setPlayerToVillager(targetPlayer);
    }

    private void handleForceMob(Player player, String targetName) {
        if (!this.village.getGame().isGameInProgress()) {
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer == null) {
            return;
        }

        player.sendMessage(Utils.formatText("&a&l[Village]&r&a Forced " + targetName + " to Mobs."));
        this.village.getGame().setPlayerToMob(targetPlayer);
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
            player.sendMessage(Utils.formatText(
                    "&a&l[Village]&r&a Command usage: /village [subcommand] [arguments]"));
            player.sendMessage(Utils.formatText("&a - vent: Activate the Vent Editor"));
            player.sendMessage(Utils.formatText("&a - task: Activate the Task Editor"));
            player.sendMessage(Utils.formatText("&a - meeting: Set the center of the meeting table"));
            player.sendMessage(Utils.formatText("&a - spawn: Set the center of the spawn"));
            player.sendMessage(Utils.formatText("&a - start: Start a game of Village"));
            player.sendMessage(Utils.formatText("&a - end: End a game of Village"));
            player.sendMessage(Utils.formatText("&a - no-edit: Exit any editor"));
            player.sendMessage(Utils.formatText("&a - force-mob (player: required): Force player to Mobs"));
            player.sendMessage(Utils.formatText("&a - force-villager (player: required): Force player to Villagers"));
            player.sendMessage(Utils.formatText(
                    "&a - save (id: optional): Will save current setup under the ID, defaults to the world name"));
            player.sendMessage(Utils
                    .formatText("&a - load (id: optional): Will load setup given the ID, defaults to the world name"));
            player.sendMessage(
                    Utils.formatText("&a - tasks-needed (number: required): Set the amount of tasks per Villager"));
            player.sendMessage(
                    Utils.formatText("&a - kill-cooldown (number: required, ticks): Set the kill cooldown in ticks"));
            player.sendMessage(
                    Utils.formatText(
                            "&a - discussion-time (number: required, ticks): Set the discussion time in ticks"));
            player.sendMessage(
                    Utils.formatText("&a - voting-time (number: required, ticks): Set the voting time in ticks"));
            player.sendMessage(
                    Utils.formatText(
                            "&a - button-time (number: required, ticks): Set the cooldown on button in ticks"));
            player.sendMessage(Utils.formatText(
                    "&a - max-button (number: required): Set the max amount of button pressed per Villager"));
            player.sendMessage(Utils.formatText(
                    "&a - task-win: Toggle allowing Villagers to win on task compleition"));
            player.sendMessage(Utils.formatText(
                    "&a - ability-cooldown (number: required, ticks): Set the abilitiy cooldown in ticks"));
            return false;
        }

        try {
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
                    player.sendMessage(Utils.formatText("&a&l[Village]&r&a Starting Village."));
                    this.village.getGame().start();
                    break;
                case "end":
                    player.sendMessage(Utils.formatText("&a&l[Village]&r&a Ending Village."));
                    this.village.getGame().end();
                    break;
                case "no-edit":
                    this.village.getEditor().exitEditor(player);
                    break;
                case "force-villager":
                    handleForceVillager(player, args[1].strip());
                    break;
                case "force-mob":
                    handleForceMob(player, args[1].strip());
                    break;
                case "save":
                    player.sendMessage(Utils.formatText("&e&l[VILLAGE]&r&e Saving world config..."));
                    if (args.length == 2) {
                        this.village.getGame().saveCurrentGameConfig(args[1].strip());
                    } else {
                        this.village.getGame().saveCurrentGameConfig(player.getWorld().getName());
                    }
                    player.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Saved world config!"));
                    break;
                case "load":
                    player.sendMessage(Utils.formatText("&e&l[VILLAGE]&r&e Loading world config..."));
                    if (args.length == 2) {
                        this.village.getGame().loadFromSaveID(player.getWorld(), args[1].strip());
                    } else {
                        this.village.getGame().loadFromSaveID(player.getWorld(), player.getWorld().getName());
                    }
                    player.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Loaded world config!"));
                    break;
                case "tasks-needed":
                    int tasksNeeded = Integer.valueOf(args[1].strip());
                    if (this.village.getGame().setTasksPerVillager(tasksNeeded)) {
                        player.sendMessage(Utils.formatText("&a&l[Village]&r&a Updated needed task count."));
                    } else {
                        player.sendMessage(
                                Utils.formatText("&c&l[Village]&r&c Failed to update needed task count. Max value: &c&l"
                                        + this.village.getGame().getVillagerTasks().size()));
                    }
                    break;
                case "kill-cooldown":
                    long killCooldown = Long.valueOf(args[1].strip());
                    this.village.getGame().setKillCooldown(killCooldown);
                    player.sendMessage(Utils.formatText("&a&l[Village]&r&a Updated kill cooldown."));
                    break;
                case "discussion-time":
                    long discussionTime = Long.valueOf(args[1].strip());
                    this.village.getGame().setDiscussionTime(discussionTime);
                    player.sendMessage(Utils.formatText("&a&l[Village]&r&a Updated discussion time."));
                    break;
                case "voting-time":
                    long votingTime = Long.valueOf(args[1].strip());
                    this.village.getGame().setVotingTime(votingTime);
                    player.sendMessage(Utils.formatText("&a&l[Village]&r&a Updated voting time."));
                    break;
                case "button-time":
                    long buttonTime = Long.valueOf(args[1].strip());
                    this.village.getGame().setMeetingButtonCooldown(buttonTime);
                    player.sendMessage(Utils.formatText("&a&l[Village]&r&a Updated button cooldown."));
                    break;
                case "max-button":
                    int uses = Integer.valueOf(args[1].strip());
                    this.village.getGame().setMaxMeetingButtonUses(uses);
                    player.sendMessage(Utils.formatText("&a&l[Village]&r&a Updated max button uses."));
                    break;
                case "task-win":
                    if (!this.village.getGame().canWinOnTasks()) {
                        this.village.getGame().setAllowTaskWin(true);
                        player.sendMessage(
                                Utils.formatText("&a&l[Village]&r&a Allowing Task Completion win condition."));
                    } else {
                        this.village.getGame().setAllowTaskWin(false);
                        player.sendMessage(
                                Utils.formatText("&a&l[Village]&r&a Ignoring Task Completion win condition."));
                    }
                case "ability-cooldown":
                    long abilityCooldown = Long.valueOf(args[1].strip());
                    this.village.getGame().setAbilityCooldown(abilityCooldown);
                    player.sendMessage(Utils.formatText("&a&l[Village]&r&a Updated ability cooldown."));
                    break;
                default:
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }

        return false;
    }
}
