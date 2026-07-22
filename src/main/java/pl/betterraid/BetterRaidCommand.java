package pl.betterraid.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.betterraid.BetterRaid;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossManager {

    private final BetterRaid plugin;
    private final Map<UUID, BossBar> activeBosses = new HashMap<>();

    public BossManager(BetterRaid plugin) {
        this.plugin = plugin;
    }

    public LivingEntity spawnBoss(Location location) {
        String typeStr = plugin.getConfigManager().getBossType();
        EntityType type;
        try {
            type = EntityType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = EntityType.RAVAGER;
        }

        LivingEntity boss = (LivingEntity) location.getWorld().spawnEntity(location, type);

        String coloredName = plugin.getConfigManager().colorize(plugin.getConfigManager().getBossName());
        boss.setCustomName(coloredName);
        boss.setCustomNameVisible(true);

        double maxHealth = plugin.getConfigManager().getBossMaxHealth();
        if (boss.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            boss.setHealth(maxHealth);
        }

        // Add visual / equipment polish
        if (boss.getEquipment() != null) {
            boss.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            boss.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        }

        // Create BossBar
        BarColor color;
        try {
            color = BarColor.valueOf(plugin.getConfigManager().getBossBarColor().toUpperCase());
        } catch (IllegalArgumentException e) {
            color = BarColor.RED;
        }

        BarStyle style;
        try {
            style = BarStyle.valueOf(plugin.getConfigManager().getBossBarStyle().toUpperCase());
        } catch (IllegalArgumentException e) {
            style = BarStyle.SOLID;
        }

        BossBar bossBar = Bukkit.createBossBar(coloredName, color, style);
        bossBar.setProgress(1.0);

        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= 100) {
                bossBar.addPlayer(player);
            }
        }

        activeBosses.put(boss.getUniqueId(), bossBar);
        return boss;
    }

    public void updateBossBar(LivingEntity boss) {
        BossBar bar = activeBosses.get(boss.getUniqueId());
        if (bar != null) {
            double maxHealth = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null ?
                    boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : boss.getMaxHealth();
            double health = Math.max(0, boss.getHealth());
            bar.setProgress(Math.min(1.0, Math.max(0.0, health / maxHealth)));
        }
    }

    public void removeBoss(LivingEntity boss) {
        BossBar bar = activeBosses.remove(boss.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    public boolean isBoss(UUID uuid) {
        return activeBosses.containsKey(uuid);
    }

    public void clearAllBosses() {
        for (BossBar bar : activeBosses.values()) {
            bar.removeAll();
        }
        activeBosses.clear();
    }
}
