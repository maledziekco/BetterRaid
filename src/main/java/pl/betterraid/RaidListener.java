package pl.betterraid;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Raider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;

import java.util.ArrayList;
import java.util.List;

public class RaidListener implements Listener {

    private final BetterRaid plugin;

    public RaidListener(BetterRaid plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWaveSpawn(RaidSpawnWaveEvent event) {
        List<Raider> originalRaiders = new ArrayList<>(event.getRaiders());
        int extraMultiplier = plugin.getConfigManager().getExtraMobsMultiplier(); // Domyślnie 2 (+200%)

        for (Raider raider : originalRaiders) {
            applyCustomizations(raider);

            Location loc = raider.getLocation();
            EntityType type = raider.getType();

            // Spawnowanie dodatkowych mobów (+200%)
            for (int i = 0; i < extraMultiplier; i++) {
                if (loc.getWorld() != null) {
                    Location spawnLoc = loc.clone().add((Math.random() - 0.5) * 2, 0, (Math.random() - 0.5) * 2);
                    Raider extraRaider = (Raider) loc.getWorld().spawnEntity(spawnLoc, type);
                    applyCustomizations(extraRaider);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Obrażenia zadawane przez raiderów
        if (event.getDamager() instanceof Raider) {
            double damageMultiplier = plugin.getConfigManager().getDamageMultiplier();
            event.setDamage(event.getDamage() * damageMultiplier);
        }

        // Aktualizacja HP nad głową po otrzymaniu obrażeń
        if (event.getEntity() instanceof Raider raider) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> updateHealthTag(raider), 1L);
        }
    }

    private void applyCustomizations(Raider raider) {
        double healthMultiplier = plugin.getConfigManager().getHealthMultiplier();
        double baseHealth = getCustomMaxHealthForType(raider.getType());
        double newMaxHealth = baseHealth * healthMultiplier;

        AttributeInstance maxHealthAttr = raider.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(newMaxHealth);
            raider.setHealth(newMaxHealth);
        }

        updateHealthTag(raider);
        raider.setCustomNameVisible(true);
    }

    private void updateHealthTag(Raider raider) {
        if (raider.isDead() || !raider.isValid()) return;

        String baseName = getCustomNameForType(raider.getType());
        int currentHp = (int) Math.max(0, raider.getHealth());

        AttributeInstance maxHealthAttr = raider.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        int maxHp = maxHealthAttr != null ? (int) maxHealthAttr.getValue() : currentHp;

        String healthTag = colorize(" &7[" + getHealthColor(currentHp, maxHp) + currentHp + "&7/&a" + maxHp + " HP&7]");
        raider.setCustomName(baseName + healthTag);
    }

    /**
     * Dedykowana ilość punktów życia (HP) dla każdego moba z osobna
     */
    private double getCustomMaxHealthForType(EntityType type) {
        return switch (type) {
            case RAVAGER -> 100.0;     // Dewastator — potężny czołg (100 HP)
            case EVOKER -> 40.0;       // Przywoływacz — mag (40 HP)
            case VINDICATOR -> 35.0;   // Siekacz — silny wojownik (35 HP)
            case WITCH -> 30.0;        // Czarownica (30 HP)
            case PILLAGER -> 24.0;     // Kusznik — standardowa piechota (24 HP)
            case ILLUSIONER -> 32.0;   // Iluzjonista (32 HP)
            default -> 24.0;
        };
    }

    private String getHealthColor(int current, int max) {
        double ratio = (double) current / max;
        if (ratio > 0.6) return "&a"; // Zielony dla > 60%
        if (ratio > 0.3) return "&e"; // Żółty dla > 30%
        return "&c";                 // Czerwony dla niskiego HP
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