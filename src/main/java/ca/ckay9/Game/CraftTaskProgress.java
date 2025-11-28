package ca.ckay9.Game;

import org.bukkit.Material;

public class CraftTaskProgress {
    private VillagerTask task;
    private Material material;

    public CraftTaskProgress(VillagerTask task, Material material) {
        this.task = task;
        this.material = material;
    }

    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public VillagerTask getTask() {
        return this.task;
    }

    public void setTask(VillagerTask task) {
        this.task = task;
    }
}
