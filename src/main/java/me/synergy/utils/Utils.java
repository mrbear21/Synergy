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

    public static String removeIgnoringCase(String word, String sentence) {
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


    public static String customColorCodes(String sentence) {
        Set < String > codes = Synergy.getSpigot().getConfig().getConfigurationSection("chat-manager.custom-color-tags").getKeys(false);
        for (String c: codes) {
            sentence = sentence.replace(c, Synergy.getSpigot().getConfig().getString("chat-manager.custom-color-tags." + c));
        }
        return sentence;
    }

    public static String processColors(String string) {
        string = applyGradient(string);
        string = customColorCodes(string);
        Pattern pattern = Pattern.compile("<(#[A-Fa-f0-9]{6})>");
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            string = string.replace(matcher.group(), "" + ChatColor.of(matcher.group(1)));
        }
        string = ChatColor.translateAlternateColorCodes('&', string);
        return string;
    }
    
    public static String stripColorTags(String string) {
        String pattern = "<#\\w{6}>";
        string = string.replaceAll(pattern, "").replaceAll("</#\\w{6}>", "");
        string = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string));
        return string;
    }
    
    public static String[] splitMessage(String message) {
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
    
    public static List<String> getBlockedWorlds() {
		return Synergy.getSpigot().getConfig().getStringList("chat-manager.blocked-words");
	}
    
    public static String censorBlockedWords(String sentence, List<String> blockedWords) {
        double tolerance = Synergy.getConfig().getDouble("chat-manager.blocked-words-tolerance-percentage");
        for (String blockedWord : blockedWords) {
            String match = "";
            int start = 0, end = 0;
            for (int i = 0; i < removeColorCodes(sentence).length(); i++) {
                if (Character.isAlphabetic(removeColorCodes(sentence).charAt(i))) {

                    boolean isFirstLetterMatches = blockedWord.charAt(0) == removeColorCodes(sentence).charAt(i);
                    boolean isWordStartsWithBadWord = blockedWord.startsWith(String.valueOf(match) + removeColorCodes(sentence).charAt(i));
                    boolean isWordWithoutDuplicatesStartsWithBadWord = blockedWord.startsWith(removeConsecutiveDuplicates(String.valueOf(match) + removeColorCodes(sentence).charAt(i)));

                    if (isFirstLetterMatches || isWordStartsWithBadWord || isWordWithoutDuplicatesStartsWithBadWord) {
                        if (match.isEmpty())
                            start = i;
                        match = String.valueOf(match) + Utils.removeColorCodes(sentence).charAt(i);
                        end = i;
                    } else {
                        match = "";
                    }

                    if (blockedWord.equals(match) || blockedWord.equals(removeConsecutiveDuplicates(match))) {
                        String word = findWordInRange(removeColorCodes(sentence), start, end);
                        double percentage = (double) match.length() / (double) word.length() * 100;
                        if (tolerance < percentage || !word.contains(blockedWord))
                            sentence = censorPartOfSentence(sentence, start, end);
                    }
                }
            }
        }
        return sentence;
    }
    
	public static String detectPlayernamePlaceholder(String text, String defaultIfNull) {
	    int startIdx = text.indexOf("%");
	    if (startIdx == -1) {
	        return defaultIfNull;
	    }
	    int nameIdx = text.indexOf("name", startIdx);
	    if (nameIdx == -1) {
	        return defaultIfNull;
	    }
	    int leftPercentIdx = text.lastIndexOf("%", nameIdx);
	    int rightPercentIdx = text.indexOf("%", nameIdx + 1);
	    if (leftPercentIdx == -1 || rightPercentIdx == -1) {
	        return defaultIfNull;
	    }
	    return text.substring(leftPercentIdx, rightPercentIdx + 1);
	}
    
	public static String translateSmiles(String string) {
		for (String e : Synergy.getConfig().getConfigurationSection("chat-manager.custom-emojis").getKeys(false)) {
			string = string.replace(e, Synergy.getConfig().getString("chat-manager.custom-emojis."+e));
		}
		return string;
	}
	
    public static String removeColorCodes(String input) {
        String regex = "&[0-9a-f]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        String result = matcher.replaceAll("**");
        return result;
    }
    
    public static String removeConsecutiveDuplicates(String text) {
        StringBuilder result = new StringBuilder();
        char prevChar = '\0';
        for (char currentChar : text.toCharArray()) {
            if (currentChar != prevChar) {
                result.append(currentChar);
                prevChar = currentChar;
            }
        }
        return result.toString();
    }
    
    public static String findWordInRange(String sentence, int start, int end) {
        String[] words = sentence.split("\\s+");
        for (String word : words) {
            int wordStart = sentence.indexOf(word);
            int wordEnd = wordStart + word.length() - 1;
            
            if (start >= wordStart && end <= wordEnd) {
                return word;
            }
        }
        return sentence.substring(start, end);
    }

    public static String censorWord(String word) {
        if (word.length() <= 2) {
            return word;
        }
        char[] charArray = word.toCharArray();
        for (int i = 1; i < charArray.length - 1; i++) {
            charArray[i] = '*';
        }
        return new String(charArray);
    }

    
    public static String censorPartOfSentence(String sentence, int start, int end) {
        if (end-start < 2) {
            return sentence;
        }
        char[] charArray = sentence.toCharArray();
        for (int i = 1; i < charArray.length - 1; i++) {
        	if (i > start && i < end && Character.isAlphabetic(sentence.charAt(i))) {
        		charArray[i] = '*';
        	}
        }
        return new String(charArray);
    }
	
    private static String applyGradientToText(String text, ChatColor startColor, ChatColor endColor) {
        StringBuilder gradientText = new StringBuilder();
        int startRgb = startColor.getColor().getRGB();
        int endRgb = endColor.getColor().getRGB();
        int textLength = text.length();
        int startR = (startRgb >> 16) & 0xFF;
        int startG = (startRgb >> 8) & 0xFF;
        int startB = startRgb & 0xFF;
        int endR = (endRgb >> 16) & 0xFF;
        int endG = (endRgb >> 8) & 0xFF;
        int endB = endRgb & 0xFF;
        double stepR = ((double)(endR - startR)) / textLength;
        double stepG = ((double)(endG - startG)) / textLength;
        double stepB = ((double)(endB - startB)) / textLength;

        for (int i = 0; i < textLength; i++) {
            int r = (int)(startR + stepR * i);
            int g = (int)(startG + stepG * i);
            int b = (int)(startB + stepB * i);
            ChatColor color = ChatColor.of(new java.awt.Color(r, g, b));
            gradientText.append(color).append(text.charAt(i));
        }
        return gradientText.toString();
    }

    public static String applyGradient(String text) {
        String pattern = "<(#\\w{6})>([^<]+?)</(#\\w{6})>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        StringBuffer result = new StringBuffer();

        while (m.find()) {
            ChatColor startColor = ChatColor.of(m.group(1));
            ChatColor endColor = ChatColor.of(m.group(3));
            String tagText = m.group(2);
            String gradientText = applyGradientToText(tagText, startColor, endColor);
            m.appendReplacement(result, gradientText);
        }
        m.appendTail(result);
        return result.toString();
    }
}