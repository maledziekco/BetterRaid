package pl.betterraid;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Raider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class RaidListener implements Listener {

    private final BetterRaid plugin;
    private final Random random = new Random();

    public RaidListener(BetterRaid plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWaveSpawn(RaidSpawnWaveEvent event) {
        try {
            Object raid = event.getClass().getMethod("getRaid").invoke(event);
            if (raid == null) return;

            int badOmenLevel = (int) raid.getClass().getMethod("getBadOmenLevel").invoke(raid);
            int spawnedGroups = (int) raid.getClass().getMethod("getSpawnedGroups").invoke(raid);
            
            int maxWaves;
            switch (badOmenLevel) {
                case 1: maxWaves = 4; break;
                case 2: maxWaves = 6; break;
                case 3: maxWaves = 8; break;
                case 4: maxWaves = 10; break;
                default: maxWaves = badOmenLevel >= 5 ? 14 : 4; break;
            }

            if (spawnedGroups > maxWaves) {
                return;
            }

            @SuppressWarnings("unchecked")
            List<Raider> originalRaiders = new ArrayList<>((List<Raider>) raid.getClass().getMethod("getRaiders").invoke(raid));
            int extraMultiplier = plugin.getConfigManager().getExtraMobsMultiplier();

            for (Raider raider : originalRaiders) {
                applyCustomizations(raider);

                Location loc = raider.getLocation();

                for (int i = 0; i < extraMultiplier; i++) {
                    if (loc.getWorld() != null) {
                        Location spawnLoc = loc.clone().add((Math.random() - 0.5) * 3, 0, (Math.random() - 0.5) * 3);
                        EntityType spawnType = getMobTypeForWave(spawnedGroups, raider.getType());

                        if (loc.getWorld().spawnEntity(spawnLoc, spawnType) instanceof LivingEntity extraMob) {
                            applyCustomizations(extraMob);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    @EventHandler
    public void onRaidStop(RaidStopEvent event) {
        try {
            Object reason = event.getClass().getMethod("getReason").invoke(event);
            if (reason != null && reason.toString().equals("TIMEOUT")) {
                Object raid = event.getClass().getMethod("getRaid").invoke(event);
                if (raid != null) {
                    int badOmenLevel = (int) raid.getClass().getMethod("getBadOmenLevel").invoke(raid);
                    raid.getClass().getMethod("setBadOmenLevel", int.class).invoke(raid, badOmenLevel);
                }
            }
        } catch (Exception ignored) {}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (isRaidMob(entity)) {
            applyCustomizations(entity);
        }
    }

    private EntityType getMobTypeForWave(int wave, EntityType fallbackType) {
        int roll = random.nextInt(100);
        int currentSum = 0;

        EntityType[] possibleTypes = {
            EntityType.PILLAGER,
            EntityType.VINDICATOR,
            EntityType.ILLUSIONER,
            EntityType.WITCH,
            EntityType.EVOKER,
            EntityType.RAVAGER
        };

        for (EntityType type : possibleTypes) {
            int chance = plugin.getConfigManager().getSpawnChance(wave, type);
            if (chance > 0) {
                currentSum += chance;
                if (roll < currentSum) {
                    return type;
                }
            }
        }

        return fallbackType;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof LivingEntity damager && isRaidMob(damager)) {
            double specificMultiplier = plugin.getConfigManager().getMobDamageMultiplier(damager.getType());
            event.setDamage(event.getDamage() * specificMultiplier);

            if (damager instanceof Ravager && event.getEntity() instanceof Player player) {
                Vector direction = player.getLocation().toVector().subtract(damager.getLocation().toVector()).normalize();
                player.setVelocity(direction.multiply(1.2).setY(0.5));
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ATTACK, 1.0f, 0.5f);
            }
        }
    }

    private boolean isRaidMob(LivingEntity entity) {
        return entity instanceof Raider || entity.getType() == EntityType.WITCH;
    }

    private void applyCustomizations(LivingEntity entity) {
        if (entity == null || !entity.isValid()) return;

        double baseHealth = plugin.getConfigManager().getMobBaseHealth(entity.getType());
        double globalMultiplier = plugin.getConfigManager().getHealthMultiplier();
        double finalMaxHealth = baseHealth * globalMultiplier;

        // Bezpieczne pobieranie atrybutu Max Health dla różnych wersji silnika
        Attribute attrMaxHealth = null;
        try {
            attrMaxHealth = Attribute.valueOf("MAX_HEALTH");
        } catch (IllegalArgumentException e) {
            try {
                attrMaxHealth = Attribute.valueOf("GENERIC_MAX_HEALTH");
            } catch (IllegalArgumentException ignored) {}
        }

        if (attrMaxHealth != null) {
            AttributeInstance maxHealthAttr = entity.getAttribute(attrMaxHealth);
            if (maxHealthAttr != null) {
                maxHealthAttr.setBaseValue(finalMaxHealth);
                entity.setHealth(finalMaxHealth);
            }
        }

        entity.setCustomName(null);
        entity.setCustomNameVisible(false);

        if (entity instanceof Mob mob) {
            findAndSetTarget(mob);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!mob.isValid() || mob.isDead()) {
                        cancel();
                        return;
                    }
                    findAndSetTarget(mob);

                    if (mob instanceof Ravager && mob.getTarget() instanceof Player target) {
                        if (mob.getLocation().distanceSquared(target.getLocation()) <= 16) {
                            if (random.nextInt(100) < 25) {
                                Vector knockback = target.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize();
                                target.setVelocity(knockback.multiply(1.5).setY(0.6));
                                target.damage(4.0, mob);
                                mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 0.8f, 1.0f);
                            }
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }

    private void findAndSetTarget(Mob mob) {
        Player target = mob.getWorld().getPlayers().stream()
                .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(mob.getLocation())))
                .orElse(null);

        if (target != null && target.getLocation().distanceSquared(mob.getLocation()) <= 2304) {
            mob.setTarget(target);
        }
    }
}