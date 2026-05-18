package com.ivteam.chat;

import java.util.*;

public class TwitchManager {
    private final MineraftChat2TwithMassage plugin;
    private final Map<String, TwitchBot> bots;

    public TwitchManager(MineraftChat2TwithMassage plugin) {
        this.plugin = plugin;
        this.bots = new HashMap<>();
    }

    public void connectAll() {
        disconnectAll();

        for (ConfigManager.ChannelConfig channelConfig : plugin.getConfigManager().getChannels()) {
            if (channelConfig.isEnabled()) {
                connectChannel(channelConfig);
            }
        }
    }

    public boolean connectChannel(ConfigManager.ChannelConfig config) {
        if (bots.containsKey(config.getName())) {
            bots.get(config.getName()).disconnect();
        }

        TwitchBot bot = new TwitchBot(plugin, config.getName());
        boolean connected = bot.connect(
                config.getUsername(),
                config.getOauthToken(),
                config.getName()
        );

        if (connected) {
            bot.startReading();
            bots.put(config.getName(), bot);
            plugin.getLogger().info("✓ Подключен к каналу: " + config.getName());
        }

        return connected;
    }

    public boolean disconnectChannel(String name) {
        TwitchBot bot = bots.remove(name);
        if (bot != null) {
            bot.disconnect();
            return true;
        }
        return false;
    }

    public void sendToAll(String message) {
        for (TwitchBot bot : bots.values()) {
            if (bot.isConnected()) {
                bot.sendMessage(message);
            }
        }
    }

    public void disconnectAll() {
        for (TwitchBot bot : bots.values()) {
            bot.disconnect();
        }
        bots.clear();
    }

    public boolean isChannelConnected(String name) {
        TwitchBot bot = bots.get(name);
        return bot != null && bot.isConnected();
    }

    public Set<String> getConnectedChannels() {
        Set<String> connected = new HashSet<>();
        for (Map.Entry<String, TwitchBot> entry : bots.entrySet()) {
            if (entry.getValue().isConnected()) {
                connected.add(entry.getKey());
            }
        }
        return connected;
    }

    public Map<String, Boolean> getAllChannelsStatus() {
        Map<String, Boolean> status = new HashMap<>();
        List<ConfigManager.ChannelConfig> configs = plugin.getConfigManager().getChannels();

        for (ConfigManager.ChannelConfig config : configs) {
            boolean connected = isChannelConnected(config.getName());
            status.put(config.getName(), connected && config.isEnabled());
        }

        return status;
    }
}