package ca.ckay9.Game;

public enum Role {
    VILLAGER,
    DETECTIVE,
    MEDIC,
    MOB,
    SWEEPER,
    DARK_WIZARD;

    public String toString() {
        return this.name().replace("_", " ");
    }
}
