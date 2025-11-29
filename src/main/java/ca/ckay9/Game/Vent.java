package ca.ckay9.Game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ca.ckay9.Utils;

public class Vent {
    private Block block;
    private ArrayList<Vent> connectedVents;
    private HashSet<UUID> mobsInside;

    public Vent(Block block) {
        this.block = block;
        this.connectedVents = new ArrayList<>();
        this.mobsInside = new HashSet<>();
    }

    public Vent(Block block, ArrayList<Vent> connectedVents) {
        this.block = block;
        this.connectedVents = connectedVents;
        this.mobsInside = new HashSet<>();
    }

    public HashSet<UUID> getMobsInside() {
        return this.mobsInside;
    }

    public void setMobsInside(HashSet<UUID> set) {
        this.mobsInside = set;
    }

    public void addMobInside(UUID uuid) {
        this.getMobsInside().add(uuid);
    }

    public void removeMobInside(UUID uuid) {
        this.getMobsInside().remove(uuid);
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

        ItemStack exitVentSystem = new ItemStack(Material.BARRIER, 1);
        ItemMeta exitMeta = exitVentSystem.getItemMeta();
        exitMeta.setDisplayName(Utils.formatText("&c&lEXIT"));
        exitVentSystem.setItemMeta(exitMeta);
        menu.setItem(18, exitVentSystem);

        player.playSound(player.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 5, 0);
        player.closeInventory();
        player.openInventory(menu);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10_000_000, 1, false, false, false));
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

    public void removeConnectedVent(Vent vent) {
        this.connectedVents.remove(vent);
    }
}
