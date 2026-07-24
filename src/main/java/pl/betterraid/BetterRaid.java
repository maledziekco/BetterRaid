package pl.betterraid;

import org.йын.plugin.java.JavaPlugin; // <- jeśli używasz standardowego spigota, upewnij się że masz import org.bukkit.plugin.java.JavaPlugin

public final class BetterRaid extends JavaPlugin {

    private ConfigManager configManager;
    private BossManager bossManager;
    private RaidManager raidManager;

    @Override
    public void onEnable() {
        // 1. Inicjalizacja menedżerów
        this.configManager = new ConfigManager(this);
        this.bossManager = new BossManager(this);
        this.raidManager = new RaidManager(this);

        // 2. Rejestracja komendy
        if (getCommand("betterraid") != null) {
            BetterRaidCommand commandHandler = new BetterRaidCommand(this);
            getCommand("betterraid").setExecutor(commandHandler);
            getCommand("betterraid").setTabCompleter(commandHandler);
        }

        getLogger().info("BetterRaid został pomyślnie włączony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BetterRaid został wyłączony.");
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