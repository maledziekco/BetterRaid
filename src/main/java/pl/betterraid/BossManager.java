packaimport org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

// Przykład w metodzie spawnowania moba/bossa:
public void setupCustomHealth(LivingEntity entity, double maxHealth) {
    // 1. Pobieramy atrybut maksymalnego zdrowia entity
    AttributeInstance healthAttribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
    
    if (healthAttribute != null) {
        // 2. Ustawiamy maksymalne zdrowie (np. z configu)
        healthAttribute.setBaseValue(maxHealth);
        
        // 3. Ustawiamy aktualne zdrowie na maksimum, żeby mob nie spawned się "ranny"
        entity.setHealth(maxHealth);
    }
}