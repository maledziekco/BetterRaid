package pl.betterraid;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Raider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class RaidListener implements Listener {

    private final BetterRaid plugin;
    private static final String CLONE_KEY = "BR_CLONED";

    public RaidListener(BetterRaid plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRaidSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Raider raider) {

            // Modyfikujemy statystyki (życie) zespawnowanego moba
            applyStats(raider);

            // Jeśli mob jest klonem, nie klonujemy go po raz kolejny
            if (raider.hasMetadata(CLONE_KEY)) return;

            // Opóźnienie 1 tick (0.05s) - czekamy aż Paper przypisze rajd do moba
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (raider.isDead() || !raider.isValid()) return;
                if (raider.getRaid() == null) return;

                int extraCount = plugin.getConfigManager().getExtraMobsMultiplier();
                World world = raider.getWorld();
                Location loc = raider.getLocation();

                // Spawnowanie dodatkowych +200% mobów
                for (int i = 0; i < extraCount; i++) {
                    Raider clone = (Raider) world.spawn(loc, raider.getClass());
                    clone.setMetadata(CLONE_KEY, new FixedMetadataValue(plugin, true));
                    applyStats(clone);
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Zwiększanie obrażeń dla mobów z rajdu
        if (event.getDamager() instanceof Raider raider) {
            double damageMultiplier = plugin.getConfigManager().getDamageMultiplier();
            event.setDamage(event.getDamage() * damageMultiplier);
        }
    }

    private void applyStats(Raider raider) {
        double healthMultiplier = plugin.getConfigManager().getHealthMultiplier();
        AttributeInstance maxHealthAttr = raider.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (maxHealthAttr != null) {
            double newHealth = maxHealthAttr.getBaseValue() * healthMultiplier;
            maxHealthAttr.setBaseValue(newHealth);
            raider.setHealth(newHealth);
        }
    }
}