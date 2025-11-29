package ca.ckay9.Game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ca.ckay9.Storage;
import ca.ckay9.Utils;
import ca.ckay9.Village;

public class Game {
    private Status status; // current game status
    private Location spawnLocation; // where players are teleported to on game start
    private Location meetingLocation; // the center of meetings, players spawn around this location
    private ArrayList<VillagerTask> villagerTasks; // all created tasks
    private HashMap<UUID, Role> playerRoles; // villager or mob => sub roles
    private HashMap<UUID, ChatTaskProgress> chatTaskExpectedResults; // used for trivia and math tasks
    private HashMap<UUID, CraftTaskProgress> craftTaskExpectedResults; // used for crafting tasks
    private ArrayList<Vent> mobVents; // all created mob vents
    private Village village; // parent class
    private int tasksPerVillager; // how many tasks each villager has
    private int villagerCount; // how many villagers there are, this is also used to calculate mobCount (see
                               // getMobCount() and setMobCount())
    private GameLoop gameLoop; // the game loop instance
    private int gameLoopID; // ID to bukkit runnable

    public Game(Village village) {
        this.village = village;
        this.status = Status.NO_GAME;
        this.villagerTasks = new ArrayList<>();
        this.mobVents = new ArrayList<>();
        this.playerRoles = new HashMap<>();
        this.spawnLocation = null;
        this.meetingLocation = null;
        this.chatTaskExpectedResults = new HashMap<>();
        this.craftTaskExpectedResults = new HashMap<>();
        this.villagerCount = 1;
        this.tasksPerVillager = 1;
        this.gameLoop = null;
        this.gameLoopID = -1;

        PluginManager manager = village.getServer().getPluginManager();
        manager.registerEvents(new VentInteract(this), village);
        manager.registerEvents(new VillagerTaskInteract(this), village);
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

    public HashMap<UUID, ChatTaskProgress> getChatTaskExpectedResults() {
        return this.chatTaskExpectedResults;
    }

    public void setChatTaskExpectedResults(HashMap<UUID, ChatTaskProgress> results) {
        this.chatTaskExpectedResults = results;
    }

    public void addChatTaskExpectedResult(UUID uuid, ChatTaskProgress progress) {
        this.chatTaskExpectedResults.put(uuid, progress);
    }

    public int getVillagerCount() {
        return this.villagerCount;
    }

    public void setVillagerCount(int value) {
        this.villagerCount = value;
    }

    public int getMobCount() {
        return Bukkit.getOnlinePlayers().size() - this.getVillagerCount();
    }

    /**
     * This works by changing villager count to players - value
     * 
     * @param value How many mobs should there be
     */
    public void setMobCount(int value) {
        this.villagerCount = Bukkit.getOnlinePlayers().size() - value;
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
     */
    public void setTasksPerVillager(int value) {
        int maxValue = this.getVillagerTasks().size();
        if (value > maxValue) {
            Utils.verboseLog("Attempted to set tasksPerVillager above max value.\n  -> max = " + maxValue
                    + "\n  -> attempted value = " + value);
            return;
        }

        this.tasksPerVillager = value;
    }

    /**
     * 
     * @return The fraction of tasks that have been completed. Double between 0 and
     *         1
     */
    public double getTaskCompletion() {
        return this.getAmountOfCompletedTasks() / (tasksPerVillager * villagerCount);
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

    public void setPlayerToVillager(Player player) {
        player.getInventory().clear();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setHealth(20);
        player.setSaturation(20);

        Utils.verbosePlayerLog(player, "Changed to villager.");
        Collections.shuffle(this.getVillagerTasks());
        List<VillagerTask> selectedTasks = this.getVillagerTasks().subList(0, this.getTasksPerVillager());
        for (VillagerTask task : selectedTasks) {
            task.addAssignedVillager(player.getUniqueId());
        }

        player.sendTitle(Utils.formatText("&a&lVILLAGER"),
                Utils.formatText("You are a &a&lVillager&r. Complete your tasks and evict the &c&lMobs&r!"), 20,
                80, 20);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10_000_000, 255, false, false, false));
        this.setPlayerRole(player.getUniqueId(), Role.VILLAGER);

        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(this.getSpawnLocation());
    }

    public void setPlayerToMob(Player player) {
        player.getInventory().clear();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setHealth(20);
        player.setSaturation(20);

        Utils.verbosePlayerLog(player, "Changed to mob.");

        player.sendTitle(Utils.formatText("&c&lMOB"),
                Utils.formatText("You are a &c&lMob&r. Kill all &a&lVillagers&r to win!"), 20, 80, 20);
        this.setPlayerRole(player.getUniqueId(), Role.MOB);

        ItemStack knife = new ItemStack(Material.NETHERITE_SWORD, 1);
        ItemMeta knifeMeta = knife.getItemMeta();
        knifeMeta.setDisplayName(Utils.formatText("&lKNIFE"));
        knife.setItemMeta(knifeMeta);

        player.getInventory().addItem(knife);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(this.getSpawnLocation());
    }

    /**
     * Attempts to start a game of Village.
     * Can fail if locations aren't setup, no players, etc.
     */
    public void start() {
        // players check
        int onlinePlayerCount = Bukkit.getOnlinePlayers().size();
        if (onlinePlayerCount <= 0) {
            Utils.verboseLog("Failed to start Village game. No online players.");
            return;
        }

        // location checks
        if (this.getSpawnLocation() == null || this.getMeetingLocation() == null) {
            Utils.verboseLog("Failed to start Village game. Spawn or meeting location are not setup.");
            return;
        }

        if (this.isGameInProgress()) {
            Utils.verboseLog("Failed to start Village game. Game already in progress.");
            return;
        }

        this.setVillagerCount(onlinePlayerCount);
        int mobCount = Math.max(1, Math.floorDiv(onlinePlayerCount, 4));
        this.setMobCount(mobCount); // will change villager count

        if (Storage.config.getBoolean("tasks.doThemAll", true)) {
            this.setTasksPerVillager(this.getVillagerTasks().size());
        } else {
            // TODO: get a provided amount.
        }

        this.setGameStatus(Status.PRE_GAME);
        Utils.verboseLog("Starting Village game. In pre-game.");

        // ready players
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);
        List<Player> selected = players.subList(0, Math.min(getMobCount(), players.size()));
        for (Player p : players) {
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
    }

    /**
     * Attempts to end a Village game. Will fail if no gaming in progress.
     * This does not display the results of the match, as in who won. Only cleans up
     * data and players. Status will be set to NO_GAME at the end.
     */
    public void end() {
        if (!this.isGameInProgress()) {
            Utils.verboseLog("Failed to end Village game. No game in progress.");
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
        }
        Utils.verboseLog("Ending Village game. Cleaned up players.");

        this.gameLoop = null;
        Bukkit.getScheduler().cancelTask(gameLoopID);
        this.gameLoopID = -1;
        Utils.verboseLog("Ending Village game. Game loop cleaned up.");

        this.setGameStatus(Status.NO_GAME);
        Utils.verboseLog("Ended Village game. Finished.");
    }

    public void setPlayerRole(UUID uuid, Role role) {
        this.playerRoles.put(uuid, role);
    }

    public Role getPlayerRole(UUID uuid) {
        return this.playerRoles.get(uuid);
    }

    public double getCompletedTaskPercent() {
        int numerator = this.getAmountOfCompletedTasks();
        double denominator = Math.max(this.getVillagerCount(), 1) * Math.max(this.getTasksPerVillager(), 1);

        return Math.floor((numerator / denominator) * 100);
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
    }

    public boolean isGameInProgress() {
        return this.status != Status.NO_GAME;
    }

    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    public void setSpawnLocation(Location location) {
        this.spawnLocation = location;
    }

    public Location getMeetingLocation() {
        return this.meetingLocation;
    }

    public void setMeetingLocation(Location location) {
        this.meetingLocation = location;
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
     * Will attempt to save the current game object to the current world's config
     * section
     * 
     * @return True if saved, false if failed
     */
    public boolean saveCurrentGameConfig(String worldName) {
        try {
            String root = "worlds. " + worldName + ".";
            int i = 0;

            String tasks = root + "tasks.";
            for (VillagerTask task : this.getVillagerTasks()) {
                Storage.worldsData.set(tasks + "task" + i + ".type", task.getTaskType().toString());

                Location loc = task.getBlock().getLocation();
                Storage.worldsData.set(tasks + "task" + i + ".coords.x", loc.getBlockX());
                Storage.worldsData.set(tasks + "task" + i + ".coords.y", loc.getBlockY());
                Storage.worldsData.set(tasks + "task" + i + ".coords.z", loc.getBlockZ());
                i++;
            }

            i = 0;

            String meetingLoc = root + "meeting.";
            Storage.worldsData.set(meetingLoc + "x", this.getMeetingLocation().getBlockX());
            Storage.worldsData.set(meetingLoc + "x", this.getMeetingLocation().getBlockY());
            Storage.worldsData.set(meetingLoc + "x", this.getMeetingLocation().getBlockZ());

            String spawnLoc = root + "spawn.";
            Storage.worldsData.set(spawnLoc + "x", this.getSpawnLocation().getBlockX());
            Storage.worldsData.set(spawnLoc + "x", this.getSpawnLocation().getBlockY());
            Storage.worldsData.set(spawnLoc + "x", this.getSpawnLocation().getBlockZ());

            Storage.worldsData.save(Storage.worldsFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }

        return true;
    }

    public void loadFromSaveID(String worldName) {
        ConfigurationSection section = Storage.worldsData.getConfigurationSection("worlds." + worldName);
        if (section == null) {
            return;
        }

    }
}
