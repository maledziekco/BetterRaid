package pl.betterraid;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RewardManager {

    private final BetterRaid plugin;

    public RewardManager(BetterRaid plugin) {
        this.plugin = plugin;
    }

    public void giveWaveRewards(Player player, int waveNumber) {
        if (player == null || !player.isOnline()) return;

        // Przykładowa ścieżka w configu dla nagród za falę, np. rewards.wave-rewards.wave-1
        String path = "rewards.wave-" + waveNumber;
        
        if (!plugin.getConfig().contains(path)) {
            // Jeśli brak nagród dla konkretnej fali, dajemy standardowe nagrody uniwersalne
            path = "rewards.default-wave-reward";
        }

        // Pobieranie listy komend z configu (np. "give %player% diamond 1", "eco give %player% 100")
        List<String> commands = plugin.getConfig().getStringList(path + ".commands");
        for (String cmd : commands) {
            String parsedCmd = cmd.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCmd);
        }

        // Wiadomość o nagrodzie
        String rewardMsg = plugin.getConfig().getString("messages.wave-reward", "&aOtrzymałeś nagrodę za ukończenie fali &e" + waveNumber + "&a!");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', rewardMsg));
    }

    public void giveRaidCompletionRewards(Player player) {
        if (player == null || !player.isOnline()) return;

        List<String> commands = plugin.getConfig().getStringList("rewards.raid-complete.commands");
        for (String cmd : commands) {
            String parsedCmd = cmd.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCmd);
        }

        String completeMsg = plugin.getConfig().getString("messages.raid-complete", "&aGratulacje! Ukończyłeś cały rajd i zdobyłeś główne nagrody!");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', completeMsg));
    }
}