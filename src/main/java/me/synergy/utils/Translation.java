package me.synergy.utils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import me.synergy.brains.Synergy;
import me.synergy.modules.LocalesManager;
import net.md_5.bungee.api.ChatColor;

public class Translation {
	
    public static String getDefaultLanguage() {
    	try {
    		return Synergy.getConfig().getString("localizations.default-language");
    	} catch (Exception c) {
    		return "en";
    	}
    }
	
	public static String translateStripped(String string, String language) {
		// Translate string
		string = translate(string, language);
		// Remove color
		string = ChatColor.stripColor(Color.removeColor(string));
		// Remove interactive
		string = Interactive.removeInteractiveTags(string);
		return string;
	}

	public static String translate(String string, String language) {
		
		// Process <lang> tags
		try {
        	if (string.contains("<lang>")) {
        		string = Translation.processLangTags(string, language);
        	}
		} catch (Exception c) { Synergy.getLogger().error(c.getLocalizedMessage()); }
		
		// Process placeholders
		try {
			if (Synergy.isDependencyAvailable("PlaceholderAPI")) {
				string = PlaceholdersProcessor.processPlaceholders(null, string);
			}
		} catch (Exception c) { Synergy.getLogger().error(c.getLocalizedMessage()); }

		// Process <translation> tags
		try {
        	if (string.contains("<translation>")) {
        		try {
        			string = Translation.processTranslationTags(string, language);
        		} catch (Exception a) {
        			Synergy.getLogger().error(a.getLocalizedMessage());

            		try {
	        			Synergy.getLogger().info("MERGED JSON "+JsonMerger.extractAndCombineText(string));
	        			string = Translation.processTranslationTags(JsonMerger.extractAndCombineText(string), language);
	        		} catch (Exception b) {
	        			Synergy.getLogger().error(b.getLocalizedMessage());
	        		}
        		}
        	}
		} catch (Exception c) { Synergy.getLogger().error(c.getLocalizedMessage()); }

		// Process <interactive> tags
		try {
        	if (string.contains("<interactive>")) {
        		string = Interactive.processInteractiveTags(string);
        	}
		} catch (Exception c) { Synergy.getLogger().error(c.getLocalizedMessage()); }

		return string;
	}
	
	
	
	
	/*
	 * <lang> tags processor
	 */
	
    public static String processLangTags(String input, String language) {
    	try {
	        String keyPattern = "<lang>(.*?)</lang>";
	        Pattern pattern = Pattern.compile(keyPattern);
	        Matcher matcher = pattern.matcher(input);
	
	        StringBuffer outputBuffer = new StringBuffer();
	        boolean found = false;
	        while (matcher.find()) {
	            found = true;
	            String translationKeyWithArgs = matcher.group(1);
	            String translationKey = translationKeyWithArgs.replaceAll("<arg>(.*?)</arg>", "");
	            HashMap<String, String> locales = LocalesManager.getLocales().getOrDefault(language, new HashMap<>());
	            String defaultTranslation = LocalesManager.getLocales().getOrDefault(getDefaultLanguage(), new HashMap<>()).getOrDefault(translationKey, translationKey);
	            String translatedText = locales.getOrDefault(translationKey, defaultTranslation);;
	            if (translatedText != null) {
	                String argsPattern = "<arg>(.*?)</arg>";
	                Pattern argsPatternPattern = Pattern.compile(argsPattern);
	                Matcher argsMatcher = argsPatternPattern.matcher(translationKeyWithArgs);
	                while (argsMatcher.find()) {
	                    String arg = argsMatcher.group(1);
	                    translatedText = translatedText.replaceFirst("%ARGUMENT%", arg);
	                }
	                matcher.appendReplacement(outputBuffer, translatedText);
	            }
	        }
	        matcher.appendTail(outputBuffer);
	        
	        if (found) {
	            return processLangTags(outputBuffer.toString(), language);
	        } else {
	            return outputBuffer.toString();
	        }
        } catch (Exception c) {
    		Synergy.getLogger().error(c.getLocalizedMessage());
    	}
    	
    	return removeLangTags(input);
    }
    
    public static String removeLangTags(String string) {
    	return string.replaceAll("<lang>(.*?)</lang>", "");
    }
	
		
	/*
	 * <translation> tags processor
	 */
	
    public static String processTranslationTags(String input, String languageCode) {
        Document doc = Jsoup.parse(input);
        StringBuilder result = new StringBuilder();
        processNode(doc.body(), languageCode, result);
        return result.toString().trim();
    }

    private static void processNode(Node node, String languageCode, StringBuilder result) {
        if (node instanceof TextNode) {
            result.append(((TextNode) node).text());
        } else if (node instanceof Element) {
            Element element = (Element) node;
            String tagName = element.tagName();
            if (tagName.equals("translation")) {
                Elements translations = element.children();
                for (Element translation : translations) {
                    if (translation.tagName().equals(languageCode)) {
                        processNode(translation, languageCode, result);
                        break;
                    }
                }
            } else {
            	if (!tagName.equals("body") && !tagName.equals(languageCode)) {
            		result.append("<").append(tagName).append(">");
            	}
                for (Node child : element.childNodes()) {
                    processNode(child, languageCode, result);
                }
            }
        }
    }
    
    public static String removeTranslationTags(String string) {
    	return string.replaceAll("<translation>(.*?)</translation>", "");
    }

}

