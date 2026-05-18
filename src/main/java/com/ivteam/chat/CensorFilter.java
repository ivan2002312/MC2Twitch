package com.ivteam.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CensorFilter {
    private final MineraftChat2TwithMassage plugin;
    private final List<Pattern> bannedPatterns;
    private boolean enabled;

    public CensorFilter(MineraftChat2TwithMassage plugin) {
        this.plugin = plugin;
        this.bannedPatterns = new ArrayList<>();
        loadBannedWords();
    }

    private void loadBannedWords() {
        enabled = plugin.getConfig().getBoolean("censor.enabled", true);
        List<String> words = plugin.getConfig().getStringList("censor.banned_words");

        bannedPatterns.clear();
        for (String word : words) {
            // Создаём паттерн, который ловит слово в любом регистре
            // и с возможными заменами букв (a = @, и = u, и т.д.)
            String pattern = word
                    .toLowerCase()
                    .replace("а", "[аa@]")
                    .replace("о", "[оo0]")
                    .replace("е", "[еe]")
                    .replace("и", "[иi]")
                    .replace("у", "[уu]")
                    .replace("с", "[сs$]")
                    .replace("к", "[кk]")
                    .replace("х", "[хhx]")
                    .replace("б", "[бb6]")
                    .replace("в", "[вbv]")
                    .replace("з", "[зz3]")
                    .replace("т", "[тt]")
                    .replace("п", "[пp]")
                    .replace("р", "[рr]")
                    .replace("м", "[мm]")
                    .replace("н", "[нnh]")
                    .replace("д", "[дd]")
                    .replace("л", "[лl]")
                    .replace("я", "[яr]")
                    .replace("ё", "[ёe]")
                    .replace("ж", "[жj]")
                    .replace("ч", "[ч4]")
                    .replace("ш", "[шw]")
                    .replace("щ", "[щw]")
                    .replace("ц", "[цc]")
                    .replace("ы", "[ыy]")
                    .replace("ф", "[фf]")
                    .replace("э", "[эe]")
                    .replace("ю", "[юu]")
                    .replace("й", "[йi]");

            bannedPatterns.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        }
    }

    public String filter(String message) {
        if (!enabled || message == null || message.isEmpty()) {
            return message;
        }

        String filteredMessage = message;

        for (Pattern pattern : bannedPatterns) {
            Matcher matcher = pattern.matcher(filteredMessage);
            if (matcher.find()) {
                // Заменяем найденное слово на звёздочки
                filteredMessage = matcher.replaceAll(match -> {
                    StringBuilder stars = new StringBuilder();
                    for (int i = 0; i < match.group().length(); i++) {
                        stars.append("*");
                    }
                    return stars.toString();
                });
            }
        }

        return filteredMessage;
    }

    public void reload() {
        loadBannedWords();
    }

    public boolean isEnabled() {
        return enabled;
    }
}