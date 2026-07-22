package pl.betterraid;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final BetterRaid plugin;

    private double healthMultiplier;
    private double damageMultiplier;
    private double extraMobsChance;
    private double eliteSpawnChance;
    private String eliteHelmet;
    private String eliteChestplate;
    private String eliteWeapon;
    private String elitePrefix;

    private boolean bossEnabled;
    private String bossType;
    private String bossName;
    private double bossMaxHealth;
    private String bossBarColor;
    private String bossBarStyle;

    private String prefix;
    private String noPermissionMsg;
    private String configReloadedMsg;
    private String raidStartedMsg;
    private String bossSpawnedMsg;
    private String bossDefeatedMsg;

    public ConfigManager(BetterRaid plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.healthMultiplier = config.getDouble("raid.health-multiplier", 1.30);
        this.damageMultiplier = config.getDouble("raid.damage-multiplier", 1.20);
        this.extraMobsChance = config.getDouble("raid.extra-mobs-chance", 0.50);

        this.eliteSpawnChance = config.getDouble("elites.spawn-chance", 0.25);
        this.eliteHelmet = config.getString("elites.helmet", "DIAMOND_HELMET");
        this.eliteChestplate = config.getString("elites.chestplate", "DIAMOND_CHESTPLATE");
        this.eliteWeapon = config.getString("elites.weapon", "NETHERITE_SWORD");
        this.elitePrefix = config.getString("elites.display-prefix", "&e[ELITA] ");

        this.bossEnabled = config.getBoolean("boss.enabled", true);
        this.bossType = config.getString("boss.type", "RAVAGER");
        this.bossName = config.getString("boss.name", "&c&lBOSS");
        this.bossMaxHealth = config.getDouble("boss.max-health", 350.0);
        this.bossBarColor = config.getString("boss.bossbar-color", "RED");
        this.bossBarStyle = config.getString("boss.bossbar-style", "SOLID");

        this.prefix = colorize(config.getString("messages.prefix", "&8[&cBetterRaid&8] "));
        this.noPermissionMsg = colorize(prefix + config.getString("messages.no-permission", "&cBrak uprawnień!"));
        this.configReloadedMsg = colorize(prefix + config.getString("messages.config-reloaded", "&aPrzeładowano config!"));
        this.raidStartedMsg = colorize(prefix + config.getString("messages.raid-started", "&aRozpoczęto rajd!"));
        this.bossSpawnedMsg = colorize(prefix + config.getString("messages.boss-spawned", "&cZespawnowano Bossa!"));
        this.bossDefeatedMsg = colorize(prefix + config.getString("messages.boss-defeated", "&aBoss został pokonany!"));
    }

    public String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    // Getters
    public double getHealthMultiplier() { return healthMultiplier; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public double getExtraMobsChance() { return extraMobsChance; }
    public double getEliteSpawnChance() { return eliteSpawnChance; }
    public String getEliteHelmet() { return eliteHelmet; }
    public String getEliteChestplate() { return eliteChestplate; }
    public String getEliteWeapon() { return eliteWeapon; }
    public String getElitePrefix() { return elitePrefix; }

    public boolean isBossEnabled() { return bossEnabled; }
    public String getBossType() { return bossType; }
    public String getBossName() { return bossName; }
    public double getBossMaxHealth() { return bossMaxHealth; }
    public String getBossBarColor() { return bossBarColor; }
    public String getBossBarStyle() { return bossBarStyle; }

    public String getPrefix() { return prefix; }
    public String getNoPermissionMsg() { return noPermissionMsg; }
    public String getConfigReloadedMsg() { return configReloadedMsg; }
    public String getRaidStartedMsg() { return raidStartedMsg; }
    public String getBossSpawnedMsg() { return bossSpawnedMsg; }
    public String getBossDefeatedMsg() { return bossDefeatedMsg; }
}