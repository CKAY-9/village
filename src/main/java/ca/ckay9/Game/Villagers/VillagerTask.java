package ca.ckay9.Game.Villagers;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ca.ckay9.Utils;
import ca.ckay9.Village;
import ca.ckay9.Game.Game;
import ca.ckay9.Game.Status;

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

        Utils.verbosePlayerLog(player, "Started math task.\n  -> a = " + a + "\n  -> b = " + b + "\n  -> operand = "
                + operand + "\n  -> answer = " + answer);
        game.addChatTaskExpectedResult(player.getUniqueId(), new ChatTaskProgress(this, String.valueOf(answer)));
        player.sendMessage(Utils.formatText(
                "&3&l[MATH TASK]&r&3 What is " + a + " " + operand + " " + b + "?"));
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

        String finalProductName = finalProduct.name().replaceAll("_", " ");
        Utils.verbosePlayerLog(player, "Started craft task.\n  -> Final Product = " + finalProductName);
        game.addCraftTaskExpectedResult(player.getUniqueId(), new CraftTaskProgress(this, finalProduct));
        player.sendMessage(Utils.formatText(
                "&b&l[CRAFT TASK]&r&b Craft the following item: " + finalProductName));
    }

    private void triviaTask(Player player, Game game) {
        Random random = new Random();

        String[] questions = {
                "What year did Minecraft officially release?",
                "Do pigs fly (yes/no)?",
                "How many iron ingots do you need to craft a full set of armor?"
        };
        String[] answers = {
                "2011",
                "no",
                "24"
        };
        int selectedIndex = random.nextInt(0, questions.length);

        String queston = questions[selectedIndex];
        String answer = answers[selectedIndex];

        Utils.verbosePlayerLog(player,
                "Started trivia task.\n  -> Question = " + queston + "\n  -> Answer = " + answer);
        game.addChatTaskExpectedResult(player.getUniqueId(), new ChatTaskProgress(this, answer));
        player.sendMessage(Utils.formatText("&9&l[TRIVIA TASK]&r&9 " + queston));
    }

    private void customTask(Player player, Game game) {

    }

    private void uploadTask(Player player, Game game) {
        UUID playerUUID = player.getUniqueId();

        UploadPart part = game.getUploadParts().get(playerUUID);
        if (!game.isFirstPartUpload(this.getBlock().getLocation()) && part != UploadPart.COPIED) {
            player.sendMessage(Utils.formatText("&c&l[UPLOAD TASK]&r&c You haven't copied the files."));
            return;
        }

        if (game.isFirstPartUpload(this.getBlock().getLocation()) && part != null) {
            player.sendMessage(Utils.formatText("&c&l[UPLOAD TASK]&r&c You've have already copied the files."));
            return;
        }

        if (part == null) {
            part = UploadPart.COPYING;
            game.addUploadPart(playerUUID, part);
            player.sendTitle(Utils.formatText("&6&lCOPYING"), Utils.formatText("Copying files... please wait"), 10, 180,
                    10);
        } else if (part == UploadPart.COPYING || part == UploadPart.UPLOADING) {
            return;
        }

        if (part == UploadPart.COPIED) {
            player.sendTitle(Utils.formatText("&6&lUPLOADING"), Utils.formatText("Uploading files... please wait"), 10,
                    180, 10);
            part = UploadPart.UPLOADING;
            game.addUploadPart(playerUUID, part);
        }

        final UploadPart fPart = part;

        // copy files (1) -> go to other (2) -> upload files (3) -> done (4)

        Utils.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Utils.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (!game.isGameInProgress()) {
                    game.removeUploadPart(playerUUID);
                    return;
                }

                // if cancelled by meeting
                if (game.getGameStatus() != Status.PLAYING) {
                    if (fPart == UploadPart.COPYING) {
                        game.removeUploadPart(playerUUID);
                    }

                    return;
                }

                if (fPart == UploadPart.COPYING) {
                    game.addUploadPart(playerUUID, UploadPart.COPIED);
                    player.sendTitle(Utils.formatText("&6&lCOPIED"), Utils.formatText("Copied files to folder."), 10,
                            40, 10);
                } else if (fPart == UploadPart.UPLOADING) {
                    player.sendTitle(Utils.formatText("&6&lUPLOADED"), Utils.formatText("Uploaded files to network."),
                            10, 40, 10);
                    game.addUploadPart(playerUUID, UploadPart.UPLOADED);
                    completeTask(player, game);
                }
            }
        }, 200L);
    }

    public void unassignPlayer(UUID player) {
        this.assignedVillagers.remove(player);
    }

    /**
     * Attempts to start the task for the given player.
     * 
     * @param player Who should be attempting the task
     * @param game   The game object
     */
    public void startTask(Player player, Game game) {
        if (!this.assignedToThis(player.getUniqueId()) && !Village.inDeveloperDebug()) {
            player.sendMessage(Utils.formatText("&c&l[TASK]&r&c You aren't assigned to this task"));
            return;
        }

        Utils.verbosePlayerLog(player, "Attempting to start task.");

        if (this.hasCompleted(player.getUniqueId())) {
            Utils.verbosePlayerLog(player, "Already completed task.");
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
        } else if (this.getTaskType() == VillagerTaskType.UPLOAD) {
            uploadTask(player, game);
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
    public void completeTask(Player player, Game game) {
        Utils.verbosePlayerLog(player, "Completed task.");
        this.assignedVillagers.put(player.getUniqueId(), true);
        player.sendMessage(Utils.formatText("&a&l[TASK]&r&a Completed task!"));
    }

    /**
     * Sets the status for this player to false
     * 
     * @param player The player who failed the task
     */
    public void failTask(Player player, Game game) {
        Utils.verbosePlayerLog(player, "Failed task.");
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

        if (taskType == VillagerTaskType.CRAFT) {
            this.getBlock().setType(Material.CRAFTING_TABLE);
        } else if (taskType == VillagerTaskType.MATH) {
            this.getBlock().setType(Material.SMITHING_TABLE);
        } else if (taskType == VillagerTaskType.TRIVIA) {
            this.getBlock().setType(Material.LECTERN);
        } else if (taskType == VillagerTaskType.UPLOAD) {
            this.getBlock().setType(Material.OBSERVER);
        } else if (taskType == VillagerTaskType.CUSTOM) {
            this.getBlock().setType(Material.ENCHANTING_TABLE);
        }
    }
}
