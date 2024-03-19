package me.synergy.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.synergy.brains.Synergy;
import net.md_5.bungee.api.ChatColor;

public class Utils {
    private String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(this.CHARACTERS.length());
            char randomChar = this.CHARACTERS.charAt(randomIndex);
            randomString.append(randomChar);
        }
        return randomString.toString();
    }

    public String removeIgnoringCase(String word, String sentence) {
        String lowerCaseSentence = sentence.toLowerCase();
        String lowerCaseWord = word.toLowerCase();
        String[] words = lowerCaseSentence.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String w: words) {
            if (!w.equals(lowerCaseWord))
                result.append(w).append(" ");
        }
        return result.toString().trim();
    }


    public String customColorCodes(String sentence) {
        Set < String > codes = Synergy.getSpigotInstance().getConfig().getConfigurationSection("chat-manager.custom-color-tags").getKeys(false);
        for (String c: codes) {
            sentence = sentence.replace(c, Synergy.getSpigotInstance().getConfig().getString("chat-manager.custom-color-tags." + c));
        }
        return sentence;
    }

    public String processColors(String string) {
        string = customColorCodes(string);
        Pattern pattern = Pattern.compile("<(#[A-Fa-f0-9]{6})>");
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            string = string.replace(matcher.group(), "" + ChatColor.of(matcher.group(1)));
        }
        string = ChatColor.translateAlternateColorCodes('&', string);
        return string;
    }
    
    public String[] splitMessage(String message) {
        List < String > parts = new ArrayList < > ();
        String[] words = message.split("\\s+");
        StringBuilder currentPart = new StringBuilder();
        for (String word: words) {
            if (currentPart.length() + word.length() + 1 <= 256) {
                currentPart.append(currentPart.length() > 0 ? " " : "").append(word);
            } else {
                parts.add(currentPart.toString());
                currentPart = new StringBuilder(word);
            }
        }
        parts.add(currentPart.toString());
        return parts.toArray(new String[0]);
    }
}