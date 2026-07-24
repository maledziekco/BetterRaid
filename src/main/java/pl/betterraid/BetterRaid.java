private RaidManager raidManager;
private BossManager bossManager;
private ConfigManager configManager;

// W metodzie onEnable():
this.configManager = new ConfigManager(this);
this.bossManager = new BossManager(this);
this.raidManager = new RaidManager(this);

// Dodaj też metody gettery w BetterRaid.java:
public RaidManager getRaidManager() {
    return raidManager;
}

public BossManager getBossManager() {
    return bossManager;
}

public ConfigManager getConfigManager() {
    return configManager;
}