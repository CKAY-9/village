package ca.ckay9.Game;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ca.ckay9.Utils;

public class VillagerTask {
    private Block block;
    private HashMap<UUID, Boolean> assignedVillagers;
    private VillagerTaskType taskType;

    public VillagerTask(Block block) {
        this.block = block;
        this.assignedVillagers = new HashMap<>();
        this.taskType = VillagerTaskType.MATH;
    }

    public VillagerTask(Block block, HashMap<UUID, Boolean> assignedVillagers) {
        this.block = block;
        this.assignedVillagers = assignedVillagers;
        this.taskType = VillagerTaskType.MATH;
    }

    public VillagerTask(Block block, HashMap<UUID, Boolean> assignedVillagers, VillagerTaskType taskType) {
        this.block = block;
        this.assignedVillagers = assignedVillagers;
        this.taskType = taskType;
    }

    private void mathTask(Player player, Game game) {
        Random random = new Random();
        int a = random.nextInt(-10, 10);
        int b = random.nextInt(-10, 10);
        char operand = '+';
        int answer = a + b;
        double operandRandom = random.nextDouble();
        if (operandRandom >= 0.33) {
            operand = '-';
            answer = a - b;
        }
        if (operandRandom >= 0.67) {
            operand = '*';
            answer = a * b;
        }

        game.addChatTaskExpectedResult(player.getUniqueId(), new ChatTaskProgress(this, String.valueOf(answer)));
        player.sendMessage(Utils.formatText(
                "&b&l[MATH TASK]&r&b What is " + a + " " + operand + " " + b));
    }

    private void craftTask(Player player, Game game) {
        Random random = new Random();

        /*
         * might change this to a class:
         * CraftTask:
         * -> finalProduct: material => the final item to be crafted
         * -> materials: material[] => what is needed to craft the item
         */
        Material[] finalProducts = { Material.DIAMOND_HOE, Material.CAKE, Material.FISHING_ROD };
        ItemStack[][] requiredItems = {
                { (new ItemStack(Material.DIAMOND, 2)), (new ItemStack(Material.STICK, 2)) },
                { (new ItemStack(Material.MILK_BUCKET, 3)), (new ItemStack(Material.WHEAT, 3)),
                        (new ItemStack(Material.SUGAR, 2)), (new ItemStack(Material.EGG)) },
                { (new ItemStack(Material.STRING, 2)), (new ItemStack(Material.STICK, 3)) }
        };

        int selectedIndex = random.nextInt(0, finalProducts.length);
        Material finalProduct = finalProducts[selectedIndex];
        ItemStack[] itemsToGive = requiredItems[selectedIndex];
        for (int i = 0; i < itemsToGive.length; i++) {
            player.getInventory().addItem(itemsToGive[i]);
        }

        game.addCraftTaskExpectedResult(player.getUniqueId(), finalProduct);
        player.sendMessage(Utils.formatText(
                "&b&l[CRAFT TASK]&r&b You must craft the following item: " + finalProduct.name().replaceAll("_", " ")));
    }

    private void triviaTask(Player player, Game game) {

    }

    private void customTask(Player player, Game game) {

    }

    /**
     * Attempts to start the task for the given player.
     * 
     * @param player Who should be attempting the task
     * @param game   The game object
     */
    public void startTask(Player player, Game game) {
        if (!this.assignedToThis(player.getUniqueId())) {
            player.sendMessage(Utils.formatText("&c&l[TASK]&r&c You aren't assigned to this task"));
            //return;
        }

        if (this.hasCompleted(player.getUniqueId())) {
            player.sendMessage(Utils.formatText("&a&l[TASK]&r&a You have already completed this task."));
            return;
        }

        if (this.getTaskType() == VillagerTaskType.MATH) {
            mathTask(player, game);
        } else if (this.getTaskType() == VillagerTaskType.CRAFT) {
            craftTask(player, game);
        } else if (this.getTaskType() == VillagerTaskType.TRIVIA) {
            triviaTask(player, game);
        } else if (this.getTaskType() == VillagerTaskType.CUSTOM) {
            customTask(player, game);
        }
    }

    /**
     * 
     * @param uuid Check if this person has completed the task
     * @return True if they have, false if they haven't or aren't assigned. Use
     *         assignedToThis to check.
     */
    public boolean hasCompleted(UUID uuid) {
        Boolean state = this.getAssignedVillagers().get(uuid);
        return (state != null && state);
    }

    /**
     * Sets the status for this player to true
     * 
     * @param player The player who completed the Task
     */
    public void completeTask(Player player) {
        this.assignedVillagers.put(player.getUniqueId(), true);
        player.sendMessage(Utils.formatText("&a&l[TASK]&r&a Completed task!"));
    }

    /**
     * Sets the status for this player to false
     * 
     * @param player The player who failed the task
     */
    public void failTask(Player player) {
        this.assignedVillagers.put(player.getUniqueId(), false);
        player.sendMessage(Utils.formatText("&c&l[TASK]&r&c Failed task! Retry by interacting with it."));
    }

    /**
     * @return Returns the block associated with the task
     */
    public Block getBlock() {
        return this.block;
    }

    /**
     * @param block Block to associated with task
     */
    public void setBlock(Block block) {
        this.block = block;
    }

    /**
     * @return Returns the assignedVillagers hash map, UUID -> Boolean, true =
     *         completed, false = needs to complete
     */
    public HashMap<UUID, Boolean> getAssignedVillagers() {
        return this.assignedVillagers;
    }

    /**
     * @param newAssignedVillagers New value for the assignedVillagers hash mpa
     */
    public void setAssignedVillagers(HashMap<UUID, Boolean> newAssignedVillagers) {
        this.assignedVillagers = newAssignedVillagers;
    }

    public void addAssignedVillager(UUID uuid) {
        this.assignedVillagers.put(uuid, false);
    }

    /**
     * 
     * @param uuid Who to check if they're assigned
     * @return True if they are assigned to this task, false otherwise
     */
    public boolean assignedToThis(UUID uuid) {
        return this.assignedVillagers.containsKey(uuid);
    }

    /**
     * @return Returns the task type
     */
    public VillagerTaskType getTaskType() {
        return this.taskType;
    }

    /**
     * @param taskType New value for the task type
     */
    public void setTaskType(VillagerTaskType taskType) {
        this.taskType = taskType;
    }
}
