package ca.ckay9.Game.Mobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ca.ckay9.Utils;
import ca.ckay9.Game.Game;

public class Sabotage {
    private SabotageType sabotageType;
    private Block block;
    private boolean active;
    private HashMap<UUID, StabilizerProgress> stabilzierProgress;
    private HashMap<UUID, ReactorProgress> reactorProgress;
    private long started; // when the sabotage when called
    private AreaEffectCloud effect;

    // how long sabotages last before ending game in ticks
    public static long SABOTAGE_TIME = Utils.secondsToTicks(45);

    public Sabotage(Block block) {
        this.block = block;
        this.sabotageType = SabotageType.STABILIZER;
        this.active = false;
        this.reactorProgress = new HashMap<>();
        this.stabilzierProgress = new HashMap<>();
    }

    public long getStartedTicks() {
        return this.started;
    }

    public void setStartedTicks(long ticks) {
        this.started = ticks;
    }

    public boolean solvedStabilizer() {
        int count = 0;
        for (StabilizerProgress progress : this.getStabilizerProgress().values()) {
            if (progress.isCompleted()) {
                count++;
            }
        }

        return count >= Math.min(Bukkit.getOnlinePlayers().size(), 2);
    }

    /**
     * 
     * @param currentTick What is the current tick since start
     * @return If the sabotage has been going on for more than SABOTAGE_TIME ticks
     *         then true,
     *         otherwise false
     */
    public boolean shouldEndGame(long currentTick) {
        if (!this.isActive()) {
            return false;
        }

        long duration = currentTick - this.getStartedTicks();
        return duration > SABOTAGE_TIME;
    }

    public HashMap<UUID, ReactorProgress> getReactorProgress() {
        return this.reactorProgress;
    }

    public ReactorProgress getReactorProgressForPlayer(UUID playerUUID) {
        return this.reactorProgress.get(playerUUID);
    }

    public HashMap<UUID, StabilizerProgress> getStabilizerProgress() {
        return this.stabilzierProgress;
    }

    public void addStabilizerProgress(UUID playerUUID, StabilizerProgress progress) {
        this.stabilzierProgress.put(playerUUID, progress);
    }

    public StabilizerProgress getStabilizerProgressForPlayer(UUID playerUUID) {
        return this.stabilzierProgress.get(playerUUID);
    }

    public void setReactorProgress(HashMap<UUID, ReactorProgress> reactorProgress) {
        this.reactorProgress = reactorProgress;
    }

    public void addReactorProgress(UUID playerUUID, ReactorProgress material) {
        this.reactorProgress.put(playerUUID, material);
    }

