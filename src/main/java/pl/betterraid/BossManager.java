package pl.betterraid;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class BossManager {

    private final BetterRaid plugin;

    public BossManager(BetterRaid plugin) {
        this.plugin = plugin;
    }

    /**
     * Spawnuje moba z rajdu, pobierając jego HP bezpośrednio z configu (bazowe HP * mnożnik).
     */
    public LivingEntity spawnRaidMob(Location location, EntityType type) {
        if (location == null || location.getWorld() == null) return null;

        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type);
        
        // Pobieramy HP zgodnie z ConfigManagerem
        double baseHp = plugin.getConfigManager().getMobBaseHealth(type);
        double multiplier = plugin.getConfigManager().getHealthMultiplier();
        double finalHealth = baseHp * multiplier;

        // DEBUG: Sprawdzenie w konsoli, co dokładnie się liczy i ustawia
        plugin.getLogger().info("SPAWN MOB: " + type.name() + " | Config HP: " + baseHp + " | Multiplier: " + multiplier + " | Obliczone HP: " + finalHealth);

        // Ustawiamy HP od razu przy spawnie
        AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(finalHealth);
            entity.setHealth(finalHealth);
            
            // DEBUG: Potwierdzenie przypisania w grze
            plugin.getLogger().info("USTAWIONO HP! Aktualne Max HP moba: " + maxHealthAttr.getValue() + " | Aktualne HP: " + entity.getHealth());
        }

        setupMobTarget(entity, null);
        return entity;
    }

    /**
     * Stara metoda spawnBoss - teraz poprawnie korzysta z ConfigManagera.
     */
    public LivingEntity spawnBoss(Location location) {
        return spawnRaidMob(location, EntityType.PILLAGER);
    }

    /**
     * Nadaje natychmiastowe agro na gracza.
     */
    public void setupMobTarget(LivingEntity entity, Player targetPlayer) {
        if (entity == null || !entity.isValid()) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (entity != null && entity.isValid() && entity instanceof Mob) {
                Mob mob = (Mob) entity;
                
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    mob.setTarget(targetPlayer);
                } else {
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
        }, 1L);
    }
}