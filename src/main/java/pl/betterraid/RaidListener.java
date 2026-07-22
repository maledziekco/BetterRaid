package pl.betterraid;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Raider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class RaidListener implements Listener {

    private final BetterRaid plugin;

    public RaidListener(BetterRaid plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRaidSpawn(EntitySpawnEvent event) {
        // Sprawdzamy czy zespawnowana istota należy do rajdu
        if (event.getEntity() instanceof Raider raider) {
            
            // Ignorujemy moby poza aktywnym rajdem
            if (raider.getRaid() == null) return;

            // Pobieramy z configu ile dodatkowych mobów ma stworzyć (dla +200% ustawiamy 2)
            int extraCount = plugin.getConfigManager().getExtraMobsMultiplier();

            World world = raider.getWorld();
            Location loc = raider.getLocation();

            // Pętla spawnuje podaną liczbę dodatkowych potworów (+200% = 2 klony na 1 moba)
            for (int i = 0; i < extraCount; i++) {
                world.spawn(loc, raider.getClass());
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Logika po śmierci moba
    }
}