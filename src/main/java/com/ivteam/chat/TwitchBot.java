package com.ivteam.chat;

import java.io.*;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;

public class TwitchBot {
    private final MineraftChat2TwithMassage plugin;
    private final String channelName;
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private boolean connected = false;
    private Thread readThread;

    public TwitchBot(MineraftChat2TwithMassage plugin, String channelName) {
        this.plugin = plugin;
        this.channelName = channelName;
    }

    public boolean connect(String username, String oauthToken, String channel) {
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = factory.createSocket("irc.chat.twitch.tv", 6697);

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            sendRaw("PASS " + oauthToken);
            sendRaw("NICK " + username.toLowerCase());
            sendRaw("CAP REQ :twitch.tv/tags");
            sendRaw("CAP REQ :twitch.tv/commands");
            sendRaw("JOIN #" + channel.toLowerCase());

            String line;
            int attempts = 0;
            while ((line = reader.readLine()) != null && attempts < 10) {
                if (line.contains("004") || (line.contains("JOIN") && line.contains("#" + channel.toLowerCase()))) {
                    connected = true;
                    plugin.getLogger().info("✓ Подключен к каналу #" + channel);
                    return true;
                }

                if (line.contains("NOTICE") && line.contains("Login unsuccessful")) {
                    plugin.getLogger().warning("✗ Ошибка авторизации для канала " + channel);
                    disconnect();
                    return false;
                }

                attempts++;
            }

        } catch (IOException e) {
            plugin.getLogger().severe("✗ Ошибка подключения к " + channel + ": " + e.getMessage());
            disconnect();
            return false;
        }

        return connected;
    }

    public void startReading() {
        readThread = new Thread(() -> {
            try {
                String line;
                while (connected && (line = reader.readLine()) != null) {
                    if (line.contains("PRIVMSG")) {
                        parseMessage(line);
                    }
                    if (line.startsWith("PING")) {
                        sendRaw("PONG :tmi.twitch.tv");
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    plugin.getLogger().warning("Ошибка чтения из Twitch: " + e.getMessage());
                }
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    private void parseMessage(String line) {
        try {
            String[] parts = line.split(":", 3);
            if (parts.length >= 3) {
                String userPart = parts[1];
                String message = parts[2];

                String username = userPart.split("!")[0];

                message = plugin.filterMessage(message);

                plugin.sendToMinecraft(username, message);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка парсинга: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        if (!connected || message == null || message.isEmpty()) {
            return;
        }

        try {
            if (message.length() > 450) {
                message = message.substring(0, 447) + "...";
            }

            sendRaw("PRIVMSG #" + channelName + " :" + message);
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка отправки: " + e.getMessage());
            connected = false;
        }
    }

    private void sendRaw(String message) throws IOException {
        if (writer != null) {
            writer.write(message + "\r\n");
            writer.flush();
        }
    }

    public void disconnect() {
        connected = false;
        if (readThread != null && readThread.isAlive()) {
            readThread.interrupt();
        }
        try {
            if (writer != null) {
                sendRaw("PART #" + channelName);
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
        } finally {
            writer = null;
            reader = null;
            socket = null;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public String getChannelName() {
        return channelName;
    }
}