package ca.ckay9.Game.Mobs;

import org.bukkit.inventory.Inventory;

public class StabilizerProgress {
    private boolean completed;
    private Inventory inventory;

    public StabilizerProgress(Inventory inv) {
        this.inventory = inv;
    }

    public void setInventory(Inventory inv) {
        this.inventory = inv;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void setCompleted(boolean value) {
        this.completed = value;
    }

    public boolean isCompleted() {
        return this.completed;
    }
}
