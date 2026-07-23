package pl.betterraid;

import org.bukkit.plugin.java.JavaPlugin;
import pl.betterraid.commands.BetterRaidCommand;
package pl.betterraid;

import org.bukkit.plugin.java.JavaPlugin;
import pl.betterraid.commands.BroadcastCommand;

public final class BetterRaid extends JavaPlugin {

    private ConfigManager configManager;
    private BossManager bossManager;

    @Override
    public void onEnable() {
        // Inicjalizacja menedżerów
        this.configManager = new ConfigManager(this);
        this.bossManager = new BossManager(this);

        // Rejestracja komend
        if (getCommand("ogloszenie") != null) {
            getCommand("ogloszenie").setExecutor(new BroadcastCommand());
        }
        if (getCommand("betterraid") != null) {
            getCommand("betterraid").setExecutor(new BetterRaidCommand(this));
        }

        getLogger().info("Plugin BetterRaid został pomyślnie uruchomiony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin BetterRaid został wyłączony.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }
}