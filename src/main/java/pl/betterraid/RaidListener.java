package pl.betterraid;

import org.bukkit.ChatColor;
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
        
        // Pobieramy numer bieżącej fali (np. 1, 2, 3...)
        int waveNumber = event.getRaid().getBadOmenLevel(); // lub estymacja fali z rajdu

        for (Raider raider : originalRaiders) {
            applyCustomizations(raider);

            Location loc = raider.getLocation();

            // Generujemy dodatkowe moby na podstawie NUMERU FALI
            for (int i = 0; i < extraMultiplier; i++) {
                if (loc.getWorld() != null) {
                    Location spawnLoc = loc.clone().add((Math.random() - 0.5) * 3, 0, (Math.random() - 0.5) * 3);
                    
                    // Dobieramy typ moba pod konkretną falę!
                    EntityType spawnType = getMobTypeForWave(waveNumber);

                    if (loc.getWorld().spawnEntity(spawnLoc, spawnType) instanceof LivingEntity extraMob) {
                        applyCustomizations(extraMob);
                    }
                }
            }
        }
    }

    /**
     * Zwraca typ moba dopasowany do trudności danej fali
     */
    private EntityType getMobTypeForWave(int wave) {
        int roll = random.nextInt(100);

        // FALA 1-2: Lekka piechota (Kusznicy + Siekacze)
        if (wave <= 2) {
            if (roll < 60) return EntityType.PILLAGER;
            return EntityType.VINDICATOR;
        } 
        // FALA 3-4: Dołączają Czarownice i Iluzjoniści
        else if (wave <= 4) {
            if (roll < 40) return EntityType.PILLAGER;
            if (roll < 70) return EntityType.VINDICATOR;
            if (roll < 90) return EntityType.WITCH;
            return EntityType.ILLUSIONER;
        } 
        // FALA 5+: Ciężka artyleria (Przywoływacze i Dewastatorzy)
        else {
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

        if (event.getEntity() instanceof LivingEntity entity && isRaidMob(entity)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> updateHealthTag(entity), 1L);
        }
    }

    private boolean isRaidMob(LivingEntity entity) {
        return entity instanceof Raider || entity.getType() == EntityType.WITCH;
    }

    private void applyCustomizations(LivingEntity entity) {
        double healthMultiplier = plugin.getConfigManager().getHealthMultiplier();
        double baseHealth = getCustomMaxHealthForType(entity.getType());
        double newMaxHealth = baseHealth * healthMultiplier;

        AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(newMaxHealth);
            entity.setHealth(newMaxHealth);
        }

        updateHealthTag(entity);
        entity.setCustomNameVisible(true);
    }

    private void updateHealthTag(LivingEntity entity) {
        if (entity.isDead() || !entity.isValid()) return;

        String baseName = getCustomNameForType(entity.getType());
        int currentHp = (int) Math.max(0, entity.getHealth());

        AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        int maxHp = maxHealthAttr != null ? (int) maxHealthAttr.getValue() : currentHp;

        String healthTag = colorize(" &7[" + getHealthColor(currentHp, maxHp) + currentHp + "&7/&a" + maxHp + " HP&7]");
        entity.setCustomName(baseName + healthTag);
    }

    private double getCustomMaxHealthForType(EntityType type) {
        return switch (type) {
            case RAVAGER -> 100.0;
            case EVOKER -> 40.0;
            case VINDICATOR -> 35.0;
            case WITCH -> 30.0;
            case PILLAGER -> 24.0;
            case ILLUSIONER -> 32.0;
            default -> 24.0;
        };
    }

    private String getHealthColor(int current, int max) {
        double ratio = (double) current / max;
        if (ratio > 0.6) return "&a";
        if (ratio > 0.3) return "&e";
        return "&c";
    }

    private String getCustomNameForType(EntityType type) {
        return switch (type) {
            case PILLAGER -> colorize("&cKusznik Najazdu");
            case VINDICATOR -> colorize("&4Siekacz Najazdu");
            case EVOKER -> colorize("&5Przywoływacz Najazdu");
            case RAVAGER -> colorize("&6Dewastator Najazdu");
            case WITCH -> colorize("&2Czarownica Najazdu");
            case ILLUSIONER -> colorize("&9Iluzjonista Najazdu");
            default -> colorize("&cWojownik Najazdu");
        };
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}