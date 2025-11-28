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

    public Editor(Village village) {
        this.village = village;
        this.editorStates = new HashMap<>();
        this.ventEditor = new VentEditor(this, village.getGame());
        this.taskEditor = new TaskEditor(this, village.getGame());

        PluginManager manager = village.getServer().getPluginManager();
        manager.registerEvents(this.ventEditor, village);
        manager.registerEvents(this.taskEditor, village);
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
                        "&a&l[Village]&r&a Vent Editor enabled. Use any of the tools to make/manage vents. Break a vent to remove and delink it."));
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
        mathMeta.setDisplayName(Utils.formatText("&aMATH TASK / PLACE TO CREATE"));
        mathTool.setItemMeta(mathMeta);

        ItemStack craftTool = new ItemStack(Material.CRAFTING_TABLE, 1);
        ItemMeta craftMeta = craftTool.getItemMeta();
        craftMeta.setDisplayName(Utils.formatText("&bCRAFT TASK / PLACE TO CREATE"));
        craftTool.setItemMeta(craftMeta);

        ItemStack triviaTool = new ItemStack(Material.LECTERN, 1);
        ItemMeta triviaMeta = triviaTool.getItemMeta();
        triviaMeta.setDisplayName(Utils.formatText("&6TRIVIA TASK / PLACE TO CREATE"));
        triviaTool.setItemMeta(triviaMeta);

        ItemStack customTool = new ItemStack(Material.ENCHANTING_TABLE, 1);
        ItemMeta customMeta = customTool.getItemMeta();
        customMeta.setDisplayName(Utils.formatText("&6CUSTOM TASK / PLACE TO CREATE / RANDOMLY SELECTS"));
        customTool.setItemMeta(customMeta);

        ItemStack exitTool = new ItemStack(Material.BARRIER, 1);
        ItemMeta exitMeta = exitTool.getItemMeta();
        exitMeta.setDisplayName(Utils.formatText("&cEXIT EDITOR"));
        exitTool.setItemMeta(exitMeta);

        playerInventory.addItem(mathTool);
        playerInventory.addItem(craftTool);
        playerInventory.addItem(triviaTool);
        playerInventory.addItem(customTool);
        playerInventory.addItem(exitTool);

        village.getEditor().addEditor(player.getUniqueId(), EditorState.TASK);
        player.sendMessage(Utils
                .formatText(
                        "&a&l[Village]&r&a Task Editor enabled. Use any of the tools to make tasks. Break a task to remove it."));
    }

    /**
     * Disables editor mode for the given player
     * 
     * @param player The player to exit the editor
     */
    public void exitEditor(Player player) {
        this.getEditorStates().put(player.getUniqueId(), EditorState.NONE);
        player.getInventory().clear();

        village.getEditor().addEditor(player.getUniqueId(), EditorState.VENT);
        player.sendMessage(Utils
                .formatText(
                        "&a&l[Village]&r&a Editor disabled."));
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
