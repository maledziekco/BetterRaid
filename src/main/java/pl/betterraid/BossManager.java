package pl.betterraid;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class BossManager {

    private final BetterRaid plugin;

    public BossManager(BetterRaid plugin) {
        this.plugin = plugin;
    }

    /**
     * Metoda do spawnowania/konfiguracji moba rajdowego.
     * Ustawia mu HP z configu oraz nadaje natychmiastowe agro na gracza.
     */
    public void setupRaidMob(LivingEntity entity, double configHealth, Player targetPlayer) {
        if (entity == null || !entity.isValid()) return;

        // Używamy opóźnienia 1 tick (1/20 sekundy), żeby silnik gry 
        // nie nadpisał domyślnych statystyk moba tuż po jego zespawnowaniu.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (entity != null && entity.isValid()) {
                
                // 1. Ustawienie HP z configu
                AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealthAttr != null) {
                    maxHealthAttr.setBaseValue(configHealth);
                    entity.setHealth(configHealth);
                }

                // 2. Nadanie natychmiastowego AGRO
                if (entity instanceof Mob) {
                    Mob mob = (Mob) entity;
                    
                    // Jeśli podano konkretnego gracza, atakuje go. 
                    // Jeśli nie, szuka najbliższego gracza w pobliżu.
                    if (targetPlayer != null && targetPlayer.isOnline()) {
                        mob.setTarget(targetPlayer);
                    } else {
                        // Znajdź najbliższego gracza w promieniu 32 bloków
                        Player nearest = entity.getWorld().getPlayers().stream()
                                .filter(p -> p.getLocation().distanceSquared(entity.getLocation()) <= 32 * 32)
                                .min((p1, p2) -> Double.compare(
                                        p1.getLocation().distanceSquared(entity.getLocation()),
                                        p2.getLocation().distanceSquared(entity.getLocation())
                                ))
                                .orElse(null);
                        
                        if (nearest != null) {
                            mob.setTarget(nearest);
                        }
                    }
                }
            }
        }, 1L);
    }
}