package pl.betterraid;

import org.bukkit.plugin.java.JavaPlugin;

public class BetterRaid extends JavaPlugin {

    private ConfigManager configManager;
    private RaidManager raidManager;
    private BossManager bossManager;
    private RewardManager rewardManager;

    @Override
    public void onEnable() {
        // 1. Inicjalizacja konfiguracji
        this.configManager = new ConfigManager(this);

        // 2. Inicjalizacja managerów
        this.raidManager = new RaidManager(this);
        this.bossManager = new BossManager(this);
        this.rewardManager = new RewardManager(this);

        // 3. Rejestracja komend
        if (getCommand("raid") != null) {
           getCommand("raid").setExecutor(new BetterRaidCommand(this));
        }

        // 4. Rejestracja listenerów (eventów)
        getServer().getPluginManager().registerEvents(new RaidListener(this), this);

        getLogger().info("Plugin BetterRaid zostal pomyslnie wlaczony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin BetterRaid zostal wylaczony.");
    }

    // Gettery do managerów
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RaidManager getRaidManager() {
        return raidManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }
}
