package pl.betterraid;

import org.bukkit.ChatColor;
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
        return 24.0;
    }

    public double getMobDamageMultiplier(EntityType type) {
        FileConfiguration config = plugin.getConfig();
        String path = "mobs-damage-multiplier." + type.name();

        if (config.contains(path)) {
            return config.getDouble(path);
        }
        return getDamageMultiplier();
    }

    // Pobiera szansę procentową z configu dla danego moba i fali
    public int getSpawnChance(int wave, EntityType type) {
        FileConfiguration config = plugin.getConfig();
        String waveGroup;

        if (wave <= 2) {
            waveGroup = "waves-1-2";
        } else if (wave <= 4) {
            waveGroup = "waves-3-4";
        } else {
            waveGroup = "waves-5-plus";
        }

        String path = "wave-spawn-chances." + waveGroup + "." + type.name();
        return config.getInt(path, 0);
    }

    public String getPrefix() {
        return colorize(plugin.getConfig().getString("messages.prefix", "&8[&cBetterRaid&8] "));
    }

    public String getNoPermissionMsg() {
        return colorize(plugin.getConfig().getString("messages.no-permission", "&cBrak uprawnień!"));
    }

    public String getConfigReloadedMsg() {
        return colorize(plugin.getConfig().getString("messages.config-reloaded", "&aPrzeładowano config!"));
    }

    public String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}