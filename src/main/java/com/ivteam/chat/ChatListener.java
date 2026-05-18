package com.ivteam.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final MineraftChat2TwithMassage plugin;

    public ChatListener(MineraftChat2TwithMassage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        String message = event.getMessage();

        message = plugin.filterMessage(message);

        if (!message.equals(event.getMessage())) {
            event.setMessage(message);
        }

        String twitchMessage = plugin.getConfigManager().getMessageFormat()
                .replace("%player%", event.getPlayer().getName())
                .replace("%displayname%", event.getPlayer().getDisplayName())
                .replace("%message%", message);

        plugin.sendToAllTwitch(twitchMessage);
    }
}