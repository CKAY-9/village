package ca.ckay9.Game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ca.ckay9.Storage;
import ca.ckay9.Utils;
import ca.ckay9.Village;
import ca.ckay9.Game.Commands.VoteCommand;
import ca.ckay9.Game.Commands.VoteCompletor;
import ca.ckay9.Game.Interactions.BodyInteract;
import ca.ckay9.Game.Interactions.CleanVentInteract;
import ca.ckay9.Game.Interactions.MagicWandInteract;
import ca.ckay9.Game.Interactions.ManifoldTaskInteract;
import ca.ckay9.Game.Interactions.MeetingButtonInteract;
import ca.ckay9.Game.Interactions.VentInteract;
import ca.ckay9.Game.Interactions.VillagerTaskInteract;
import ca.ckay9.Game.Interactions.VoteInteract;
import ca.ckay9.Game.Listeners.PlayerDamage;
import ca.ckay9.Game.Listeners.PlayerDropItem;
import ca.ckay9.Game.Listeners.PlayerJoin;
import ca.ckay9.Game.Listeners.PlayerMove;
import ca.ckay9.Game.Mobs.Vent;
import ca.ckay9.Game.Villagers.ChatTaskProgress;
import ca.ckay9.Game.Villagers.CraftTaskProgress;
import ca.ckay9.Game.Villagers.UploadPart;
import ca.ckay9.Game.Villagers.VillagerTask;
import ca.ckay9.Game.Villagers.VillagerTaskType;

public class Game {
    private Status status; // current game status
    private Location spawnLocation; // where players are teleported to on game start
    private Location meetingLocation; // the center of meetings, players spawn around this location
    private ArmorStand meetingButton; // this acts as a custom button to start meetings
    private ArrayList<VillagerTask> villagerTasks; // all created tasks
    private HashMap<UUID, Role> playerRoles; // villager or mob => sub roles
    private HashMap<UUID, ChatTaskProgress> chatTaskExpectedResults; // used for trivia and math tasks
    private HashMap<UUID, CraftTaskProgress> craftTaskExpectedResults; // used for crafting tasks
    private ArrayList<Vent> mobVents; // all created mob vents
    private Village village; // parent class
    private int tasksPerVillager; // how many tasks each villager has
    private int mobCount; // the amount of mobs
    private GameLoop gameLoop; // the game loop instance
    private int gameLoopID; // ID to bukkit runnable
    private boolean completedAllTasks; // used to check for villager completion
    private HashMap<UUID, Long> killCooldowns; // keeps track of kill cooldowns for each mob
    private HashMap<UUID, Long> abilityCooldowns; // keeps track of all ability cooldowns
    private HashMap<UUID, Long> timeOfDeaths; // time of death for each player
    private HashSet<UUID> deadPlayers;
    private long killCooldown; // how long do mobs have to wait inbetween kills in ticks
    private long abilityCooldown; // how long do special roles have to wait before using their ability
    private long votingTime; // how long does the voting time last in ticks
    private long discussionTime; // how long do discussions last in ticks
    private HashMap<UUID, ArrayList<UUID>> votes; // used to keep track of each players votes and who voted for them
    private long meetingButtonCooldown; // how long do you have to wait before being able to use the meeting button in
                                        // ticks
    private HashMap<UUID, Integer> buttonUses; // how many times a player has pressed the meeting button
    private int maxButtonUses; // how many times can each player use the button
    private boolean allowTaskWin; // if set to true, villagers can win if they finish all their tasks
    private HashMap<UUID, UploadPart> uploadParts;
    private int blindAmount;

    public Game(Village village) {
        this.village = village;
        this.status = Status.NO_GAME;
        this.villagerTasks = new ArrayList<>();
        this.mobVents = new ArrayList<>();
        this.playerRoles = new HashMap<>();
        this.spawnLocation = null;
        this.meetingLocation = null;
        this.meetingButton = null;
        this.chatTaskExpectedResults = new HashMap<>();
        this.craftTaskExpectedResults = new HashMap<>();
        this.mobCount = 1;
        this.tasksPerVillager = 1;
        this.gameLoop = null;
        this.gameLoopID = -1;
        this.blindAmount = 1;
        this.completedAllTasks = false;
        this.killCooldowns = new HashMap<>();
        this.abilityCooldowns = new HashMap<>();
        this.killCooldown = Utils.secondsToTicks(25);
        this.abilityCooldown = Utils.secondsToTicks(30);
        this.votingTime = Utils.secondsToTicks(45);
        this.discussionTime = Utils.secondsToTicks(30);
        this.votes = new HashMap<>();
        this.meetingButtonCooldown = Utils.secondsToTicks(15);
        this.maxButtonUses = 1;
        this.buttonUses = new HashMap<>();
        this.allowTaskWin = false;
        this.timeOfDeaths = new HashMap<>();
        this.deadPlayers = new HashSet<>();
        this.uploadParts = new HashMap<>();

        PluginManager manager = village.getServer().getPluginManager();
        manager.registerEvents(new VentInteract(this), village);
        manager.registerEvents(new VillagerTaskInteract(this), village);
        manager.registerEvents(new PlayerDamage(this), village);
        manager.registerEvents(new BodyInteract(this), village);
        manager.registerEvents(new PlayerMove(this), village);
        manager.registerEvents(new VoteInteract(this), village);
        manager.registerEvents(new MeetingButtonInteract(this), village);
        manager.registerEvents(new PlayerDropItem(this), village);
        manager.registerEvents(new PlayerJoin(this), village);
        manager.registerEvents(new MagicWandInteract(this), village);
        manager.registerEvents(new ManifoldTaskInteract(this), village);
        manager.registerEvents(new CleanVentInteract(this), village);

        village.getCommand("vote").setExecutor(new VoteCommand(this));
        village.getCommand("vote").setTabCompleter(new VoteCompletor());
    }

    public void setBlindAmount(int amount) {
        this.blindAmount = amount;
    }

    public int getBlindAmount() {
        return this.blindAmount;
    }

    public boolean shouldBlind() {
        return this.blindAmount >= 1;
    }

    public HashMap<UUID, UploadPart> getUploadParts() {
        return this.uploadParts;
    }

    public void setUploadParts(HashMap<UUID, UploadPart> parts) {
        this.uploadParts = parts;
    }

    public void addUploadPart(UUID playerUUID, UploadPart part) {
        this.uploadParts.put(playerUUID, part);
    }

    public void removeUploadPart(UUID playerUUID) {
        this.uploadParts.remove(playerUUID);
    }

    public boolean uploadTaskCreated() {
        int total = 0;
        for (VillagerTask t : this.getVillagerTasks()) {
            if (t.getTaskType() == VillagerTaskType.UPLOAD) {
                total++;
            }
        }

        return total == 2;
    }

    public boolean isFirstPartUpload(Location partLocation) {
        for (VillagerTask t : this.getVillagerTasks()) {
            // first instance of upload
            if (t.getTaskType() == VillagerTaskType.UPLOAD) {
                return t.getBlock().getLocation().equals(partLocation);
            }
        }

        return false;
    }

    public void setDeadPlayers(HashSet<UUID> hashSet) {
        this.deadPlayers = hashSet;
    }

    public void addDeadPlayer(UUID uuid) {
        this.deadPlayers.add(uuid);
    }

