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
import org.bukkit.event.raid.RaidTriggerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RaidListener implements Listener {

    private final BetterRaid plugin;
    private final Random random = new Random();

    public RaidListener(BetterRaid plugin) {
        this.plugin = plugin;
    }

    // Ustawianie liczby fal na podstawie poziomu Bad Omen zgodnie z Twoją tabelą
    @EventHandler
    public void onRaidTrigger(RaidTriggerEvent event) {
        int badOmenLevel = event.getRaid().getBadOmenLevel();
        int targetWaves;

        switch (badOmenLevel) {
            case 1:
                targetWaves = 4;
                break;
            case 2:
                targetWaves = 6;
                break;
            case 3:
                targetWaves = 8;
                break;
            case 4:
                targetWaves = 10;
                break;
            default:
                // Dla poziomu 5 i wyższych ustawiamy 14 fal (lub więcej, jeśli podasz wyższy poziom)
                targetWaves = badOmenLevel >= 5 ? 14 : 4;
                break;
        }

        event.getRaid().setTotalWaves(targetWaves);
    }

    @EventHandler
    public void onWaveSpawn(RaidSpawnWaveEvent event) {
        List<Raider> originalRaiders = new ArrayList<>(event.getRaiders());
        int extraMultiplier = plugin.getConfigManager().getExtraMobsMultiplier();
        int waveNumber = event.getRaid().getBadOmenLevel();

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
        double baseHealth = plugin.getConfigManager().getMobBaseHealth(entity.getType());
        double globalMultiplier = plugin.getConfigManager().getHealthMultiplier();
        double finalMaxHealth = baseHealth * globalMultiplier;

        AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(finalMaxHealth);
            entity.setHealth(finalMaxHealth);
        }

        entity.setCustomName(null);
        entity.setCustomNameVisible(false);
    }
}