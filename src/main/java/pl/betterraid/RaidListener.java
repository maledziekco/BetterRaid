package pl.betterraid;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.raid.Raid;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class RaidListener implements Listener {

    private final BetterRaid plugin;
    private final Random random = new Random();
    
    private final Map<Raid, BossBar> raidBossBars = new HashMap<>();

    public RaidListener(BetterRaid plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWaveSpawn(RaidSpawnWaveEvent event) {
        Raid raid = event.getRaid();
        int badOmenLevel = raid.getBadOmenLevel();
        int maxWaves;

        switch (badOmenLevel) {
            case 1: maxWaves = 4; break;
            case 2: maxWaves = 6; break;
            case 3: maxWaves = 8; break;
            case 4: maxWaves = 10; break;
            default: maxWaves = badOmenLevel >= 5 ? 14 : 4; break;
        }

        int waveNumber = raid.getSpawnedGroups();
        if (waveNumber > maxWaves) {
            return;
        }

        BossBar bossBar = raidBossBars.computeIfAbsent(raid, r -> {
            BossBar bar = Bukkit.createBossBar(
                    ChatColor.RED + "⚔ Rajd w toku (Fala " + waveNumber + ") ⚔", 
                    BarColor.RED, 
                    BarStyle.SEGMENTED_10
            );
            bar.setVisible(true);
            return bar;
        });

        updateBossBarView(raid, bossBar, waveNumber);

        List<Raider> originalRaiders = new ArrayList<>(event.getRaiders());
        int extraMultiplier = plugin.getConfigManager().getExtraMobsMultiplier();

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
        
        updateBossBarProgress(raid, bossBar, waveNumber);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (isRaidMob(entity)) {
            for (Map.Entry<Raid, BossBar> entry : raidBossBars.entrySet()) {
                Raid raid = entry.getKey();
                if (raid.getRaiders().contains(entity) || entity.getWorld().equals(raid.getLocation().getWorld())) {
                    updateBossBarProgress(raid, entry.getValue(), raid.getSpawnedGroups());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onRaidStop(RaidStopEvent event) {
        Raid raid = event.getRaid();
        if (event.getReason() == RaidStopEvent.Reason.TIMEOUT) {
            try {
                event.getRaid().getClass().getMethod("setBadOmenLevel", int.class).invoke(event.getRaid(), event.getRaid().getBadOmenLevel());
                return;
            } catch (Exception ignored) {}
        }

        BossBar bossBar = raidBossBars.remove(raid);
        if (bossBar != null) {
            bossBar.removeAll();
        }
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

            // Dodatkowa mechanika taranowania Ravagera (Pomysł nr 4):
            // Kiedy Ravager uderza gracza w zwarciu, zadaje dodatkowy impet odrzutu
            if (damager instanceof Ravager && event.getEntity() instanceof Player player) {
                Vector direction = player.getLocation().toVector().subtract(damager.getLocation().toVector()).normalize();
                player.setVelocity(direction.multiply(1.2).setY(0.5)); // Wyrzuca gracza w górę i w tył
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ATTACK, 1.0f, 0.5f);
            }
        }
    }

    private boolean isRaidMob(LivingEntity entity) {
        return entity instanceof Raider || entity.getType() == EntityType.WITCH;
    }

    private void updateBossBarView(Raid raid, BossBar bossBar, int waveNumber) {
        Location center = raid.getLocation();
        if (center == null || center.getWorld() == null) return;

        bossBar.setTitle(ChatColor.DARK_RED + "⚡ ATAK NA WIOSKĘ — Fala " + waveNumber + " ⚡");

        for (Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(center) <= 65536) {
                if (!bossBar.getPlayers().contains(player)) {
                    bossBar.addPlayer(player);
                }
            } else {
                bossBar.removePlayer(player);
            }
        }
    }

    private void updateBossBarProgress(Raid raid, BossBar bossBar, int waveNumber) {
        updateBossBarView(raid, bossBar, waveNumber);
        
        int totalRaiders = raid.getRaiders().size();
        if (totalRaiders <= 0) {
            bossBar.setProgress(0.0);
            return;
        }

        long aliveCount = raid.getRaiders().stream().filter(LivingEntity::isValid).count();
        double progress = Math.min(1.0, (double) aliveCount / Math.max(1.0, (double) totalRaiders));
        bossBar.setProgress(Math.max(0.0, progress));
    }

    private void applyCustomizations(LivingEntity entity) {
        if (entity == null || !entity.isValid()) return;

        double baseHealth = plugin.getConfigManager().getMobBaseHealth(entity.getType());
        double globalMultiplier = plugin.getConfigManager().getHealthMultiplier();
        double finalMaxHealth = baseHealth * globalMultiplier;

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

                    // Specjalna zdolność taranowania Ravagera (Co jakiś czas szuka graczy blisko siebie i ich odrzuca)
                    if (mob instanceof Ravager && mob.getTarget() instanceof Player target) {
                        if (mob.getLocation().distanceSquared(target.getLocation()) <= 16) { // 4 bloki
                            if (random.nextInt(100) < 25) { // 25% szansy co sekundę przy bliskim kontakcie
                                Vector knockback = target.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize();
                                target.setVelocity(knockback.multiply(1.5).setY(0.6));
                                target.damage(4.0, mob); // Dodatkowe uderzenie taranem
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