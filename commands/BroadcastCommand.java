package pl.betterraid.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class BroadcastCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("betterraid.broadcast")) {
            sender.sendMessage(ChatColor.RED + "Nie masz uprawnień do użycia tej komendy!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Użycie: /ogloszenie <tekst>");
            return true;
        }

        String rawMessage = String.join(" ", args);
        String formattedText = ChatColor.translateAlternateColorCodes('&', rawMessage);

        Component titleComponent = LegacyComponentSerializer.legacySection().deserialize(formattedText);
        Component subtitleComponent = LegacyComponentSerializer.legacySection().deserialize(ChatColor.YELLOW + "Wiadomość od administracji");

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(4000),
                Duration.ofMillis(500)
        );

        Title title = Title.title(titleComponent, subtitleComponent, times);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(title);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        }

        sender.sendMessage(ChatColor.GREEN + "Wysłano ogłoszenie na środek ekranu do wszystkich graczy!");
        return true;
    }
}