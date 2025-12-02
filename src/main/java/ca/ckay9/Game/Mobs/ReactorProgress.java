package ca.ckay9.Game.Mobs;

import org.bukkit.Material;

public class ReactorProgress {
    private Material currentMaterial;
    private int completed;

    public ReactorProgress(Material material) {
        this.currentMaterial = material;
        this.completed = 0;
    }

    public void setMaterial(Material mat) {
        this.currentMaterial = mat;
    }

    public Material getMaterial() {
        return this.currentMaterial;
    }

    public void setCompleted(int value) {
        this.completed = value;
    }

    public int getCompleted() {
        return this.completed;
    }
}