    public void removeDeadPlayer(UUID uuid) {
        this.deadPlayers.remove(uuid);
    }

    public HashSet<UUID> getDeadPlayers() {
        return this.deadPlayers;
    }

    /**
     * 
     * @param player Who to check if they're dead
     * @return True if they are dead, false otherwise
     */
    public boolean isPlayerDead(Player player) {
        return this.deadPlayers.contains(player.getUniqueId());
    }

    /**
     * @param value How long should votes last in
     *              ticks (1sec = 20ticks)
     */
    public void setVotingTime(long value) {
        this.votingTime = value;
    }

    /**
     * @return The duration of a vote in ticks
     */
    public long getVotingTime() {
        return this.votingTime;
    }

    /**
     * @param value How long should discussions, where players just talk, should
     *              last in
     *              ticks (1sec = 20ticks)
     */
    public void setDiscussionTime(long value) {
        this.discussionTime = value;
    }

    /**
     * @return The duration of a discussion in ticks
     */
    public long getDiscussionTime() {
        return this.discussionTime;
    }

    /**
     * @param cooldownInTicks How long special roles have to wait before using their
     *                        ability. Given in ticks (1sec = 20ticks)
     */
    public void setAbilityCooldown(long cooldownInTicks) {
        this.abilityCooldown = cooldownInTicks;
    }

    public long getAbilityCooldown() {
        return this.abilityCooldown;
    }

    public HashMap<UUID, Long> getAbilityCooldowns() {
        return this.abilityCooldowns;
    }

    public void setAbilityCooldowns(HashMap<UUID, Long> cooldowns) {
        this.abilityCooldowns = cooldowns;
    }

    /**
     * @param uuid     Player to receive cooldown
     * @param duration Duration of cooldown in ticks
     */
    public void addAbilityCooldown(UUID uuid, long duration) {
        this.abilityCooldowns.put(uuid, duration);
    }

    /**
     * @param uuid The player to check if they can use their ability
     * @return True if they haven't used it yet or the cooldown is reset
     */
    public boolean canUseAbility(UUID uuid) {
        Long cooldown = this.getAbilityCooldowns().get(uuid);
        return (cooldown == null || cooldown.longValue() <= 0);
    }

    /**
     * @param cooldownInTicks How long Mobs have to wait in-between kills. Given in
     *                        ticks (1sec = 20ticks)
     */
    public void setKillCooldown(long cooldownInTicks) {
        this.killCooldown = cooldownInTicks;
    }

    public void setVotes(HashMap<UUID, ArrayList<UUID>> votes) {
        this.votes = votes;
    }

    public HashMap<UUID, ArrayList<UUID>> getVotes() {
        return this.votes;
    }

    /**
     * Adds voter to whoToVoteFor's vote array. Doesn't clear previous entries of
     * voter.
     * 
     * @param whoToVoteFor Player who is getting voted
     * @param voter        Player who is voting
     */
    public void addVote(UUID whoToVoteFor, UUID voter) {
        ArrayList<UUID> arr = this.getVotes().get(whoToVoteFor);
        if (arr == null) {
            arr = new ArrayList<>();
        }

        arr.add(voter);
        this.getVotes().put(whoToVoteFor, arr);
    }

    public boolean hasAlreadyVotedForThisPlayer(UUID uuid, UUID voter) {
        ArrayList<UUID> votes = this.getVotes().get(uuid);
        return (votes != null && votes.contains(voter));
    }

    /**
     * Clears all instances of this player in the votes. Used to cast new votes
     * 
     * @param voter Who to remove
     */
    public void clearPreviousPlayerVotes(UUID voter) {
        for (Map.Entry<UUID, ArrayList<UUID>> entry : this.getVotes().entrySet()) {
            ArrayList<UUID> uuids = entry.getValue();
            if (entry.getValue().contains(voter)) {
                uuids.remove(voter);
                entry.setValue(uuids);
                break;
            }
        }
    }

    /**
     * If the player already has no votes, this just returns
     * 
     * @param uuid          Who to remove a vote from
     * @param voterToRemove The voter who changed/removed thier vote
     */
    public void removeVote(UUID uuid, UUID voterToRemove) {
        ArrayList<UUID> arr = this.getVotes().get(uuid);
        if (arr == null) {
            return;
        }

        arr.remove(voterToRemove);
        if (arr.size() <= 0) {
            this.getVotes().put(uuid, null);
            return;
        }

        this.getVotes().put(uuid, arr);
    }

    public void clearVotes() {
        this.votes.clear();
    }

    public HashMap<UUID, Long> getTimeOfDeaths() {
        return this.timeOfDeaths;
    }

    public Long getTimeOfDeathOfPlayer(Player player) {
        return this.timeOfDeaths.get(player.getUniqueId());
    }

    public void addTimeOfDeath(UUID uuid, long value) {
        this.timeOfDeaths.put(uuid, value);
    }

    /**
     * @param timeOfDeath The time of death in ticks
     * @return The amount of time a player has been dead in ticks
     */
    public long timeSinceDeath(long timeOfDeath) {
        return this.gameLoop.getTicksSinceStart() - timeOfDeath;
    }

    public long getKillCooldown() {
        return this.killCooldown;
    }

    public HashMap<UUID, Long> getKillCooldowns() {
        return this.killCooldowns;
    }

    public void setKillCooldowns(HashMap<UUID, Long> killCooldowns) {
        this.killCooldowns = killCooldowns;
    }

    public void addKillCooldown(UUID uuid, long cooldownInTicks) {
        this.killCooldowns.put(uuid, cooldownInTicks);
    }

    public HashMap<UUID, CraftTaskProgress> getCraftTaskExpectedResults() {
        return this.craftTaskExpectedResults;
    }

    public void setCraftTaskExpectedResults(HashMap<UUID, CraftTaskProgress> results) {
        this.craftTaskExpectedResults = results;
    }

    public void addCraftTaskExpectedResult(UUID uuid, CraftTaskProgress answer) {
        this.craftTaskExpectedResults.put(uuid, answer);
    }

    public void removeCraftTaskExpectedResult(UUID uuid) {
        this.craftTaskExpectedResults.remove(uuid);
    }

    public HashMap<UUID, ChatTaskProgress> getChatTaskExpectedResults() {
        return this.chatTaskExpectedResults;
    }

    public void setChatTaskExpectedResults(HashMap<UUID, ChatTaskProgress> results) {
        this.chatTaskExpectedResults = results;
    }

    public void addChatTaskExpectedResult(UUID uuid, ChatTaskProgress progress) {
        this.chatTaskExpectedResults.put(uuid, progress);
    }

    public void removeChatTaskExpectedResult(UUID uuid) {
        this.chatTaskExpectedResults.remove(uuid);
    }

    public int getMobCount() {
        return this.mobCount;
    }

    public void setMobCount(int value) {
        this.mobCount = value;
    }

    public int getVillagerCount() {
        return Bukkit.getOnlinePlayers().size() - this.getMobCount();
    }

    public boolean hasCompletedAllTasks() {
        return this.completedAllTasks;
    }

    public void setCompletedAllTasks(boolean value) {
        this.completedAllTasks = value;
    }

    public void setMaxMeetingButtonUses(int value) {
        this.maxButtonUses = value;
    }

