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
     * Spawnuje określoną liczbę bossów/mobów z configu wokół podanej lokalizacji.
     */
    public void spawnRaidBosses(Location location, int amount, double health) {
        if (location == null || location.getWorld() == null) return;

        for (int i = 0; i < amount; i++) {
            LivingEntity boss = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.PILLAGER);
            setupRaidMob(boss, health, null);
        }
    }

    /**
     * Spawnuje pojedynczego bossa z domyślnym HP z configu.
     */
    public LivingEntity spawnBoss(Location location) {
        double health = plugin.getConfigManager() != null ? plugin.getConfigManager().getBossHealth() : 100.0;
        return spawnBoss(location, health);
    }

    /**
     * Spawnuje pojedynczego bossa z podanym HP.
     */
    public LivingEntity spawnBoss(Location location, double health) {
        if (location == null || location.getWorld() == null) return null;
        
        LivingEntity boss = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.PILLAGER);
        setupRaidMob(boss, health, null);
        
        return boss;
    }

    /**
     * Ustawia HP z configu oraz nadaje natychmiastowe agro na gracza.
     */
    public void setupRaidMob(LivingEntity entity, double configHealth, Player targetPlayer) {
        if (entity == null || !entity.isValid()) return;

        // Opóźnienie 1 tick, żeby silnik gry nie nadpisał naszego HP
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (entity != null && entity.isValid()) {
                
                // 1. Ustawienie HP
                AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealthAttr != null) {
                    maxHealthAttr.setBaseValue(configHealth);
                    entity.setHealth(configHealth);
                }

                // 2. Nadanie natychmiastowego AGRO
                if (entity instanceof Mob) {
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
            }
        }, 1L);
    }
}