package pl.betterraid;

import org.bukkit.plugin.java.JavaPlugin;

public final class BetterRaid extends JavaPlugin {

    private ConfigManager configManager;
    private BossManager bossManager;
    private RaidManager raidManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.bossManager = new BossManager(this);
        this.raidManager = new RaidManager(this);

        // Rejestracja listenera, aby modyfikacje zdrowia i obrażeń mobów z rajdu działały poprawnie
        getServer().getPluginManager().registerEvents(new RaidListener(this), this);

        if (getCommand("betterraid") != null) {
            BetterRaidCommand commandHandler = new BetterRaidCommand(this);
            getCommand("betterraid").setExecutor(commandHandler);
            getCommand("betterraid").setTabCompleter(commandHandler);
        }

        getLogger().info("BetterRaid zostal pomyslnie wlaczony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BetterRaid zostal wylaczony.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public RaidManager getRaidManager() {
        return raidManager;
    }
}