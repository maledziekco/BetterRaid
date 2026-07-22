package pl.betterraid;

import org.bukkit.plugin.java.JavaPlugin;
import pl.betterraid.boss.BossManager;
import pl.betterraid.command.BetterRaidCommand;
import pl.betterraid.config.ConfigManager;
import pl.betterraid.listener.RaidListener;
import pl.betterraid.raid.RaidManager;
import pl.betterraid.reward.RewardManager;

public final class BetterRaid extends JavaPlugin {

    private static BetterRaid instance;
    private ConfigManager configManager;
    private RaidManager raidManager;
    private BossManager bossManager;
    private RewardManager rewardManager;

    @Override
    public void onEnable() {
        instance = this;

        // Configuration setup
        this.configManager = new ConfigManager(this);

        // Managers initialization
        this.bossManager = new BossManager(this);
        this.rewardManager = new RewardManager(this);
        this.raidManager = new RaidManager(this);

        // Register event listener
        getServer().getPluginManager().registerEvents(new RaidListener(this), this);

        // Register commands
        if (getCommand("betterraid") != null) {
            BetterRaidCommand cmd = new BetterRaidCommand(this);
            getCommand("betterraid").setExecutor(cmd);
            getCommand("betterraid").setTabCompleter(cmd);
        }

        getLogger().info("BetterRaid został pomyślnie włączony na Paper 1.21!");
    }

    @Override
    public void onDisable() {
        if (bossManager != null) {
            bossManager.clearAllBosses();
        }
        getLogger().info("BetterRaid został wyłączony.");
    }

    public static BetterRaid getInstance() {
        return instance;
    }

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
