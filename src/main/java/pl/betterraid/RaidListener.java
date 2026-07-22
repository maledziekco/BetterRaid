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
        List<Raider> originalRaiders = new ArrayList<>(event.getRaiders());
        int extraMultiplier = plugin.getConfigManager().getExtraMobsMultiplier();
        int waveNumber = event.getRaid().getBadOmenLevel();

        for (Raider raider : originalRaiders) {
            applyCustomizations(raider);

            Location loc = raider.getLocation();

            for (int i = 0; i < extraMultiplier; i++) {
                if (loc.getWorld() != null) {
                    Location spawnLoc = loc.clone().add((Math.random() - 0.5) * 3, 0, (Math.random() - 0.5) * 3);
                    EntityType spawnType = getMobTypeForWave(waveNumber);

                    if (loc.getWorld().spawnEntity(spawnLoc, spawnType) instanceof LivingEntity extraMob) {
                        applyCustomizations(extraMob);
                    }
                }
            }
        }
    }

    private EntityType getMobTypeForWave(int wave) {
        int roll = random.nextInt(100);

        if (wave <= 2) {
            if (roll < 60) return EntityType.PILLAGER;
            return EntityType.VINDICATOR;
        } else if (wave <= 4) {
            if (roll < 40) return EntityType.PILLAGER;
            if (roll < 70) return EntityType.VINDICATOR;
            if (roll < 90) return EntityType.WITCH;
            return EntityType.ILLUSIONER;
        } else {
            if (roll < 25) return EntityType.VINDICATOR;
            if (roll < 50) return EntityType.WITCH;
            if (roll < 75) return EntityType.EVOKER;
            return EntityType.RAVAGER;
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof LivingEntity damager && isRaidMob(damager)) {
            double damageMultiplier = plugin.getConfigManager().getDamageMultiplier();
            event.setDamage(event.getDamage() * damageMultiplier);
        }
    }

    private boolean isRaidMob(LivingEntity entity) {
        return entity instanceof Raider || entity.getType() == EntityType.WITCH;
    }

    private void applyCustomizations(LivingEntity entity) {
        // Pobieramy bazowe HP z pliku config.yml oraz globalny mnożnik
        double baseHealth = plugin.getConfigManager().getMobBaseHealth(entity.getType());
        double globalMultiplier = plugin.getConfigManager().getHealthMultiplier();
        double finalMaxHealth = baseHealth * globalMultiplier;

        AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(finalMaxHealth);
            entity.setHealth(finalMaxHealth);
        }

        // Nazwa i pasek zdrowia nad głową są całkowicie wyłączone
        entity.setCustomName(null);
        entity.setCustomNameVisible(false);
    }
}