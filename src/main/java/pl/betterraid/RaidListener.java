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
        // Sprawdzamy, czy zespawnowany mob to uczestnik rajdu (Raider)
        if (event.getEntity() instanceof Raider raider) {
            
            // Ignorujemy moby poza aktywnym rajdem
            if (raider.getRaid() == null) return;

            // Pobieramy szansę z configu
            double chance = plugin.getConfigManager().getExtraMobsChance();

            // Jeśli losowa wartość jest mniejsza niż szansa - klonujemy moba!
            if (Math.random() < chance) {
                World world = raider.getWorld();
                Location loc = raider.getLocation();
                
                // Spawnuje dodatkowego moba tego samego typu w tym samym miejscu
                world.spawn(loc, raider.getClass());
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Logika zdarzeń po śmierci moba
    }
}