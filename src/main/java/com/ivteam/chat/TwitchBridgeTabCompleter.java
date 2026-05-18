package com.ivteam.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.*;

public class TwitchBridgeTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("twitchbridge.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> commands = new ArrayList<>(Arrays.asList(
                    "help", "reload", "status", "toggle", "test",
                    "channel", "format", "censor"
            ));

            // Фильтруем по вводу
            String input = args[0].toLowerCase();
            commands.removeIf(cmd -> !cmd.startsWith(input));
            return commands;
        }

        if (args.length >= 2) {
            switch (args[0].toLowerCase()) {
                case "channel":
                    return channelTabComplete(args);
                case "censor":
                    return censorTabComplete(args);
                case "format":
                    return formatTabComplete(args);
                default:
                    return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    private List<String> channelTabComplete(String[] args) {
        MineraftChat2TwithMassage plugin = MineraftChat2TwithMassage.getInstance();

        if (args.length == 2) {
            List<String> subCommands = new ArrayList<>(Arrays.asList(
                    "add", "remove", "list", "connect", "disconnect"
            ));
            String input = args[1].toLowerCase();
            subCommands.removeIf(cmd -> !cmd.startsWith(input));
            return subCommands;
        }

        if (args.length == 3) {
            String subCommand = args[1].toLowerCase();

            if (subCommand.equals("remove") || subCommand.equals("connect") || subCommand.equals("disconnect")) {
                List<String> channels = plugin.getConfigManager().getChannelNames();
                String input = args[2].toLowerCase();
                channels.removeIf(ch -> !ch.toLowerCase().startsWith(input));
                return channels;
            }
        }

        return Collections.emptyList();
    }

    private List<String> censorTabComplete(String[] args) {
        if (args.length == 2) {
            List<String> subCommands = new ArrayList<>(Arrays.asList(
                    "add", "remove", "list", "on", "off"
            ));
            String input = args[1].toLowerCase();
            subCommands.removeIf(cmd -> !cmd.startsWith(input));
            return subCommands;
        }

        if (args.length == 3 && args[1].toLowerCase().equals("remove")) {
            MineraftChat2TwithMassage plugin = MineraftChat2TwithMassage.getInstance();
            List<String> words = plugin.getConfigManager().getBannedWords();
            String input = args[2].toLowerCase();
            words.removeIf(w -> !w.startsWith(input));
            return words;
        }

        return Collections.emptyList();
    }

    private List<String> formatTabComplete(String[] args) {
        if (args.length == 2) {
            List<String> subCommands = new ArrayList<>(Arrays.asList(
                    "mc", "twitch", "show"
            ));
            String input = args[1].toLowerCase();
            subCommands.removeIf(cmd -> !cmd.startsWith(input));
            return subCommands;
        }

        return Collections.emptyList();
    }
}