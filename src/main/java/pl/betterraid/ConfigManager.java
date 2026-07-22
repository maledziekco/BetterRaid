package pl.betterraid;

import org.bukkit.ChatColor;

public class ConfigManager {

    private final BetterRaid plugin;

    public ConfigManager(BetterRaid plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public String getPrefix() {
        return plugin.getConfig().getString("prefix", "&8[&bBetterRaid&8] ");
    }

    public String getNoPermissionMsg() {
        return colorize(getPrefix() + "&cBrak uprawnien!");
    }

    public String getConfigReloadedMsg() {
        return colorize(getPrefix() + "&aKonfiguracja zostala przeladowana!");
    }

    public String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
}
