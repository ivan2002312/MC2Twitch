package com.ivteam.chat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.Map;

public class TwitchBridgeCommand implements CommandExecutor {
    private final MineraftChat2TwithMassage plugin;

    public TwitchBridgeCommand(MineraftChat2TwithMassage plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("twitchbridge.admin")) {
            sender.sendMessage(ChatColor.RED + "⛔ У вас нет прав!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(sender);
                break;

            case "reload":
                plugin.getConfigManager().loadConfig();
                plugin.getCensorFilter().reload();
                plugin.getTwitchManager().disconnectAll();
                plugin.getTwitchManager().connectAll();
                sender.sendMessage(ChatColor.GREEN + "✓ Конфигурация перезагружена!");
                break;

            case "status":
                sendStatus(sender);
                break;

            case "toggle":
                ConfigManager config = plugin.getConfigManager();
                config.setEnabled(!config.isEnabled());
                if (config.isEnabled()) {
                    plugin.getTwitchManager().connectAll();
                    sender.sendMessage(ChatColor.GREEN + "✓ Мост включен!");
                } else {
                    plugin.getTwitchManager().disconnectAll();
                    sender.sendMessage(ChatColor.RED + "✗ Мост выключен!");
                }
                break;

            case "test":
                if (!plugin.getTwitchManager().getConnectedChannels().isEmpty()) {
                    plugin.sendToAllTwitch("🧪 Тестовое сообщение с Minecraft сервера!");
                    sender.sendMessage(ChatColor.GREEN + "✓ Тест отправлен во все каналы!");
                } else {
                    sender.sendMessage(ChatColor.RED + "✗ Нет активных подключений!");
                }
                break;

            case "channel":
                handleChannel(sender, args);
                break;

            case "format":
                handleFormat(sender, args);
                break;

            case "censor":
                handleCensor(sender, args);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleChannel(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Использование: /twitchbridge channel <add|remove|list|connect|disconnect> [канал] [username] [token]");
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        TwitchManager manager = plugin.getTwitchManager();

        switch (args[1].toLowerCase()) {
            case "add":
                if (args.length < 5) {
                    sender.sendMessage(ChatColor.RED + "Использование: /twitchbridge channel add <канал> <username> <oauth_token>");
                    return;
                }
                config.addChannel(args[2], args[3], args[4]);
                sender.sendMessage(ChatColor.GREEN + "✓ Канал " + args[2] + " добавлен!");
                sender.sendMessage(ChatColor.YELLOW + "Используйте /twitchbridge channel connect " + args[2] + " для подключения");
                break;

            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Использование: /twitchbridge channel remove <канал>");
                    return;
                }
                if (config.removeChannel(args[2])) {
                    manager.disconnectChannel(args[2]);
                    sender.sendMessage(ChatColor.GREEN + "✓ Канал " + args[2] + " удалён!");
                } else {
                    sender.sendMessage(ChatColor.RED + "✗ Канал " + args[2] + " не найден!");
                }
                break;

            case "list":
                sender.sendMessage(ChatColor.GOLD + "=== Список каналов ===");
                Map<String, Boolean> status = manager.getAllChannelsStatus();
                for (Map.Entry<String, Boolean> entry : status.entrySet()) {
                    String statusStr = entry.getValue() ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
                    sender.sendMessage(ChatColor.YELLOW + "  " + entry.getKey() + ": " + statusStr);
                }
                if (status.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Нет добавленных каналов");
                }
                break;

            case "connect":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Использование: /twitchbridge channel connect <канал>");
                    return;
                }
                for (ConfigManager.ChannelConfig ch : config.getChannels()) {
                    if (ch.getName().equalsIgnoreCase(args[2])) {
                        if (manager.connectChannel(ch)) {
                            sender.sendMessage(ChatColor.GREEN + "✓ Подключено к " + args[2]);
                        } else {
                            sender.sendMessage(ChatColor.RED + "✗ Не удалось подключиться к " + args[2]);
                        }
                        return;
                    }
                }
                sender.sendMessage(ChatColor.RED + "✗ Канал не найден в конфиге!");
                break;

            case "disconnect":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Использование: /twitchbridge channel disconnect <канал>");
                    return;
                }
                if (manager.disconnectChannel(args[2])) {
                    sender.sendMessage(ChatColor.GREEN + "✓ Отключено от " + args[2]);
                } else {
                    sender.sendMessage(ChatColor.RED + "✗ Канал не подключен!");
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда. Используйте: add, remove, list, connect, disconnect");
        }
    }

    private void handleFormat(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Использование: /twitchbridge format <mc|twitch|show> [формат]");
            return;
        }

        ConfigManager config = plugin.getConfigManager();

