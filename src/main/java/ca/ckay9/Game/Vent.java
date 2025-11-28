package ca.ckay9.Game;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ca.ckay9.Utils;

public class Vent {
    private Block block;
    private ArrayList<Vent> connectedVents;

    public Vent(Block block) {
        this.block = block;
        this.connectedVents = new ArrayList<>();
    }

    public Vent(Block block, ArrayList<Vent> connectedVents) {
        this.block = block;
        this.connectedVents = connectedVents;
    }

    public void openVentConnectionsMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, Utils.formatText("&c&lVENT SYSTEM"));
        menu.clear();

        for (int i = 0; i < this.getConnectedVents().size(); i++) {
            ItemStack ventStack = new ItemStack(Material.IRON_TRAPDOOR, 1);
            ItemMeta ventMeta = ventStack.getItemMeta();
            ventMeta.setDisplayName(Utils.formatText("&lVENT #" + (i + 1)));
            ventStack.setItemMeta(ventMeta);

            menu.setItem(i, ventStack);
        }

        

        player.closeInventory();
        player.openInventory(menu);
    }

    public Block getBlock() {
        return this.block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public ArrayList<Vent> getConnectedVents() {
        return this.connectedVents;
    }

    public void setConnectedVents(ArrayList<Vent> connectedVents) {
        this.connectedVents = connectedVents;
    }

    public void addConnectedVent(Vent vent) {
        this.connectedVents.add(vent);
    }
}
