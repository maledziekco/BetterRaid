package pl.betterraid.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import pl.betterraid.BetterRaid;

public class RaidCommand implements CommandExecutor {
    private final BetterRaid plugin;

    public RaidCommand(BetterRaid plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("betterraid.admin")) {
                sender.sendMessage(plugin.getConfigManager().getNoPermissionMsg());
                return true;
            }
            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(plugin.getConfigManager().getConfigReloadedMsg());
            return true;
        }
        return true;
    }
}
