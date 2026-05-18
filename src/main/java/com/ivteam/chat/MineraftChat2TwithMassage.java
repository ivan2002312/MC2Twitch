package com.ivteam.chat;

import org.bukkit.plugin.java.JavaPlugin;

public final class MineraftChat2TwithMassage extends JavaPlugin {

    private static MineraftChat2TwithMassage instance;
    private TwitchManager twitchManager;
    private ConfigManager configManager;
    private CensorFilter censorFilter;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        censorFilter = new CensorFilter(this);
        twitchManager = new TwitchManager(this);

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        if (getCommand("twitchbridge") != null) {
            getCommand("twitchbridge").setExecutor(new TwitchBridgeCommand(this));
            getCommand("twitchbridge").setTabCompleter(new TwitchBridgeTabCompleter());
        }

        if (configManager.isEnabled()) {
            twitchManager.connectAll();
        }

        getLogger().info("MinecraftChat2TwitchMassage успешно загружен!");
    }

    @Override
    public void onDisable() {
        if (twitchManager != null) {
            twitchManager.disconnectAll();
        }
        getLogger().info("MinecraftChat2TwitchMassage выключен!");
        instance = null;
    }

    public void sendToAllTwitch(String message) {
        if (configManager.isEnabled()) {
            twitchManager.sendToAll(message);
        }
    }

    public void sendToMinecraft(String twitchUser, String message) {
        getServer().getScheduler().runTask(this, () -> {
            String format = configManager.getTwitchMessageFormat()
                    .replace("%user%", twitchUser)
                    .replace("%message%", message);

            getServer().broadcastMessage(format);
        });
    }

    public String filterMessage(String message) {
        return censorFilter.filter(message);
    }

    public static MineraftChat2TwithMassage getInstance() {
        return instance;
    }

    public TwitchManager getTwitchManager() {
        return twitchManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CensorFilter getCensorFilter() {
        return censorFilter;
    }
}