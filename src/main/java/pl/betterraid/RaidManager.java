package pl.betterraid;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class RaidManager {

    private final BetterRaid plugin;

    public RaidManager(BetterRaid plugin) {
        this.plugin = plugin;
    }

    /**
     * Uruchamia falę rajdu, pobierając wszystko z configu.
     */
    public void startRaidWave(Player player, int waveNumber) {
        Location loc = player.getLocation();
        ConfigManager config = plugin.getConfigManager();

        // 1. Pobieramy ustawienia z configu
        double healthMultiplier = config.getHealthMultiplier();
        double damageMultiplier = config.getDamageMultiplier();

        player.sendMessage(config.colorize(config.getPrefix() + "&cRozpoczęto falę &e" + waveNumber + "&c!"));

        // 2. Sprawdzamy szansę i spawnujemy moby (np. Pillagera) na podstawie configu fal
        int pillagerChance = config.getSpawnChance(waveNumber, EntityType.PILLAGER);
        if (pillagerChance > 0) {
            // BossManager sam pobierze bazowe HP i pomnoży przez mnożnik z configu oraz da agro
            plugin.getBossManager().spawnRaidMob(loc, EntityType.PILLAGER);
        }

        // Tutaj możesz dodać też inne moby, np. Ravagera:
        int ravagerChance = config.getSpawnChance(waveNumber, EntityType.RAVAGER);
        if (ravagerChance > 0) {
            plugin.getBossManager().spawnRaidMob(loc, EntityType.RAVAGER);
        }
    }
}