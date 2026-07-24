package pl.betterraid;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BetterRaidCommand implements CommandExecutor, TabCompleter {

    private final BetterRaid plugin;

    public BetterRaidCommand(BetterRaid plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("betterraid.admin")) {
            sender.sendMessage(plugin.getConfigManager().getNoPermissionMsg());
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(plugin.getConfigManager().getConfigReloadedMsg());
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("spawnboss")) {
            if (sender instanceof Player player) {
                // Pobieramy HP bezpośrednio z configu i spawnujemy bossa
                double health = plugin.getConfigManager().getBossHealth();
                
                plugin.getBossManager().spawnBoss(player.getLocation(), health);
                player.sendMessage(plugin.getConfigManager().colorize(plugin.getConfigManager().getPrefix() + "&aZespawnowano Bossa z HP: &e" + health));
            } else {
                sender.sendMessage("Tę komendę może wykonać tylko gracz.");
            }
            return true;
        }

        sender.sendMessage(plugin.getConfigManager().colorize(plugin.getConfigManager().getPrefix() + "&7Użycie: &e/betterraid <reload|spawnboss>"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("spawnboss");
        }
        return completions;
    }
}