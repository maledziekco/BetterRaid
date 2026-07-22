package pl.betterraid.listener;

import org.bukkit.event.Listener;
import pl.betterraid.BetterRaid;

public class RaidListener implements Listener {
    private final BetterRaid plugin;

    public RaidListener(BetterRaid plugin) {
        this.plugin = plugin;
    }
}
