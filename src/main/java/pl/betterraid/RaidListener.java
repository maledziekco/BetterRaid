package pl.betterraid;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;

import java.util.ArrayList;
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
        int badOmenLevel = event.getRaid().getBadOmenLevel();
        int maxWaves;

        switch (badOmenLevel) {
            case 1: maxWaves = 4; break;
            case 2: maxWaves = 6; break;
            case 3: maxWaves = 8; break;
            case 4: maxWaves = 10; break;
            default: maxWaves = badOmenLevel >= 5 ? 14 : 4; break;
        }

        // Jeśli aktualna fala przekracza limit, przerywamy, nie niszcząc rajdu przez clear()
        if (event.getRaid().getSpawnedGroups() > maxWaves) {
            return;
        }

        List<Raider> originalRaiders = new ArrayList<>(event.getRaiders());
        int extraMultiplier = plugin.getConfigManager().getExtraMobsMultiplier();
        int waveNumber = event.getRaid().getSpawnedGroups();

        for (Raider raider : originalRaiders) {
            applyCustomizations(raider);

            Location loc = raider.getLocation();

            for (int i = 0; i < extraMultiplier; i++) {
                if (loc.getWorld() != null) {
                    Location spawnLoc = loc.clone().add((Math.random() - 0.5) * 3, 0, (Math.random() - 0.5) * 3);
                    EntityType spawnType = getMobTypeForWave(waveNumber, raider.getType());

                    if (loc.getWorld().spawnEntity(spawnLoc, spawnType) instanceof LivingEntity extraMob) {
                        applyCustomizations(extraMob);
                    }
                }
            }
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
        }
    }

    private boolean isRaidMob(LivingEntity entity) {
        return entity instanceof Raider || entity.getType() == EntityType.WITCH;
    }

    private void applyCustomizations(LivingEntity entity) {
        // Opóźnienie 2 ticków pozwala silnikowi gry zakończyć inicjalizację rajdu, zanim nadpiszemy HP
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (entity == null || !entity.isValid()) return;

            double baseHealth = plugin.getConfigManager().getMobBaseHealth(entity.getType());
            double globalMultiplier = plugin.getConfigManager().getHealthMultiplier();
            double finalMaxHealth = baseHealth * globalMultiplier;

            // Bezpieczne pobieranie atrybutu HP dla różnych wersji serwera (1.20+ / 1.21+)
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
                    // Usuwamy stare modyfikatory narzucone przez silnik rajdu
                    maxHealthAttr.getModifiers().forEach(maxHealthAttr::removeModifier);
                    
                    // Ustawiamy nową bazę punktów życia
                    maxHealthAttr.setBaseValue(finalMaxHealth);
                    
                    // Ustawiamy aktualne życie na pełną nową pulę
                    entity.setHealth(finalMaxHealth);
                }
            }

            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
        }, 2L);
    }
}