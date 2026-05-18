package com.ivteam.chat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.*;

public class ConfigManager {
    private final MineraftChat2TwithMassage plugin;
    private FileConfiguration config;

    public ConfigManager(MineraftChat2TwithMassage plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        updateConfigFormat();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    private void updateConfigFormat() {
        // Если есть старый формат, конвертируем в новый
        if (config.contains("twitch.username")) {
            String username = config.getString("twitch.username");
            String token = config.getString("twitch.oauth_token");
            String channel = config.getString("twitch.channel");

            List<Map<String, Object>> channels = new ArrayList<>();
            Map<String, Object> defaultChannel = new LinkedHashMap<>();
            defaultChannel.put("name", channel != null ? channel : "default");
            defaultChannel.put("username", username != null ? username : "bot");
            defaultChannel.put("oauth_token", token != null ? token : "oauth:token");
            defaultChannel.put("enabled", true);
            channels.add(defaultChannel);

            config.set("twitch", null);
            config.set("twitch.channels", channels);
            saveConfig();
        }
    }

    public List<ChannelConfig> getChannels() {
        List<ChannelConfig> channels = new ArrayList<>();
        List<Map<?, ?>> channelList = config.getMapList("twitch.channels");

        for (Map<?, ?> map : channelList) {
            String name = getStringFromMap(map, "name", "unknown");
            String username = getStringFromMap(map, "username", "bot");
            String token = getStringFromMap(map, "oauth_token", "");
            boolean enabled = getBooleanFromMap(map, "enabled", true);

            if (!token.isEmpty()) {
                channels.add(new ChannelConfig(name, username, token, enabled));
            }
        }

        return channels;
    }

    private String getStringFromMap(Map<?, ?> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return String.valueOf(value);
    }

    private boolean getBooleanFromMap(Map<?, ?> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    public void addChannel(String name, String username, String token) {
        List<Map<?, ?>> channels = config.getMapList("twitch.channels");
        List<Map<String, Object>> newChannels = new ArrayList<>();

        // Копируем существующие каналы
        for (Map<?, ?> map : channels) {
            Map<String, Object> channel = new LinkedHashMap<>();
            channel.put("name", getStringFromMap(map, "name", ""));
            channel.put("username", getStringFromMap(map, "username", ""));
            channel.put("oauth_token", getStringFromMap(map, "oauth_token", ""));
            channel.put("enabled", getBooleanFromMap(map, "enabled", true));
            newChannels.add(channel);
        }

        // Добавляем новый канал
        Map<String, Object> newChannel = new LinkedHashMap<>();
        newChannel.put("name", name);
        newChannel.put("username", username);
        newChannel.put("oauth_token", token.startsWith("oauth:") ? token : "oauth:" + token);
        newChannel.put("enabled", true);
        newChannels.add(newChannel);

        config.set("twitch.channels", newChannels);
        saveConfig();
    }

    public boolean removeChannel(String name) {
        List<Map<?, ?>> channels = config.getMapList("twitch.channels");
        List<Map<String, Object>> newChannels = new ArrayList<>();
        boolean removed = false;

        for (Map<?, ?> map : channels) {
            String channelName = getStringFromMap(map, "name", "");
            if (!channelName.equalsIgnoreCase(name)) {
                Map<String, Object> channel = new LinkedHashMap<>();
                channel.put("name", channelName);
                channel.put("username", getStringFromMap(map, "username", ""));
                channel.put("oauth_token", getStringFromMap(map, "oauth_token", ""));
                channel.put("enabled", getBooleanFromMap(map, "enabled", true));
                newChannels.add(channel);
            } else {
                removed = true;
            }
        }

        if (removed) {
            config.set("twitch.channels", newChannels);
            saveConfig();
        }

        return removed;
    }

    public boolean toggleChannel(String name, boolean enabled) {
        List<Map<?, ?>> channels = config.getMapList("twitch.channels");
        List<Map<String, Object>> newChannels = new ArrayList<>();
        boolean found = false;

        for (Map<?, ?> map : channels) {
            Map<String, Object> channel = new LinkedHashMap<>();
            String channelName = getStringFromMap(map, "name", "");
            channel.put("name", channelName);
            channel.put("username", getStringFromMap(map, "username", ""));
            channel.put("oauth_token", getStringFromMap(map, "oauth_token", ""));

            if (channelName.equalsIgnoreCase(name)) {
                channel.put("enabled", enabled);
                found = true;
            } else {
                channel.put("enabled", getBooleanFromMap(map, "enabled", true));
            }

            newChannels.add(channel);
        }

        if (found) {
            config.set("twitch.channels", newChannels);
            saveConfig();
        }

        return found;
    }

    public List<String> getChannelNames() {
        List<String> names = new ArrayList<>();
        for (ChannelConfig channel : getChannels()) {
            names.add(channel.getName());
        }
        return names;
    }

    public boolean isEnabled() {
        return config.getBoolean("settings.enabled", true);
    }

    public void setEnabled(boolean enabled) {
        config.set("settings.enabled", enabled);
        saveConfig();
    }

    public String getMessageFormat() {
        return config.getString("settings.message_format", "[MC] %player%: %message%");
    }

    public void setMessageFormat(String format) {
        config.set("settings.message_format", format);
        saveConfig();
    }

    public String getTwitchMessageFormat() {
        return config.getString("settings.twitch_message_format", "&5[Twitch] &d%user%&f: %message%");
    }

    public void setTwitchMessageFormat(String format) {
        config.set("settings.twitch_message_format", format);
        saveConfig();
    }

    public void addBannedWord(String word) {
        List<String> words = config.getStringList("censor.banned_words");
        if (!words.contains(word.toLowerCase())) {
            words.add(word.toLowerCase());
            config.set("censor.banned_words", words);
            saveConfig();
        }
    }

    public boolean removeBannedWord(String word) {
        List<String> words = config.getStringList("censor.banned_words");
        boolean removed = words.remove(word.toLowerCase());
        if (removed) {
            config.set("censor.banned_words", words);
            saveConfig();
        }
        return removed;
    }

    public List<String> getBannedWords() {
        return config.getStringList("censor.banned_words");
    }

    public boolean isCensorEnabled() {
        return config.getBoolean("censor.enabled", true);
    }

    public void setCensorEnabled(boolean enabled) {
        config.set("censor.enabled", enabled);
        saveConfig();
    }

    public static class ChannelConfig {
        private final String name;
        private final String username;
        private final String oauthToken;
        private final boolean enabled;

        public ChannelConfig(String name, String username, String oauthToken, boolean enabled) {
            this.name = name;
            this.username = username;
            this.oauthToken = oauthToken.startsWith("oauth:") ? oauthToken : "oauth:" + oauthToken;
            this.enabled = enabled;
        }

        public String getName() { return name; }
        public String getUsername() { return username; }
        public String getOauthToken() { return oauthToken; }
        public boolean isEnabled() { return enabled; }
    }
}