    public void setActive(boolean value) {
        this.active = value;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setSabotageType(SabotageType type) {
        this.sabotageType = type;
    }

    public SabotageType getSabotageType() {
        return this.sabotageType;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return this.block;
    }

    public boolean canBuild() {
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = 0; y < 5; y++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    Block offsetBlock = this.getBlock().getRelative(x, y, z);
                    if (offsetBlock.getType() != Material.AIR) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void destroy() {
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = 0; y < 5; y++) {
                    Block offsetBlock = this.getBlock().getRelative(x, y, z);
                    offsetBlock.setType(Material.AIR);
                }
            }
        }
    }

    public void build() {
        if (this.isReactor()) {
            // this is vertical slices of the build. so model[0] is used for the bottom and
            // top, model[1] is used twice in the middle
            Material[][][] model = {
                    {
                            { Material.AIR, Material.SEA_LANTERN, Material.AIR },
                            { Material.SEA_LANTERN, Material.TINTED_GLASS, Material.SEA_LANTERN },
                            { Material.AIR, Material.SEA_LANTERN, Material.AIR },
                    },
                    {
                            { Material.AIR, Material.TINTED_GLASS, Material.AIR },
                            { Material.TINTED_GLASS, Material.TNT, Material.TINTED_GLASS },
                            { Material.AIR, Material.TINTED_GLASS, Material.AIR },
                    },
            };

            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    this.getBlock().getRelative(x - 1, 0, z - 1).setType(model[0][x][z]);
                    this.getBlock().getRelative(x - 1, 3, z - 1).setType(model[0][x][z]);

                    this.getBlock().getRelative(x - 1, 1, z - 1).setType(model[1][x][z]);
                    this.getBlock().getRelative(x - 1, 2, z - 1).setType(model[1][x][z]);
                }
            }
        } else if (this.isStabilizer()) {
            Material[][][] model = {
                    {
                            { Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK },
                            { Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK },
                            { Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK },
                    },
                    {
                            { Material.AIR, Material.GRAY_CONCRETE, Material.AIR },
                            { Material.GRAY_CONCRETE, Material.BEACON, Material.GRAY_CONCRETE },
                            { Material.AIR, Material.GRAY_CONCRETE, Material.AIR },
                    },
                    {
                            { Material.AIR, Material.WHITE_STAINED_GLASS, Material.AIR },
                            { Material.WHITE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS,
                                    Material.WHITE_STAINED_GLASS },
                            { Material.AIR, Material.WHITE_STAINED_GLASS, Material.AIR },
                    },
                    {
                            { Material.AIR, Material.AIR, Material.AIR },
                            { Material.AIR, Material.BLUE_STAINED_GLASS, Material.AIR },
                            { Material.AIR, Material.AIR, Material.AIR },
                    }
            };

            for (int i = 0; i < 4; i++) {
                for (int x = 0; x < 3; x++) {
                    for (int z = 0; z < 3; z++) {
                        this.getBlock().getRelative(x - 1, i - 1, z - 1).setType(model[i][x][z]);
                    }
                }
            }
        }
    }

    private void solveReactor(Player player) {
        player.closeInventory();

        Inventory inv = Bukkit.createInventory(null, 54, Utils.formatText("&e&lREACTOR SHUTDOWN"));
        inv.clear();

        ArrayList<Material> materials = new ArrayList<>();
        materials.add(Material.AMETHYST_SHARD);
        materials.add(Material.IRON_INGOT);
        materials.add(Material.BONE_MEAL);
        materials.add(Material.NETHER_STAR);
        materials.add(Material.BLAZE_POWDER);
        materials.add(Material.GOLD_NUGGET);
        materials.add(Material.MUSIC_DISC_STAL);
        Collections.shuffle(materials);

        Random random = new Random();
        Material toChooseMat = materials.get(random.nextInt(0, materials.size()));
        ItemStack toChooseStack = new ItemStack(toChooseMat);
        ItemMeta toChooseMeta = toChooseStack.getItemMeta();
        toChooseMeta.setDisplayName(Utils.formatText("&9&lREQUIRED INPUT"));
        toChooseStack.setItemMeta(toChooseMeta);

        inv.setItem(13, toChooseStack);

        for (int i = 0; i < materials.size(); i++) {
            ItemStack stack = new ItemStack(materials.get(i));
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(Utils.formatText("&c&lREACTOR INPUT"));
            stack.setItemMeta(meta);
            inv.setItem(28 + i, stack);
        }

        ReactorProgress progress = this.getReactorProgressForPlayer(player.getUniqueId());
        if (progress == null) {
            progress = new ReactorProgress(toChooseMat);
        }

        progress.setMaterial(toChooseMat);
        this.addReactorProgress(player.getUniqueId(), progress);
        player.openInventory(inv);
    }

    private void solveStabilizer(Player player) {
        player.closeInventory();

        Inventory inv = Bukkit.createInventory(null, 54, Utils.formatText("&e&lSTABILIZER MISALIGNMENT"));
        inv.clear();

        for (int row = 0; row < 6; row++) {
            for (int column = 3; column < 6; column++) {
                int slot = (row * 9) + column;
                ItemStack alignmentItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta meta = alignmentItem.getItemMeta();
                meta.setDisplayName(Utils.formatText("&8&lSTABILIZER ALIGNMENT"));
                alignmentItem.setItemMeta(meta);

                inv.setItem(slot, alignmentItem);
            }
        }

        Random random = new Random();
        for (int i = 0; i < 7; i++) {
            int randomRow = random.nextInt(0, 6);
            int randomCol = random.nextInt(0, 3);
            int randomCount = random.nextInt(3, 7);
            boolean right = random.nextBoolean();

            if (right) {
                randomCol += 6;
            }

            ItemStack errorItem = new ItemStack(Material.RED_STAINED_GLASS_PANE, randomCount);
            ItemMeta errorMeta = errorItem.getItemMeta();
            errorMeta.setDisplayName(Utils.formatText("&cSTABILIZATION ERROR"));
            errorItem.setItemMeta(errorMeta);

            inv.setItem((randomRow * 9) + randomCol, errorItem);
        }

        StabilizerProgress progress = this.getStabilizerProgressForPlayer(player.getUniqueId());
        if (progress == null) {
            progress = new StabilizerProgress(inv);
        }

        this.addStabilizerProgress(player.getUniqueId(), progress);
        player.openInventory(inv);
    }

    /**
     * Will start/continue the solving process for the player
     * 
     * @param player Who should try to solve the sabotage
     */
    public void solve(Player player) {
        if (this.isReactor()) {
            this.solveReactor(player);
        } else if (this.isStabilizer()) {
            this.solveStabilizer(player);
        }
    }

    public boolean isStabilizer() {
        return this.getSabotageType() == SabotageType.STABILIZER;
    }

    public boolean isReactor() {
        return this.getSabotageType() == SabotageType.REACTOR;
    }

    /**
     * @param currentTicks
     * @return time remaining before end in ticks
     */
    public long getTimeRemaining(long currentTicks) {
        long duration = currentTicks - this.getStartedTicks();
        return SABOTAGE_TIME - duration;
    }

    private void createEffectCloud() {
        Location cloudLoc = this.getBlock().getLocation().add(0.5, 0.5, 0.5);
        AreaEffectCloud cloud = (AreaEffectCloud) this.getBlock().getWorld().spawnEntity(cloudLoc,
                EntityType.AREA_EFFECT_CLOUD);
        cloud.setInvulnerable(true);
        cloud.setGravity(false);
        cloud.teleport(cloudLoc);
        cloud.setDuration(Integer.MAX_VALUE);
        cloud.setRadius(2f);
        cloud.setParticle(Particle.REDSTONE, new Particle.DustOptions(Color.RED, 1.5f));
        cloud.setWaitTime(0);
        cloud.setReapplicationDelay(0);

        this.effect = cloud;
    }

    private void removeEffectCloud() {
        if (this.effect == null) {
            return;
        }

        this.effect.remove();
        this.effect = null;
    }

    public void activate(Game game) {
        if (!game.canActivateSabotage(game.getGameLoop().getTicksSinceStart())) {
            return;
        }

        this.reactorProgress.clear();
        this.stabilzierProgress.clear();

        this.createEffectCloud();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Utils
                    .formatText("&c&l[SABOTAGE]&r&c WARNING! THE " + this.getSabotageType() + " IS BEING SABOTAGED."));
            p.playSound(this.getBlock().getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
        }

        this.setStartedTicks(game.getGameLoop().getTicksSinceStart());
        this.setActive(true);
    }

    public void deactivate(Game game) {
        this.setActive(false);
        this.removeEffectCloud();

        this.started = 0;
        this.reactorProgress.clear();
        this.stabilzierProgress.clear();
        game.setLastTimeSabotageWasDisarmed(game.getGameLoop().getTicksSinceStart());

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Utils.formatText("&a&l[SABOTAGE]&r&a Fixed."));
            p.playSound(this.getBlock().getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
        }
    }
}
