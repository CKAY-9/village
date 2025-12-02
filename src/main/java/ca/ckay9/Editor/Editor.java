package ca.ckay9.Editor;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import ca.ckay9.Utils;
import ca.ckay9.Village;

public class Editor {
    private HashMap<UUID, EditorState> editorStates;
    private Village village;
    private VentEditor ventEditor;
    private TaskEditor taskEditor;
    private SabotageEditor sabotageEditor;

    public Editor(Village village) {
        this.village = village;
        this.editorStates = new HashMap<>();
        this.ventEditor = new VentEditor(this, village.getGame());
        this.taskEditor = new TaskEditor(this, village.getGame());
        this.sabotageEditor = new SabotageEditor(this, village.getGame());

        PluginManager manager = village.getServer().getPluginManager();
        manager.registerEvents(this.ventEditor, village);
        manager.registerEvents(this.taskEditor, village);
        manager.registerEvents(this.sabotageEditor, village);
    }

    public boolean isPlayerEditing(Player player) {
        EditorState state = this.getEditorStates().get(player.getUniqueId());
        return state != null && (state == EditorState.TASK || state == EditorState.VENT);
    }

    /**
     * Clears the players inventory, gives them the tools, and then sets their
     * editor status to VENT
     * 
     * @param player The player to enter vent editor
     */
    public void enableVentEditorForPlayer(Player player) {
        Inventory playerInventory = player.getInventory();
        playerInventory.clear();

        // give player tools
        ItemStack ventTool = new ItemStack(Material.IRON_TRAPDOOR, 1);
        ItemMeta ventMeta = ventTool.getItemMeta();
        ventMeta.setDisplayName(Utils.formatText("&aNEW VENT / PLACE TO CREATE"));
        ventTool.setItemMeta(ventMeta);

        ItemStack linkTool = new ItemStack(Material.TRIPWIRE_HOOK, 1);
        ItemMeta linkMeta = linkTool.getItemMeta();
        linkMeta.setDisplayName(Utils.formatText("&bLINK VENTS / RIGHT CLICK ON VENT TO START"));
        linkTool.setItemMeta(linkMeta);

        ItemStack exitTool = new ItemStack(Material.BARRIER, 1);
        ItemMeta exitMeta = exitTool.getItemMeta();
        exitMeta.setDisplayName(Utils.formatText("&cEXIT EDITOR"));
        exitTool.setItemMeta(exitMeta);

        playerInventory.addItem(ventTool);
        playerInventory.addItem(linkTool);
        playerInventory.addItem(exitTool);

        village.getEditor().addEditor(player.getUniqueId(), EditorState.VENT);
        player.sendMessage(Utils
                .formatText(
                        "&a&l[VILLAGE]&r&a Vent Editor enabled. Use any of the tools to make/manage vents. Break a vent to remove and delink it."));
    }

