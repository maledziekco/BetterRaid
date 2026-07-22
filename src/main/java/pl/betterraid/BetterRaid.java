package pl.betterraid;

import org.bukkit.plugin.java.JavaPlugin;

public class BetterRaid extends JavaPlugin {

    private ConfigManager configManager;
    private RaidManager raidManager;
    private BossManager bossManager;
    private RewardManager rewardManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.raidManager = new RaidManager(this);
        this.bossManager = new BossManager(this);
        this.rewardManager = new RewardManager(this);

        if (getCommand("raid") != null) {
            getCommand("raid").setExecutor(new BetterRaidCommand(this));
        }

        getServer().getPluginManager().registerEvents(new RaidListener(this), this);

        getLogger().info("Plugin BetterRaid zostal pomyslnie wlaczony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin BetterRaid zostal wylaczony.");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public RaidManager getRaidManager() { return raidManager; }
    public BossManager getBossManager() { return bossManager; }
    public RewardManager getRewardManager() { return rewardManager; }
}
