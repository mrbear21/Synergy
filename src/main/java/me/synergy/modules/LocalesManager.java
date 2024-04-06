package me.synergy.modules;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import me.synergy.brains.Synergy;
import me.synergy.utils.LangTagProcessor;
import net.md_5.bungee.api.ChatColor;

public class LocalesManager {

	private static Map<String, HashMap<String, String>> LOCALES;
	
	public LocalesManager() {
	}

	public void initialize() {
		try {
			loadLocales();
			
			if (!Synergy.getConfig().getBoolean("localizations.enabled")) {
				return;
			}

			Synergy.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
		} catch (Exception c) {
			Synergy.getLogger().error(this.getClass().getSimpleName()+" module failed to initialize:");
			c.printStackTrace();
		}
    }

	@Deprecated
	public static String translateString(String string, String language) {
		if (Synergy.getConfig().getBoolean("localizations.enabled")) {
			for (String lang : new String[] {language, getDefaultLanguage()}) {
				HashMap<String, String> locales = getLocales().get(lang);
				locales = locales.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).collect(Collectors.toMap(
		                Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new ));
				
				for (Entry<String, String> locale : locales.entrySet()) {
					string = string.replace(locale.getKey(), locale.getValue());
				}
			}
		}
		string = string.replace("%nl%", System.lineSeparator());
		string = string.replace("%RANDOM%", String.valueOf(new Random().nextInt(99)));
		
		return string;
	}
	
    public static String removeColorCodes(String text) {
        String pattern = "<#[0-9A-Fa-f]{6}>";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        String result = m.replaceAll("");
        
        return result;
    }

	public static String translateStringColorStripped(String string, String defaultLanguage) {

		return removeColorCodes(ChatColor.stripColor(LangTagProcessor.processLangTags(string, defaultLanguage)));
	}
	

	public void loadLocales() {
		
		LOCALES = new HashMap<String, HashMap<String, String>>();
		
		if (!new File(Synergy.getSpigot().getDataFolder(), "locales.yml").exists()) {
			Synergy.getLogger().info("Creating locales file...");
			try {
				Synergy.getSpigot().saveResource("locales.yml", false);
			} catch (Exception c) { c.printStackTrace(); }
		}
		
        File localesFile = new File(Synergy.getSpigot().getDataFolder(), "locales.yml");
        if (localesFile.exists()) {
            try {
            	Synergy.getSpigot().getLocalesFile().load(localesFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
		
	    int count = 0;
	    for (String key : Synergy.getSpigot().getLocalesFile().getKeys(false)) {
	        ConfigurationSection subSection = Synergy.getSpigot().getLocalesFile().getConfigurationSection(key);
	        if (subSection != null) {
	            for (String language : subSection.getKeys(false)) {
	                if (subSection.isString(language)) {
	                    String translation = subSection.getString(language);
	                    HashMap<String, String> translationMap = getLocales().getOrDefault(language, new HashMap<>());
	                    translation = translation.replace("%nl%", System.lineSeparator());
	                    translationMap.put(key, translation);
	                    count++;
	                    getLocales().put(language, translationMap);
	                } else if (subSection.isList(language)) {
	                    List<String> translations = subSection.getStringList(language);
	                    StringBuilder sb = new StringBuilder();
	                    for (String translation : translations) {
	                        sb.append(translation).append("\n");
	                    }
	                    if (sb.length() > 0) {
	                        sb.setLength(sb.length() - 1);
	                    }
	                    String combinedTranslations = sb.toString();
	                    combinedTranslations = combinedTranslations.replace("%nl%", System.lineSeparator());
	                    HashMap<String, String> translationMap = getLocales().getOrDefault(language, new HashMap<>());
	                    translationMap.put(key, combinedTranslations);
	                    count++;
	                    getLocales().put(language, translationMap);
	                } else {
	                    // Other types
	                }
	            }
	        }
	    }
	    Synergy.getLogger().info("There were "+count+" translations initialized!");
	}
	
	public Set<String> getLanguages() {
		return getLocales().keySet();
	}
	
	public static Map<String, HashMap<String, String>> getLocales() {
		return LOCALES;
	}
	
    public static String getDefaultLanguage() {
        return Synergy.getConfig().getString("localizations.default-language");
    }
	
}