    /**
     * Clears the players inventory, gives them the tools, and then sets their
     * editor status to TASK
     * 
     * @param player
     */
    public void enableTaskEditorForPlayer(Player player) {
        Inventory playerInventory = player.getInventory();
        playerInventory.clear();

        // give player tools
        ItemStack mathTool = new ItemStack(Material.SMITHING_TABLE, 1);
        ItemMeta mathMeta = mathTool.getItemMeta();
        mathMeta.setDisplayName(Utils.formatText("&dMATH TASK / PLACE TO CREATE"));
        mathTool.setItemMeta(mathMeta);

        ItemStack craftTool = new ItemStack(Material.CRAFTING_TABLE, 1);
        ItemMeta craftMeta = craftTool.getItemMeta();
        craftMeta.setDisplayName(Utils.formatText("&5CRAFT TASK / PLACE TO CREATE"));
        craftTool.setItemMeta(craftMeta);

        ItemStack triviaTool = new ItemStack(Material.LECTERN, 1);
        ItemMeta triviaMeta = triviaTool.getItemMeta();
        triviaMeta.setDisplayName(Utils.formatText("&6TRIVIA TASK / PLACE TO CREATE"));
        triviaTool.setItemMeta(triviaMeta);

        ItemStack uploadTool = new ItemStack(Material.OBSERVER, 1);
        ItemMeta uploadMeta = uploadTool.getItemMeta();
        uploadMeta.setDisplayName(Utils.formatText("&eUPLOAD TASK / PLACE 2 TOTAL"));
        uploadTool.setItemMeta(uploadMeta);

        ItemStack manifoldTool = new ItemStack(Material.DISPENSER, 1);
        ItemMeta manifoldMeta = manifoldTool.getItemMeta();
        manifoldMeta.setDisplayName(Utils.formatText("&2MANIFOLD TASK / PLACE TO CREATE"));
        manifoldTool.setItemMeta(manifoldMeta);

        ItemStack scanTool = new ItemStack(Material.REDSTONE_BLOCK, 1);
        ItemMeta scanMeta = scanTool.getItemMeta();
        scanMeta.setDisplayName(Utils.formatText("&aMEDICAL SCAN TASK / PLACE TO CREATE"));
        scanTool.setItemMeta(scanMeta);

        ItemStack ventCleanTool = new ItemStack(Material.DRIED_KELP, 1);
        ItemMeta ventCleanMeta = ventCleanTool.getItemMeta();
        ventCleanMeta.setDisplayName(Utils.formatText("&9CLEAN VENT TASK / RIGHT CLICK A VENT TO TOGGLE IT"));
        ventCleanTool.setItemMeta(ventCleanMeta);

        ItemStack customTool = new ItemStack(Material.ENCHANTING_TABLE, 1);
        ItemMeta customMeta = customTool.getItemMeta();
        customMeta.setDisplayName(Utils.formatText("&8CUSTOM TASK / &c&lNON-IMPLEMENTED"));
        customTool.setItemMeta(customMeta);

        ItemStack exitTool = new ItemStack(Material.BARRIER, 1);
        ItemMeta exitMeta = exitTool.getItemMeta();
        exitMeta.setDisplayName(Utils.formatText("&cEXIT EDITOR"));
        exitTool.setItemMeta(exitMeta);

        playerInventory.addItem(mathTool);
        playerInventory.addItem(craftTool);
        playerInventory.addItem(triviaTool);
        playerInventory.addItem(manifoldTool);
        playerInventory.addItem(scanTool);
        playerInventory.addItem(uploadTool);
        playerInventory.addItem(ventCleanTool);
        playerInventory.addItem(customTool);
        playerInventory.addItem(exitTool);

        village.getEditor().addEditor(player.getUniqueId(), EditorState.TASK);
        player.sendMessage(Utils
                .formatText(
                        "&a&l[VILLAGE]&r&a Task Editor enabled. Use any of the tools to make tasks. Break a task to remove it."));
    }

    public void enableSabotageEditor(Player player) {
        Inventory playerInventory = player.getInventory();
        playerInventory.clear();

        // give player tools
        ItemStack stabilizerTool = new ItemStack(Material.REDSTONE_LAMP, 1);
        ItemMeta stabilizerMeta = stabilizerTool.getItemMeta();
        stabilizerMeta.setDisplayName(Utils.formatText("&dSTABILIZER SABOTAGE / PLACE 2 TO CREATE"));
        stabilizerTool.setItemMeta(stabilizerMeta);

        ItemStack reactorTool = new ItemStack(Material.TNT, 1);
        ItemMeta reactorMeta = reactorTool.getItemMeta();
        reactorMeta.setDisplayName(Utils.formatText("&eREACTOR SABOTAGE / PLACE 1 TO CREATE"));
        reactorTool.setItemMeta(reactorMeta);

        ItemStack exitTool = new ItemStack(Material.BARRIER, 1);
        ItemMeta exitMeta = exitTool.getItemMeta();
        exitMeta.setDisplayName(Utils.formatText("&cEXIT EDITOR"));
        exitTool.setItemMeta(exitMeta);

        playerInventory.addItem(stabilizerTool);
        playerInventory.addItem(reactorTool);
        playerInventory.addItem(exitTool);

        village.getEditor().addEditor(player.getUniqueId(), EditorState.SABOTAGE);
        player.sendMessage(Utils
                .formatText(
                        "&a&l[VILLAGE]&r&a Sabotage Editor enabled. Use any of the tools to make sabotages. Break a sabotage to remove it."));
    }

    /**
     * Disables editor mode for the given player
     * 
     * @param player The player to exit the editor
     */
    public void exitEditor(Player player) {
        this.getEditorStates().put(player.getUniqueId(), EditorState.NONE);
        player.getInventory().clear();
        player.sendMessage(Utils
                .formatText(
                        "&a&l[VILLAGE]&r&a Editor disabled."));
    }

    public HashMap<UUID, EditorState> getEditorStates() {
        return this.editorStates;
    }

    public void setEditorStates(HashMap<UUID, EditorState> editorStates) {
        this.editorStates = editorStates;
    }

    public void addEditor(UUID playerUUID, EditorState state) {
        this.editorStates.put(playerUUID, state);
    }

    public void removeEditor(UUID playerUUID) {
        this.editorStates.remove(playerUUID);
    }
}
