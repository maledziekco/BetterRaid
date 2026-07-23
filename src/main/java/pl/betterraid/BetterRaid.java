package pl.betterraid;

import org.bukkit.plugin.java.JavaPlugin;
import pl.betterraid.commands.BroadcastCommand;

public final class BetterRaid extends JavaPlugin {

    @Override
    public void onEnable() {
        // Rejestracja komendy /ogloszenie
        if (getCommand("ogloszenie") != null) {
            getCommand("ogloszenie").setExecutor(new BroadcastCommand());
        }
        
        getLogger().info("Plugin BetterRaid został pomyślnie uruchomiony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin BetterRaid został wyłączony.");
    }
}