        switch (args[1].toLowerCase()) {
            case "mc":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Использование: /twitchbridge format mc <формат>");
                    sender.sendMessage(ChatColor.YELLOW + "Переменные: %player%, %displayname%, %message%");
                    return;
                }
                String mcFormat = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);
                config.setMessageFormat(mcFormat);
                sender.sendMessage(ChatColor.GREEN + "✓ Формат MC → Twitch обновлён!");
                break;

            case "twitch":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Использование: /twitchbridge format twitch <формат>");
                    sender.sendMessage(ChatColor.YELLOW + "Переменные: %user%, %message% | Цвета: &5, &d, &f и т.д.");
                    return;
                }
                String twitchFormat = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);
                config.setTwitchMessageFormat(twitchFormat);
                sender.sendMessage(ChatColor.GREEN + "✓ Формат Twitch → MC обновлён!");
                break;

            case "show":
                sender.sendMessage(ChatColor.GOLD + "=== Текущие форматы ===");
                sender.sendMessage(ChatColor.YELLOW + "MC → Twitch: " + ChatColor.WHITE + config.getMessageFormat());
                sender.sendMessage(ChatColor.YELLOW + "Twitch → MC: " + ChatColor.WHITE + config.getTwitchMessageFormat());
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Используйте: mc, twitch, show");
        }
    }

    private void handleCensor(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Использование: /twitchbridge censor <add|remove|list|on|off> [слово]");
            return;
        }

        ConfigManager config = plugin.getConfigManager();

        switch (args[1].toLowerCase()) {
            case "add":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Использование: /twitchbridge censor add <слово>");
                    return;
                }
                config.addBannedWord(args[2]);
                plugin.getCensorFilter().reload();
                sender.sendMessage(ChatColor.GREEN + "✓ Слово '" + args[2] + "' добавлено в фильтр!");
                break;

            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Использование: /twitchbridge censor remove <слово>");
                    return;
                }
                if (config.removeBannedWord(args[2])) {
                    plugin.getCensorFilter().reload();
                    sender.sendMessage(ChatColor.GREEN + "✓ Слово '" + args[2] + "' удалено из фильтра!");
                } else {
                    sender.sendMessage(ChatColor.RED + "✗ Слово не найдено!");
                }
                break;

            case "list":
                sender.sendMessage(ChatColor.GOLD + "=== Запрещённые слова ===");
                for (String word : config.getBannedWords()) {
                    sender.sendMessage(ChatColor.RED + "  - " + word);
                }
                sender.sendMessage(ChatColor.YELLOW + "Всего: " + config.getBannedWords().size() + " слов");
                break;

            case "on":
                config.setCensorEnabled(true);
                plugin.getCensorFilter().reload();
                sender.sendMessage(ChatColor.GREEN + "✓ Цензура включена!");
                break;

            case "off":
                config.setCensorEnabled(false);
                plugin.getCensorFilter().reload();
                sender.sendMessage(ChatColor.RED + "✗ Цензура выключена!");
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Используйте: add, remove, list, on, off");
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "╔══════════════════════════════════════╗");
        sender.sendMessage(ChatColor.GOLD + "║   " + ChatColor.YELLOW + "MinecraftChat2TwitchMassage" + ChatColor.GOLD + "   ║");
        sender.sendMessage(ChatColor.GOLD + "╠══════════════════════════════════════╣");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge help" + ChatColor.GRAY + " - Помощь              ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge reload" + ChatColor.GRAY + " - Перезагрузка      ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge status" + ChatColor.GRAY + " - Статус            ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge toggle" + ChatColor.GRAY + " - Вкл/Выкл          ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge test" + ChatColor.GRAY + " - Тест сообщения      ║");
        sender.sendMessage(ChatColor.GOLD + "╠══════════════════════════════════════╣");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.AQUA + "Управление каналами:" + ChatColor.GRAY + "                ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge channel add <канал> <user> <token>  ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge channel remove <канал>   ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge channel list             ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge channel connect <канал>  ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge channel disconnect <канал>║");
        sender.sendMessage(ChatColor.GOLD + "╠══════════════════════════════════════╣");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.AQUA + "Форматы сообщений:" + ChatColor.GRAY + "                ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge format mc <формат>       ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge format twitch <формат>   ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge format show              ║");
        sender.sendMessage(ChatColor.GOLD + "╠══════════════════════════════════════╣");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.AQUA + "Управление цензурой:" + ChatColor.GRAY + "                ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge censor add <слово>       ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge censor remove <слово>    ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge censor list              ║");
        sender.sendMessage(ChatColor.GOLD + "║ " + ChatColor.WHITE + "/twitchbridge censor on/off            ║");
        sender.sendMessage(ChatColor.GOLD + "╚══════════════════════════════════════╝");
    }

    private void sendStatus(CommandSender sender) {
        ConfigManager config = plugin.getConfigManager();
        TwitchManager manager = plugin.getTwitchManager();

        sender.sendMessage(ChatColor.GOLD + "=== Статус TwitchBridge ===");
        sender.sendMessage(ChatColor.YELLOW + "Плагин: " +
                (config.isEnabled() ? ChatColor.GREEN + "✓ Включен" : ChatColor.RED + "✗ Выключен"));
        sender.sendMessage(ChatColor.YELLOW + "Цензура: " +
                (config.isCensorEnabled() ? ChatColor.GREEN + "✓ Вкл" : ChatColor.RED + "✗ Выкл"));

        sender.sendMessage(ChatColor.GOLD + "=== Каналы ===");
        Map<String, Boolean> status = manager.getAllChannelsStatus();
        for (Map.Entry<String, Boolean> entry : status.entrySet()) {
            String statusStr = entry.getValue() ? ChatColor.GREEN + "✓ Подключен" : ChatColor.RED + "✗ Отключен";
            sender.sendMessage(ChatColor.YELLOW + "  " + entry.getKey() + ": " + statusStr);
        }
        if (status.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "  Нет каналов");
        }
    }
}