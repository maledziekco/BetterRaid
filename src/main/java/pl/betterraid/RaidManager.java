package pl.betterraid;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

public class ConfigManager {

    private final BetterRaid plugin;

    public ConfigManager(BetterRaid plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public int getExtraMobsMultiplier() {
        return plugin.getConfig().getInt("raid.extra-mobs-multiplier", 2);
    }

    public double getDamageMultiplier() {
        return plugin.getConfig().getDouble("raid.damage-multiplier", 2.5);
    }

    public double getHealthMultiplier() {
        return plugin.getConfig().getDouble("raid.health-multiplier", 1.0);
    }

    public double getMobBaseHealth(EntityType type) {
        FileConfiguration config = plugin.getConfig();
        String path = "mobs-base-hp." + type.name();
        
        if (config.contains(path)) {
            return config.getDouble(path);
        }
        return 24.0; // Domyślna wartość, jeśli brak wpisu w configu
    }
}