    public void clearCraftingMaterials(Player player) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) {
                continue;
            }

            if (stack.getType() != Material.COMPASS &&
                    stack.getType() != Material.NETHERITE_SWORD &&
                    stack.getType() != Material.CLOCK &&
                    stack.getType() != Material.NETHERITE_HOE &&
                    stack.getType() != Material.GOLDEN_CARROT) {
                stack.setAmount(0);
            }
        }
    }

    /**
     * @return The amount of tasks each villager
     */
    public int getTasksPerVillager() {
        return this.tasksPerVillager;
    }

    /**
     * Attempts to set tasksPerVillager to the new value. Will fail if you go above
     * the max possible amount of tasks.
     * 
     * @param value The value to set
     * @return true if successful, false if went over max
     */
    public void setTasksPerVillager(int value) {
        this.tasksPerVillager = value;
    }

    /**
     * @return The amount of assigned tasks that have been completed
     */
    public int getAmountOfCompletedTasks() {
        int total = 0;
        for (VillagerTask task : this.getVillagerTasks()) {
            for (Map.Entry<UUID, Boolean> assigned : task.getAssignedVillagers().entrySet()) {
                if (assigned.getValue()) {
                    total++;
                }
            }
        }

        return total;
    }

    public int getAmountOfTasksForPlayers(UUID playerUUID) {
        int count = 0;
        for (VillagerTask task : this.getVillagerTasks()) {
            if (!task.assignedToThis(playerUUID)) {
                continue;
            }

            count++;
        }

        return count;
    }

    public int getAmountOfCompletedTasksByPlayer(UUID playerUUID) {
        boolean countedUpload = !this.uploadTaskCreated();
        int count = 0;
        for (VillagerTask task : this.getVillagerTasks()) {
            if (!task.assignedToThis(playerUUID)) {
                continue;
            }

            if (task.getTaskType() == VillagerTaskType.UPLOAD && countedUpload) {
                continue;
            }

            UploadPart part = this.getUploadParts().get(playerUUID);
            if (task.getTaskType() == VillagerTaskType.UPLOAD && !countedUpload) {
                if (part != null && part != UploadPart.COPYING) {
                    count++;
                    countedUpload = true;
                    continue;
                }
            }

            if (task.hasCompleted(playerUUID)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Forces a player to the villagers. Sets up inventory, tasks, and everything
     * else needed.
     * 
     * @param player Who should be made a villager
     */
    public void setPlayerToVillager(Player player) {
        player.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        player.getInventory().clear();
        if (!this.getPlayerRoles().containsValue(Role.DETECTIVE)
                || this.getPlayerRole(player.getUniqueId()) == Role.DETECTIVE) {
            this.setPlayerRole(player.getUniqueId(), Role.DETECTIVE);

            ItemStack clock = new ItemStack(Material.CLOCK);
            ItemMeta clockMeta = clock.getItemMeta();
            clockMeta.setDisplayName(Utils.formatText("&lT.O.D. CLOCK / RIGHT CLICK BODIES TO SEE DEATH TIME"));
            clock.setItemMeta(clockMeta);
            player.getInventory().addItem(clock);

            Utils.verbosePlayerLog(player, "Changed to detective.");
            player.sendTitle(Utils.formatText("&a&lDETECTIVE"),
                    Utils.formatText("You are a &a&lDecective&r. Can inspect bodies and kill one &a&lVillager."), 20,
                    80, 20);
        } else if (!this.getPlayerRoles().containsValue(Role.MEDIC)
                || this.getPlayerRole(player.getUniqueId()) == Role.MEDIC) {
            this.setPlayerRole(player.getUniqueId(), Role.MEDIC);

            ItemStack goldenCarrot = new ItemStack(Material.GOLDEN_CARROT);
            ItemMeta goldenCarrotMeta = goldenCarrot.getItemMeta();
            goldenCarrotMeta.setDisplayName(Utils.formatText("&lMAGICAL CARROT / RIGHT CLICK BODIES TO REVIVES"));
            goldenCarrot.setItemMeta(goldenCarrotMeta);
            player.getInventory().addItem(goldenCarrot);

            Utils.verbosePlayerLog(player, "Changed to medic.");
            player.sendTitle(Utils.formatText("&a&lMEDIC"),
                    Utils.formatText("You are a &a&lMedic&r. Can revive dead &a&lVillagers."), 20, 80, 20);
        } else {
            this.setPlayerRole(player.getUniqueId(), Role.VILLAGER);

            Utils.verbosePlayerLog(player, "Changed to villager.");
            player.sendTitle(Utils.formatText("&a&lVILLAGER"),
                    Utils.formatText("You are a &a&lVillager&r. Complete your tasks and evict the &c&lMobs&r."), 20, 80,
                    20);
        }

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setHealth(20);
        player.setSaturation(20);

        if (this.uploadTaskCreated()) {
            for (VillagerTask task : this.getVillagerTasks()) {
                if (task.getTaskType() != VillagerTaskType.UPLOAD) {
                    continue;
                }

                task.addAssignedVillager(player.getUniqueId());
            }
        }

        if (this.getVillagerTasks().size() > 0) {
            List<VillagerTask> copy = new ArrayList<>(this.getVillagerTasks());
            copy.removeIf(task -> task.getTaskType() == VillagerTaskType.UPLOAD);
            Collections.shuffle(copy);

            int taskLimit = this.getTasksPerVillager()
                    - (this.uploadTaskCreated() ? Math.min(this.getTasksPerVillager(), 2) : 0);
            taskLimit = Math.max(0, Math.min(taskLimit, copy.size()));
            List<VillagerTask> selectedTasks = copy.subList(0, taskLimit);

            for (VillagerTask task : selectedTasks) {
                task.addAssignedVillager(player.getUniqueId());
            }
        }

        if (this.shouldBlind()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                    10_000_000, this.getBlindAmount(), false, false, false));
        }
        this.revivePlayer(player, this.getSpawnLocation());
    }

    /**
     * Forces a player to the mobs. Sets up inventory, tasks, and everything else
     * needed.
     * 
     * @param player Who should be made a mob
     */
    public void setPlayerToMob(Player player) {
        player.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

        player.getInventory().clear();
        if (!this.getPlayerRoles().containsValue(Role.SWEEPER)
                || this.getPlayerRole(player.getUniqueId()) == Role.SWEEPER) {
            this.setPlayerRole(player.getUniqueId(), Role.SWEEPER);

            ItemStack broom = new ItemStack(Material.NETHERITE_SHOVEL);
            ItemMeta broomMeta = broom.getItemMeta();
            broomMeta.setDisplayName(Utils.formatText("&lBROOM / RIGHT CLICK BODIES TO HIDE"));
            broom.setItemMeta(broomMeta);
            player.getInventory().addItem(broom);

            Utils.verbosePlayerLog(player, "Changed to sweeper.");
            player.sendTitle(Utils.formatText("&c&lSWEEPER"),
                    Utils.formatText("You are a &c&lSweeper&r. Can kill and hide &a&lVillagers."), 20, 80, 20);
        } else if (!this.getPlayerRoles().containsValue(Role.DARK_WIZARD)
                || this.getPlayerRole(player.getUniqueId()) == Role.DARK_WIZARD) {
            this.setPlayerRole(player.getUniqueId(), Role.DARK_WIZARD);

            ItemStack wand = new ItemStack(Material.NETHERITE_HOE);
            ItemMeta wandMeta = wand.getItemMeta();
            wandMeta.setDisplayName(Utils.formatText("&lMAGIC WAND / RIGHT CLICK TO SWAP PLAYER LOCATIONS"));
            wand.setItemMeta(wandMeta);
            player.getInventory().addItem(wand);

            Utils.verbosePlayerLog(player, "Changed to dark wizard.");
            player.sendTitle(Utils.formatText("&c&lDARK WIZARD"),
                    Utils.formatText("You are a &c&lDark Wizard&r. Can kill and swap locations of &a&lVillagers&r."),
                    20, 80, 20);
        } else {
            this.setPlayerRole(player.getUniqueId(), Role.MOB);
            Utils.verbosePlayerLog(player, "Changed to mob.");
            player.sendTitle(Utils.formatText("&c&lMOB"),
                    Utils.formatText("You are a &c&lMob&r. Kill all of the &as&lVillagers&r."),
                    20, 80, 20);
        }

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setHealth(20);
        player.setSaturation(20);

        ItemStack knife = new ItemStack(Material.NETHERITE_SWORD, 1);
        ItemMeta knifeMeta = knife.getItemMeta();
        knifeMeta.setDisplayName(Utils.formatText("&lKNIFE"));
        knife.setItemMeta(knifeMeta);

        player.getInventory().addItem(knife);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(this.getSpawnLocation());

        this.revivePlayer(player, this.getSpawnLocation());
    }

    /**
     * @return The win condition that is complete, NONE if no win condition has been
     *         met.
     */
    public WinCondition getWinCondition() {
        if (!this.isGameInProgress()) {
            Utils.verboseLog("No win condition met.");
            return WinCondition.NONE;
        }

        if (this.hasCompletedAllTasks() && this.canWinOnTasks()) {
            Utils.verboseLog("Task Completion win condition completed.");
            return WinCondition.TASK_COMPLETION;
        }

        if (this.getAliveMobCount() >= this.getAliveVillagerCount()) {
            Utils.verboseLog("Mob Overwhelm win condition completed.");
            return WinCondition.MOB_OVERWHELM;
        }

        if (this.getAliveMobCount() <= 0) {
            Utils.verboseLog("Mobs Voted Out win condition completed.");
            return WinCondition.MOBS_VOTED_OUT;
        }

        Utils.verboseLog("No win condition met.");
        return WinCondition.NONE;
    }

    /**
     * Handles what happens when a player is voted out. "Kills" them.
     * 
     * @param player
     */
    public void evictPlayer(Player player) {
        this.killPlayer(player, true, false, null);
        player.sendTitle(Utils.formatText("&c&lVOTED OUT"),
                Utils.formatText("You have been evicted by the &a&lVillagers"), 20, 80, 20);
    }

    /**
     * Called after important events like meetings, kills, disconnects, etc.
     * If there is a win condition that's met, this will handle it and end the game
     */
    public void checkWinCondition() {
        Utils.verboseLog("Checking win condition.");
        WinCondition condition = this.getWinCondition();
        if (condition == WinCondition.NONE) {
            this.setGameStatus(Status.PLAYING);
            return;
        }

        if (condition == WinCondition.MOBS_VOTED_OUT) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (this.isPlayerVillager(player)) {
                    player.sendTitle(Utils.formatText("&a&lVICTORY"),
                            Utils.formatText("All &c&lMobs&r have been evicted."), 20, 80, 20);
                } else {
                    player.sendTitle(Utils.formatText("&c&lDEFEAT"),
                            Utils.formatText("All &c&lMobs&r have been evicted."), 20, 80, 20);
                }
            }
        } else if (condition == WinCondition.MOB_OVERWHELM) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!this.isPlayerVillager(player)) {
                    player.sendTitle(Utils.formatText("&a&lVICTORY"),
                            Utils.formatText("The &a&lVillagers&r have been overwhelmed."), 20, 80, 20);
                } else {
                    player.sendTitle(Utils.formatText("&c&lDEFEAT"),
                            Utils.formatText("The &a&lVillagers&r have been overwhelmed."), 20, 80, 20);
                }
            }
        } else if (condition == WinCondition.TASK_COMPLETION) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (this.isPlayerVillager(player)) {
                    player.sendTitle(Utils.formatText("&a&lVICTORY"),
                            Utils.formatText("All &c&ltasks&r have been completed."), 20, 80, 20);
                } else {
                    player.sendTitle(Utils.formatText("&c&lDEFEAT"),
                            Utils.formatText("All &c&ltasks&r have been completed."), 20, 80, 20);
                }
            }
        }

        this.village.getServer().getScheduler().scheduleSyncDelayedTask(village, new Runnable() {
            @Override
            public void run() {
                end(null);
            }
        }, 100L);
    }

    /**
     * @return The current amount of Villagers that are alive
     */
    public int getAliveVillagerCount() {
        int total = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.isPlayerDead(player) || !this.isPlayerVillager(player)) {
                continue;
            }

            total++;
        }

        return total;
    }

    public int getAliveMobCount() {
        int total = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.isPlayerDead(player) || this.isPlayerVillager(player)) {
                continue;
            }

            total++;
        }

        return total;
    }

    /**
     * Starts a discussion by teleporting everyone to the meeting location
     * 
     * @param caller Who initiated the meeting
     * @param reason Why the meeting was started
     */
    public void startDiscussion(Player caller, String reason) {
        this.setGameStatus(Status.DISCUSSION);
        Bukkit.broadcastMessage(
                Utils.formatText("&b&l[MEETING]&r&b Discussion started. Called by &a&l" + caller.getName()));
        Utils.verbosePlayerLog(caller, "Started meeting.\n  -> reason = " + reason);

        double increment = (Math.PI * 2) / Bukkit.getOnlinePlayers().size();
        long distance = 2;
        int i = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.closeInventory(); // clear any inventory tasks
            p.playSound(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 5, 1);
            p.sendTitle(Utils.formatText("&b&lMEETING"),
                    Utils.formatText("Called by &a&l" + caller.getName() + "&r. " + reason), 20,
                    80, 20);

            double x = Math.cos(increment * i) * distance;
            double z = Math.sin(increment * i) * distance;
            Location playerLoc = this.getMeetingLocation().clone();
            playerLoc.add(x, 0, z);
            p.teleport(playerLoc);
            i++;

            p.setAllowFlight(true);
            p.setFlying(true);
        }
    }

    /**
     * Starts the meeting section of a discussion, players are able to evict mobs.
     * This should only be called after startDiscussion
     * as it doesn't teleport players around the talbe
     */
    public void startVoting() {
        this.setGameStatus(Status.VOTING);
        Bukkit.broadcastMessage(Utils.formatText("&b&l[MEETING]&r&b Voting started."));
        Utils.verboseLog("Started voting.");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 5, 1);
            if (!this.isPlayerDead(p)) {
                p.setAllowFlight(false);
                p.setFlying(false);
            }
        }
    }

    /**
     * Ends a meeting/discussion. Will handle player votes. Resets killer cooldowns
     * to default.
     * Players are shown results for 5 seconds and then let free
     */
    public void endMeeting() {
        // announce end and votes
        this.setGameStatus(Status.ENDING_MEETING);
        Utils.verboseLog("Meeting ending.");

        if (this.getVotes().size() <= 0) {
            Bukkit.broadcastMessage(Utils.formatText("&b&l[MEETING]&r&b Meeting over. No one voted."));
        } else {
            Bukkit.broadcastMessage(Utils.formatText("&b&l[MEETING]&r&b Meeting over. Votes:"));
            boolean tie = false;
            Map.Entry<UUID, ArrayList<UUID>> highest = null;
            for (Map.Entry<UUID, ArrayList<UUID>> entry : this.getVotes().entrySet()) {
                UUID uuid = entry.getKey();
                if (uuid.equals(Game.getSkipVoteUUID())) {
                    Bukkit.broadcastMessage(
                            Utils.formatText("&d - &lSKIP&r&d: &9&l" + entry.getValue().size()));
                    Utils.verboseLog("Skip Votes = " + entry.getValue().size());
                } else {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        // disconnected or something else
                        continue;
                    }

                    // reset killer cooldowns
                    if (!this.isPlayerVillager(player)) {
                        this.addKillCooldown(player.getUniqueId(), this.getKillCooldown());
                    }

                    Bukkit.broadcastMessage(
                            Utils.formatText("&3 - &l" + player.getName() + "&r&3: &b&l" + entry.getValue().size()));
                    Utils.verbosePlayerLog(player, "Votes = " + entry.getValue().size());
                }

                if (highest == null) {
                    highest = entry;
                    continue;
                }

                if (highest.getValue().size() < entry.getValue().size()) {
                    highest = entry;
                    tie = false;
                } else if (highest.getValue().size() == entry.getValue().size()) {
                    tie = true;
                }
            }

            if (highest != null && !tie) {
                if (highest.getKey().equals(Game.getSkipVoteUUID())) {
                    Bukkit.broadcastMessage(
                            Utils.formatText("&b&l[MEETING] Voting has been skipped."));
                    Utils.verboseLog("Meeting voting skipped.");
                } else {
                    Player votedOut = Bukkit.getPlayer(highest.getKey());
                    if (votedOut != null) {
                        Bukkit.broadcastMessage(
                                Utils.formatText("&b&l[MEETING] " + votedOut.getName() + "&r&b has been voted out."));
                        Utils.verbosePlayerLog(votedOut,
                                "Has been voted out.\n  -> votes = " + highest.getValue().size());

                        this.evictPlayer(votedOut);
                    }
                }
            }

            this.clearVotes();
        }

        this.village.getServer().getScheduler().scheduleSyncDelayedTask(village, new Runnable() {
            @Override
            public void run() {
                checkWinCondition();
            }
        }, 100L);
    }

    /**
     * Attempts to start a game of Village.
     * Can fail if locations aren't setup, no players, etc.
     */
    public void start(@Nullable CommandSender caller) {
        if (caller != null) {
            caller.sendMessage(Utils.formatText("&e&l[VILLAGE]&r&e Starting Village game."));
        }

        // players check
        int onlinePlayerCount = Bukkit.getOnlinePlayers().size();
        if (onlinePlayerCount <= 0) {
            Utils.verboseLog("Failed to start Village game. No online players.");
            if (caller != null) {
                caller.sendMessage(Utils.formatText("&c&l[VILLAGE]&r&c Failed: No online players."));
            }
            return;
        }

        // location checks
        if (this.getSpawnLocation() == null || this.getMeetingLocation() == null) {
            Utils.verboseLog("Failed to start Village game. Spawn or meeting location are not setup.");
            if (caller != null) {
                caller.sendMessage(
                        Utils.formatText("&c&l[VILLAGE]&r&c Failed: Meeting and or spawn locations aren't setup."));
            }
            return;
        }

        if (this.isGameInProgress()) {
            Utils.verboseLog("Failed to start Village game. Game already in progress.");
            if (caller != null) {
                caller.sendMessage(Utils.formatText("&c&l[VILLAGE]&r&c Failed: On-going game."));
            }
            return;
        }

        if (Storage.config.getBoolean("tasks.doThemAll", true)) {
            this.setTasksPerVillager(this.getVillagerTasks().size());
        }

        this.setGameStatus(Status.PRE_GAME);
        Utils.verboseLog("Starting Village game. In pre-game.");

        // ready players
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);
        List<Player> selected = players.subList(0, Math.min(getMobCount(), players.size()));
        for (Player p : players) {
            p.playSound(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 5, 1);
            if (selected.contains(p)) {
                setPlayerToMob(p);
            } else {
                setPlayerToVillager(p);
            }
        }

        GameLoop loop = new GameLoop(this);
        this.gameLoop = loop;
        this.gameLoopID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.village, loop, 1L, 1L);
        Utils.verboseLog("Starting Village game. Game loop started.");

        this.setGameStatus(Status.PLAYING);
        Utils.verboseLog("Started Village game. Playing.");
        if (caller != null) {
            caller.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Started Village game."));
        }
    }

    /**
     * Attempts to end a Village game. Will fail if no gaming in progress.
     * This does not display the results of the match, as in who won. Only cleans up
     * data and players. Doesn't handle win conditions. Status will be set to
     * NO_GAME at the end.
     */
    public void end(@Nullable CommandSender caller) {
        if (caller != null) {
            caller.sendMessage(Utils.formatText("&e&l[VILLAGE]&r&e Ending Village game."));
        }

        if (!this.isGameInProgress()) {
            Utils.verboseLog("Failed to end Village game. No game in progress.");
            if (caller != null) {
                caller.sendMessage(Utils.formatText("&c&l[VILLAGE]&r&c Failed: No game on-going."));
            }
            return;
        }

        this.setGameStatus(Status.POST_GAME);
        Utils.verboseLog("Ending Village game. Cleaning up.");

        this.chatTaskExpectedResults.clear();
        this.craftTaskExpectedResults.clear();
        Utils.verboseLog("Cleared expected result maps.");

        for (VillagerTask task : this.getVillagerTasks()) {
            task.getAssignedVillagers().clear();
        }
        Utils.verboseLog("Cleared task data.");

        // cleanup players
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player p : players) {
            p.getInventory().clear();
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            p.setHealth(20);
            p.setSaturation(20);
            p.setGameMode(GameMode.SURVIVAL);
            p.teleport(this.getSpawnLocation());
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

            p.setFlying(false);
            p.setAllowFlight(false);

            p.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, true);
        }
        Utils.verboseLog("Ending Village game. Cleaned up players.");

        // cleanup bodies
        for (Entity entity : players.get(0).getWorld().getEntities()) {
            if (entity.getType() != EntityType.ARMOR_STAND) {
                continue;
            }

            ArmorStand stand = (ArmorStand) entity;
            if (!stand.getCustomName().contains(Utils.formatText("&c&lBODY"))) {
                continue;
            }

            stand.teleport(new Location(stand.getWorld(), 0, 0, 0));
            stand.remove();
        }

        this.playerRoles.clear();
        this.killCooldowns.clear();
        this.abilityCooldowns.clear();
        this.deadPlayers.clear();
        this.buttonUses.clear();
        this.votes.clear();
        this.chatTaskExpectedResults.clear();
        this.craftTaskExpectedResults.clear();
        this.completedAllTasks = false;

        this.gameLoop = null;
        Bukkit.getScheduler().cancelTask(gameLoopID);
        this.gameLoopID = -1;
        Utils.verboseLog("Ending Village game. Game loop cleaned up.");

        this.setGameStatus(Status.NO_GAME);
        Utils.verboseLog("Ended Village game. Finished.");
        if (caller != null) {
            caller.sendMessage(Utils.formatText("&a&l[VILLAGE]&r&a Ended Village game."));
        }
    }

    /**
     * Attempts to find the medscan task by player
     * 
     * @param player The player to check
     * @return The medical scan task object or null
     */
    public VillagerTask getMedicalScanByPlayer(Player player) {
        for (VillagerTask task : this.getVillagerTasks()) {
            if (task.getScanning() != null && task.getScanning().equals(player.getUniqueId())) {
                return task;
            }
        }

        return null;
    }

    public VillagerTask getManifoldByPlayer(Player player) {
        for (VillagerTask task : this.getVillagerTasks()) {
            if (task.getManifoldTaskExpectedNexts().containsKey(player.getUniqueId())) {
                return task;
            }
        }

        return null;
    }

    public VillagerTask getCleanVentTaskByPlayer(Player player) {
        for (VillagerTask task : this.getVillagerTasks()) {
            if (task.getCleanVentItemPositions().containsKey(player.getUniqueId())) {
                return task;
            }
        }

        return null;
    }

    public void setPlayerRole(UUID uuid, Role role) {
        this.playerRoles.put(uuid, role);
    }

    public Role getPlayerRole(UUID uuid) {
        return this.playerRoles.get(uuid);
    }

    public HashMap<UUID, Role> getPlayerRoles() {
        return this.playerRoles;
    }

    public void giveVillagerMobCompass(Player player) {
        // compasses will be updated in game loop to point
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        meta.setDisplayName(Utils.formatText("&c&lMob&r&c Locator"));
        meta.setUnbreakable(true);
        compass.setItemMeta(meta);

        player.getInventory().addItem(compass);
    }

    /**
     * Will also give Villagers compasses upon completion
     * 
     * @return A value between 0-100
     */
    public double getCompletedTaskPercent() {
        int numerator = this.getAmountOfCompletedTasks();
        double denominator = Math.max(this.getVillagerCount(), 1) * Math.max(this.getTasksPerVillager(), 1);
        double result = Math.floor((numerator / denominator) * 100);

        if (result >= 100 && !this.hasCompletedAllTasks()) {
            this.setCompletedAllTasks(true);
            Utils.verboseLog("Villagers have completed 100% of tasks. Giving compass.");

            if (this.canWinOnTasks()) {
                this.checkWinCondition();
            }

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (this.isPlayerVillager(p)) {
                    giveVillagerMobCompass(p);
                }
            }
        }

        return result;
    }

    /**
     * Returns a 0 UUID that is used for skip voting
     * 
     * @return A 0 UUID object
     */
    public static UUID getSkipVoteUUID() {
        return new UUID(0L, 0L);
    }

    /**
     * Attempt to get the vent at the given location
     * 
     * @param location Where to check for a vent
     * @return The vent object or null if not found
     */
    public Vent getVentAtLocation(Location location) {
        for (Vent v : this.getMobVents()) {
            Location l = v.getBlock().getLocation();
            if (l.equals(location)) {
                return v;
            }
        }

        return null;
    }

    /**
     * Attempt to get the villager task at the given location
     * 
     * @param location Where to check for a task
     * @return The task object or null if not found
     */
    public VillagerTask getTaskAtLocation(Location location) {
        for (VillagerTask t : this.getVillagerTasks()) {
            Location l = t.getBlock().getLocation();
            if (l.equals(location)) {
                return t;
            }
        }

        return null;
    }

    /**
     * Checks if the player is apart of the villagers
     * 
     * @param player The player to check
     * @return True if the player is apart of the villagers (e.g. villager, medic,
     *         detective), false otherwise
     */
    public boolean isPlayerVillager(Player player) {
        Role role = this.playerRoles.get(player.getUniqueId());
        return (role == null || role == Role.VILLAGER || role == Role.DETECTIVE || role == Role.MEDIC);
    }

    /**
     * Attempts to find the current vent the mob is in
     * 
     * @param mob Who to check
     * @return The vent object if they are inside it, otherwise null
     */
    public Vent getVentMobIsIn(Player mob) {
        for (Vent vent : this.getMobVents()) {
            if (vent.getMobsInside().contains(mob.getUniqueId())) {
                return vent;
            }
        }

        return null;
    }

    /**
     * Set a players vote to whoToVoteFor. Will handle if they voted for them
     * previously or someone else previously.
     * 
     * @param whoToVoteFor The player who is getting voted
     * @param voter        The player who is voting
     */
    public void voteForPlayer(Player whoToVoteFor, Player voter) {
        UUID playerUUID = voter.getUniqueId();
        UUID targetUUID = whoToVoteFor.getUniqueId();

        if (this.hasAlreadyVotedForThisPlayer(targetUUID, playerUUID)) {
            this.clearPreviousPlayerVotes(playerUUID);
            Utils.verbosePlayerLog(voter,
                    "Removed their vote from player " + whoToVoteFor.getName() + " (" + targetUUID + ")");
            voter.sendMessage(
                    Utils.formatText("&e&l[MEETING]&r&e You removed your vote from &l" + whoToVoteFor.getName()));
        } else {
            this.clearPreviousPlayerVotes(playerUUID);
            Utils.verbosePlayerLog(voter, "Voted for player " + whoToVoteFor.getName() + " (" + targetUUID + ")");
            this.addVote(targetUUID, playerUUID);
            voter.sendMessage(Utils.formatText("&e&l[MEETING]&r&e You voted for &l" + whoToVoteFor.getName()));
        }
    }

    /**
     * Sets a players vote to 0000, the skip UUID. Should this be something else,
     * maybe.
     * 
     * @param voter Who the vote skip
     */
    public void voteForSkip(Player voter) {
        UUID playerUUID = voter.getUniqueId();

        if (this.hasAlreadyVotedForThisPlayer(Game.getSkipVoteUUID(), playerUUID)) {
            this.clearPreviousPlayerVotes(playerUUID);
            Utils.verbosePlayerLog(voter,
                    "Removed their skip vote");
            voter.sendMessage(
                    Utils.formatText("&e&l[MEETING]&r&e You removed your skip vote"));
        } else {
            this.clearPreviousPlayerVotes(playerUUID);
            Utils.verbosePlayerLog(voter, "Voted for skip)");
            this.addVote(Game.getSkipVoteUUID(), playerUUID);
            voter.sendMessage(Utils.formatText("&e&l[MEETING]&r&e You voted for skip"));
        }
    }

    /**
     * @return The current status of the game
     */
    public Status getGameStatus() {
        return this.status;
    }

    /**
     * Set the game status
     * 
     * @param newStatus What status should the game be put in to
     */
    public void setGameStatus(Status newStatus) {
        this.status = newStatus;

        // reset ticks in current state
        if (this.isGameInProgress() && this.gameLoop != null) {
            this.gameLoop.setTicksInCurrentState(0);
        }
    }

    public boolean isGameInProgress() {
        return this.status != Status.NO_GAME;
    }

    public boolean inDiscussion() {
        return this.status == Status.DISCUSSION;
    }

    public boolean ableToVote() {
        return this.status == Status.VOTING;
    }

    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    public void setSpawnLocation(Location location) {
        this.spawnLocation = location;
    }

    public void setAllowTaskWin(boolean value) {
        this.allowTaskWin = value;
    }

    /**
     * @return True if Villagers are able to win upon 100%ing tasks
     */
    public boolean canWinOnTasks() {
        return this.allowTaskWin;
    }

    public Location getMeetingLocation() {
        return this.meetingLocation;
    }

    /**
     * Sets the meeting location. Players will spawn around this location.
     * A "button" will also spawn here and act as an emergency meeting button.
     * 
     * @param location Where to set the center of the meeting location
     */
    public void setMeetingLocation(Location location) {
        this.meetingLocation = location;

        // move button
        ArmorStand stand = this.getMeetingButton();
        Location standLoc = location.clone();
        standLoc.add(0, -1.7f, 0);
        if (stand != null) {
            stand.remove();
        }

        stand = (ArmorStand) location.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
        stand.setCustomNameVisible(true);
        stand.setCustomName(Utils.formatText("&c&lEMERGENCY MEETING"));
        stand.teleport(standLoc);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setInvisible(true);
        stand.setMarker(false);
        stand.setSmall(false);
        stand.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));

        this.setMeetingButton(stand);
    }

    /**
     * @return The amount of time players must wait before using the button in ticks
     *         (1sec = 20ticks)
     */
    public long getMeetingButtonCooldown() {
        return this.meetingButtonCooldown;
    }

    /**
     * @param ticks The new cooldown in ticks
     */
    public void setMeetingButtonCooldown(long ticks) {
        this.meetingButtonCooldown = ticks;
    }

    public HashMap<UUID, Integer> getMeetingUses() {
        return this.buttonUses;
    }

    public int getMaxMeetingButtonUses() {
        return this.maxButtonUses;
    }

    public GameLoop getGameLoop() {
        return this.gameLoop;
    }

    public ArmorStand getMeetingButton() {
        return this.meetingButton;
    }

    public void setMeetingButton(ArmorStand armorStand) {
        this.meetingButton = armorStand;
    }

    public ArrayList<VillagerTask> getVillagerTasks() {
        return this.villagerTasks;
    }

    public void setVillagerTasks(ArrayList<VillagerTask> tasks) {
        this.villagerTasks = tasks;
    }

    public void addVillagerTask(VillagerTask task) {
        this.villagerTasks.add(task);
    }

    public void removeVillagerTask(VillagerTask task) {
        this.villagerTasks.remove(task);
    }

    public ArrayList<Vent> getMobVents() {
        return this.mobVents;
    }

    public void setMobVents(ArrayList<Vent> mobVents) {
        this.mobVents = mobVents;
    }

    public void addMobVent(Vent mobVent) {
        this.mobVents.add(mobVent);
    }

    public void removeMobVent(Vent mobVent) {
        this.mobVents.remove(mobVent);
    }

    /**
     * @param toRevive Which player should be revived.
     * @param location The location the player should be teleported to. If null is
     *                 provided, it will just revive where they are.
     */
    public void revivePlayer(Player toRevive, @Nullable Location location) {
        toRevive.setGameMode(GameMode.ADVENTURE);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(village, toRevive);
        }

        if (location != null) {
            toRevive.teleport(location);
        }

        this.removeDeadPlayer(toRevive.getUniqueId());
    }

    /**
     * 
     * @param toKill     Which player should die.
     * @param silent     Should the player be sent a title
     * @param createBody Should a body be spawned
     * @param killer     Who killed this player. If null is provided, it will just
     *                   force
     *                   them to spectate
     */
    public void killPlayer(Player toKill, boolean silent, boolean createBody, @Nullable Player killer) {
        toKill.setGameMode(GameMode.ADVENTURE);
        this.addDeadPlayer(toKill.getUniqueId());
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (this.isPlayerDead(p)) {
                p.showPlayer(village, toKill);
                continue;
            }

            p.hidePlayer(village, toKill);
        }

        if (createBody) {
            Location damagedLocation = toKill.getLocation().add(0, 1, 0);
            ArmorStand armorStand = (ArmorStand) toKill.getWorld().spawnEntity(damagedLocation,
                    EntityType.ARMOR_STAND);
            armorStand.setInvulnerable(true);
            armorStand.setCustomName(Utils.formatText("&c&lBODY&r | &c&l" + toKill.getName()));
            armorStand.setCustomNameVisible(true);

            EntityEquipment equipment = armorStand.getEquipment();
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            headMeta.setOwnerProfile(toKill.getPlayerProfile());
            head.setItemMeta(headMeta);

            equipment.setHelmet(head);
            equipment.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            equipment.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
            equipment.setBoots(new ItemStack(Material.LEATHER_BOOTS));
        }

        if (killer != null && !silent) {
            toKill.playSound(toKill.getLocation(), Sound.ENTITY_WITHER_SPAWN, 5, 1);

            Role killerRole = this.getPlayerRole(killer.getUniqueId());
            if (killerRole == Role.DETECTIVE) {
                toKill.sendTitle(Utils.formatText("&c&lKILLED"),
                        Utils.formatText("Detective &a&l" + killer.getName() + "&r has killed you"), 20, 80, 20);
            } else {
                toKill.sendTitle(Utils.formatText("&c&lKILLED"),
                        Utils.formatText("You have been killed by &c&l" + killer.getName()), 20, 80, 20);
            }

            this.addKillCooldown(killer.getUniqueId(), this.getKillCooldown());
            this.addTimeOfDeath(toKill.getUniqueId(), this.getGameLoop().getTicksSinceStart());
            this.addDeadPlayer(toKill.getUniqueId());
            this.checkWinCondition();
        }
    }

    /**
     * Will attempt to save the current game object to the current world's config
     * section
     * 
     * @return True if saved, false if failed
     */
    public boolean saveCurrentGameConfig(String id) {
        try {
            Utils.verboseLog("Saving world config...");
            String root = "saved." + id + ".";

            // reset
            Storage.worldsData.set("saved." + id, null);

            Utils.verboseLog("Saving gameplay values...");
            Storage.worldsData.set(root + "tasksNeeded", this.getTasksPerVillager());
            Storage.worldsData.set(root + "killCooldown", this.getKillCooldown());
            Storage.worldsData.set(root + "discussionTime", this.getDiscussionTime());
            Storage.worldsData.set(root + "votingTime", this.getVotingTime());
            Storage.worldsData.set(root + "buttonTime", this.getMeetingButtonCooldown());
            Storage.worldsData.set(root + "maxButtons", this.getMaxMeetingButtonUses());
            Storage.worldsData.set(root + "taskWin", this.canWinOnTasks());
            Storage.worldsData.set(root + "abilityCooldown", this.getAbilityCooldown());
            Storage.worldsData.set(root + "blind", this.getBlindAmount());
            Utils.verboseLog("Saved gameplay values!");

            int i = 0;
            String tasks = root + "tasks.";
            for (VillagerTask task : this.getVillagerTasks()) {
                Storage.worldsData.set(tasks + "task" + i + ".type", task.getTaskType().toString());

                Location loc = task.getBlock().getLocation();
                Storage.worldsData.set(tasks + "task" + i + ".x", loc.getBlockX());
                Storage.worldsData.set(tasks + "task" + i + ".y", loc.getBlockY());
                Storage.worldsData.set(tasks + "task" + i + ".z", loc.getBlockZ());
                i++;

                Utils.verboseLog("Saved task.");
            }

            i = 0;
            String vents = root + "vents.";
            for (Vent vent : this.getMobVents()) {
                Location loc = vent.getBlock().getLocation();
                Storage.worldsData.set(vents + i + ".x", loc.getX());
                Storage.worldsData.set(vents + i + ".y", loc.getY());
                Storage.worldsData.set(vents + i + ".z", loc.getZ());

                int j = 0;
                for (Vent connectedVent : vent.getConnectedVents()) {
                    Location l = connectedVent.getBlock().getLocation();
                    Storage.worldsData.set(vents + i + ".vents." + j + ".x", l.getX());
                    Storage.worldsData.set(vents + i + ".vents." + j + ".y", l.getY());
                    Storage.worldsData.set(vents + i + ".vents." + j + ".z", l.getZ());
                }

                Utils.verboseLog("Saved vent.");
                i++;
            }

            // meeting location
            if (this.getMeetingLocation() != null) {
                String meetingLoc = root + "meeting.";
                Storage.worldsData.set(meetingLoc + "x", this.getMeetingLocation().getX());
                Storage.worldsData.set(meetingLoc + "y", this.getMeetingLocation().getY());
                Storage.worldsData.set(meetingLoc + "z", this.getMeetingLocation().getZ());
                Utils.verboseLog("Saved meeting location.");
            }

            // spawn location
            if (this.getSpawnLocation() != null) {
                String spawnLoc = root + "spawn.";
                Storage.worldsData.set(spawnLoc + "x", this.getSpawnLocation().getX());
                Storage.worldsData.set(spawnLoc + "y", this.getSpawnLocation().getY());
                Storage.worldsData.set(spawnLoc + "z", this.getSpawnLocation().getZ());
                Utils.verboseLog("Saved spawn location.");
            }

            Storage.worldsData.save(Storage.worldsFile);
            Utils.verboseLog("Saved world config!");
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }

        return true;
    }

    /**
     * Attempts to load a saved config by its ID
     * 
     * @param world The world to load the config in
     * @param id    The id of the config
     * @return True if successful, false if otherwise
     */
    public boolean loadFromSaveID(World world, String id) {
        ConfigurationSection section = Storage.worldsData.getConfigurationSection("saved." + id);
        if (section == null) {
            return false;
        }

        if (this.getVillagerTasks() != null && this.getVillagerTasks().size() >= 1) {
            for (VillagerTask task : this.getVillagerTasks()) {
                if (task == null) {
                    continue;
                }

                task.destroy();
                removeVillagerTask(task);
            }
        }

        if (this.getVillagerTasks() != null && this.getMobVents().size() >= 1) {
            for (Vent vent : this.getMobVents()) {
                if (vent == null) {
                    continue;
                }

                vent.destroy();
                removeMobVent(vent);
            }
        }

        Utils.verboseLog("Loading gameplay values...");
        this.setTasksPerVillager(section.getInt("tasksNeeded", this.getTasksPerVillager()));
        this.setKillCooldown(section.getLong("killCooldown", this.getKillCooldown()));
        this.setDiscussionTime(section.getLong("discussionTime", this.getDiscussionTime()));
        this.setVotingTime(section.getLong("votingTime", this.getVotingTime()));
        this.setMeetingButtonCooldown(section.getLong("buttonTime", this.getMeetingButtonCooldown()));
        this.setMaxMeetingButtonUses(section.getInt("maxButtons", this.getMaxMeetingButtonUses()));
        this.setAllowTaskWin(section.getBoolean("taskWin", this.canWinOnTasks()));
        this.setAbilityCooldown(section.getLong("abilityCooldown", 500));
        this.setBlindAmount(section.getInt("blind", 1));
        Utils.verboseLog("Loaded gameplay values!");

        this.setSpawnLocation(new Location(
                world,
                section.getDouble("spawn.x", 0),
                section.getDouble("spawn.y", 0), section.getDouble("spawn.z", 0)));
        Utils.verboseLog("Loaded spawn location!");

        this.setMeetingLocation(new Location(
                world,
                section.getDouble("meeting.x", 0),
                section.getDouble("meeting.y", 0), section.getDouble("meeting.z", 0)));
        Utils.verboseLog("Loaded meeting location!");

        ConfigurationSection savedTasks = section.getConfigurationSection("tasks");
        if (savedTasks != null) {
            Utils.verboseLog("Loading saved tasks...");
            for (String key : savedTasks.getKeys(false)) {
                ConfigurationSection savedTask = savedTasks.getConfigurationSection(key);
                if (savedTask == null) {
                    continue;
                }

                Block block = world.getBlockAt((int) savedTask.getDouble("x", 0), (int) savedTask.getDouble("y", 0),
                        (int) savedTask.getDouble("z", 0));
                VillagerTask task = new VillagerTask(block);
                task.setTaskType(VillagerTaskType.valueOf(savedTask.getString("type", "CRAFT")));
                this.addVillagerTask(task);

                Utils.verboseLog("Loaded saved " + savedTask.getString("type", "CRAFT") + " task at position "
                        + block.getLocation().getBlockX() + ", " + block.getLocation().getBlockY() + ", "
                        + block.getLocation().getBlockZ());
            }
            Utils.formatText("Loaded saved tasks!");
        }

        ConfigurationSection savedVents = section.getConfigurationSection("vents");
        if (savedVents != null) {
            this.getMobVents().clear();
            Utils.verboseLog("Loading saved vents...");

            // first load all vents
            for (String k : savedVents.getKeys(false)) {
                ConfigurationSection savedVent = savedVents.getConfigurationSection(k);
                if (savedVent == null) {
                    continue;
                }

                Block block = world.getBlockAt((int) savedVent.getDouble("x"), (int) savedVent.getDouble("y"),
                        (int) savedVent.getDouble("z"));
                block.setType(Material.IRON_TRAPDOOR);
                Vent vent = new Vent(block);
                this.addMobVent(vent);

                Utils.verboseLog("Loaded saved vent at position " + block.getLocation().getBlockX() + ", "
                        + block.getLocation().getBlockY() + ", " + block.getLocation().getBlockZ());
            }

            Utils.verboseLog("Loaded saved vents!");

            Utils.verboseLog("Loading vent systems...");
            // now link all vents

            for (String k : savedVents.getKeys(false)) {
                ConfigurationSection savedVent = savedVents.getConfigurationSection(k);
                if (savedVent == null) {
                    continue;
                }

                ConfigurationSection linkedVents = savedVent.getConfigurationSection("vents");
                if (linkedVents == null) {
                    continue;
                }

                Block block = world.getBlockAt((int) savedVent.getDouble("x"), (int) savedVent.getDouble("y"),
                        (int) savedVent.getDouble("z"));
                Vent vent = this.getVentAtLocation(block.getLocation());
                if (vent == null) {
                    continue;
                }

                // go through each connected vent
                for (String j : linkedVents.getKeys(false)) {
                    ConfigurationSection pos = linkedVents.getConfigurationSection(j);
                    if (pos == null) {
                        continue;
                    }

                    Location loc = new Location(world, (int) pos.getDouble("x"), (int) pos.getDouble("y"),
                            (int) pos.getDouble("z"));
                    Vent linkVent = this.getVentAtLocation(loc);
                    if (linkVent == null) {
                        continue;
                    }

                    vent.addConnectedVent(linkVent);
                    linkVent.addConnectedVent(vent);
                }
            }

            Utils.verboseLog("Loaded saved vent systems!");
        }

        return true;
    }

}
