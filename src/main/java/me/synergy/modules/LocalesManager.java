package me.synergy.modules;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import me.synergy.brains.Synergy;


public class LocalesManager {

	private static Map<String, HashMap<String, String>> LOCALES = new HashMap<String, HashMap<String, String>>();
	
	public LocalesManager() {}

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

	public void loadLocales() {
		
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
}