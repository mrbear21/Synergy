package me.synergy.modules;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import me.synergy.brains.Synergy;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class LocalesManager implements Listener {

	private static Map<String, HashMap<String, String>> LOCALES;
	
	public LocalesManager() {
	}

	public void initialize() {
		try {
			loadLocales();
			
			if (!Synergy.getConfig().getBoolean("localizations.enabled")) {
				return;
			}
			if (!Synergy.isDependencyAvailable("ProtocolLib")) {
				Synergy.getLogger().warning("ProtocolLib is required to initialize "+this.getClass().getSimpleName()+" module!");
				return;
			}
			
			Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());

			Synergy.getSpigot().getProtocolManager().addPacketListener(
				new PacketAdapter(Synergy.getSpigot(), ListenerPriority.MONITOR, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
					@Override
		            public void onPacketSending(PacketEvent event) {

		                PacketContainer packet = event.getPacket();
		                BreadMaker bread = Synergy.getBread(event.getPlayer().getUniqueId());

		                String language = bread.getLanguage();
		                
		                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
		                	
		                    ItemStack itemStack = packet.getItemModifier().read(0);
		                    ItemMeta itemMeta = itemStack.getItemMeta();
			                if (itemStack != null && itemMeta != null) {
			                    String translate = itemMeta.getDisplayName() != null ? translateString(itemMeta.getDisplayName(), language) : itemMeta.getDisplayName();
			                    itemMeta.setDisplayName(translate);
			                    if (itemMeta.hasLore()) {
			                        List<String> lore = itemMeta.getLore();
			                        for (int i1 = 0; i1 < lore.size(); i1++) {
					                    translate = translateString(lore.get(i1), language);
			                            lore.set(i1, translate);
			                            itemMeta.setLore(lore);
			                        }
			                    }
			                    itemStack.setItemMeta(itemMeta);
			                }
			                packet.getItemModifier().write(0, itemStack);
			                
		                } else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
		                    StructureModifier<ItemStack[]> itemArrayModifier = packet.getItemArrayModifier();
		                    for (int i = 0; i < itemArrayModifier.size(); i++) {
		                        ItemStack[] itemStacks = itemArrayModifier.read(i);
		                        if (itemStacks != null) {
		                            for (int j = 0; j < itemStacks.length; j++) {
		                                ItemStack itemStack = itemStacks[i];
					                    ItemMeta itemMeta = itemStack.getItemMeta();
						                if (itemStack != null && itemMeta != null) {
						                    String translate = itemMeta.getDisplayName() != null ? translateString(itemMeta.getDisplayName(), language) : itemMeta.getDisplayName();
						                    itemMeta.setDisplayName(translate);
						                    if (itemMeta.hasLore()) {
						                        List<String> lore = itemMeta.getLore();
						                        for (int i1 = 0; i1 < lore.size(); i1++) {
								                    translate = translateString(lore.get(i1), language);
						                            lore.set(i1, translate);
						                            itemMeta.setLore(lore);
						                        }
						                    }
						                    itemStack.setItemMeta(itemMeta);
						                }
		                            }
		                        }
		                    }
		                }
					}
				}
			);

			Synergy.getSpigot().getProtocolManager().addPacketListener(
				    new PacketAdapter(Synergy.getSpigot(), ListenerPriority.MONITOR, PacketType.Play.Server.SYSTEM_CHAT) {
				        @Override
				        public void onPacketSending(PacketEvent event) {
				            try {
				                PacketContainer packet = event.getPacket();
				                BreadMaker bread = Synergy.getBread(event.getPlayer().getUniqueId());
				                String language = bread.getLanguage();
		                        HashMap<String, String> locales = getLocales().get(language);
		                        if (locales != null) {
					                List<WrappedChatComponent> components = packet.getChatComponents().getValues();
					                for (WrappedChatComponent component : components) {
					                    if (component != null) {
					                    	
					                    	component.setJson(processLangTags(component.getJson(), language));
					                    	
					                    	component.setJson(translateString(component.getJson(), language));
					                       
					                        packet.getChatComponents().write(components.indexOf(component), component);    
				                        }
				                    }
				                }
				            } catch (Exception e) {
				                e.printStackTrace();
				            }
				        }
				    }
				);

			Synergy.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
		} catch (Exception c) {
			Synergy.getLogger().error(this.getClass().getSimpleName()+" module failed to initialize:");
			c.printStackTrace();
		}
    }
	
	public String translateString(String string, String language) {
		if (Synergy.getConfig().getBoolean("localizations.enabled")) {
			HashMap<String, String> locales = getLocales().get(language);
			locales = locales.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).collect(Collectors.toMap(
	                Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new ));
			
			for (Entry<String, String> locale : locales.entrySet()) {
				string = string.replace(locale.getKey(), locale.getValue());
			}
		}
		string = string.replace("%nl%", System.lineSeparator());
		string = string.replace("%RANDOM%", String.valueOf(new Random().nextInt(99)));
		
		return string;
	}
	
	public String translateStringColorStripped(String string, String defaultLanguage) {
		return ChatColor.stripColor(translateString(string, defaultLanguage));
	}
	
    public static String processLangTags(String input, String language) {

        String keyPattern = "<lang>(.*?)</lang>";
        Pattern pattern = Pattern.compile(keyPattern);
        Matcher matcher = pattern.matcher(input);

        StringBuffer outputBuffer = new StringBuffer();
        while (matcher.find()) {
            String translationKeyWithArgs = matcher.group(1);
            String translationKey = translationKeyWithArgs.replaceAll("<arg>(.*?)</arg>", "");
            LocalesManager localesManager = Synergy.getLocalesManager();
            HashMap<String, String> locales = localesManager.getLocales().getOrDefault(language, new HashMap<String, String>());
            String translatedText = locales.getOrDefault(translationKey, localesManager.getLocales().get(localesManager.getDefaultLanguage()).getOrDefault(translationKey, "Translation not found for key: " + translationKey));
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
        return outputBuffer.toString();
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
	                    translationMap.put(key, Utils.processColors(translation));
	                    count++;
	                    getLocales().put(language, translationMap);
	                } else if (subSection.isList(language)) {
	                    List<String> translations = subSection.getStringList(language);
	                    StringBuilder sb = new StringBuilder();
	                    for (String translation : translations) {
	                        translation = Utils.processColors(translation);
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
	
	public Map<String, HashMap<String, String>> getLocales() {
		return LOCALES;
	}
	
    public String getDefaultLanguage() {
        return Synergy.getConfig().getString("localizations.default-language");
    }
	
}