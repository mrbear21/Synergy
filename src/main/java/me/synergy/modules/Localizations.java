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

public class Localizations implements Listener {

	public Localizations() {
	}

	public void initialize() {
		try {
			if (!Synergy.getConfig().getBoolean("localizations.enabled")) {
				return;
			}
			if (!Synergy.isDependencyAvailable("ProtocolLib")) {
				Synergy.getLogger().warning("ProtocolLib is required to initialize "+this.getClass().getSimpleName()+" module!");
				return;
			}
			
			Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigotInstance());
			loadLocales();
	
			Synergy.getSpigotInstance().getProtocolManager().addPacketListener(
				new PacketAdapter(Synergy.getSpigotInstance(), ListenerPriority.MONITOR, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
					@Override
		            public void onPacketSending(PacketEvent event) {

		                PacketContainer packet = event.getPacket();
		                BreadMaker bread = Synergy.getBread(event.getPlayer().getName());

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

			Synergy.getSpigotInstance().getProtocolManager().addPacketListener(
				    new PacketAdapter(Synergy.getSpigotInstance(), ListenerPriority.MONITOR, PacketType.Play.Server.SYSTEM_CHAT) {
				        @Override
				        public void onPacketSending(PacketEvent event) {
				            try {
				                PacketContainer packet = event.getPacket();
				                BreadMaker bread = Synergy.getBread(event.getPlayer().getName());
				                String language = bread.getLanguage();
		                        HashMap<String, String> locales = Synergy.getSpigotInstance().getLocales().get(language);
		                        if (locales != null) {
					                List<WrappedChatComponent> components = packet.getChatComponents().getValues();
					                for (WrappedChatComponent component : components) {
					                    if (component != null) {
					                        locales = locales.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).collect(Collectors.toMap(
					                                Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
					                        locales.entrySet().forEach(l ->
					                                component.setJson(component.getJson().replace(l.getKey(), l.getValue()).replace("%RANDOM%", ""+new Random().nextInt(99))));
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
			HashMap<String, String> locales = Synergy.getSpigotInstance().getLocales().get(language);
			locales = locales.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).collect(Collectors.toMap(
	                Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new ));
			
			for (Entry<String, String> locale : locales.entrySet()) {
				string = string.replace(locale.getKey(), locale.getValue());
			}
		}
		string = string.replace("%nl%", System.lineSeparator());
		return string;
	}
	
	public String translateStringColorStripped(String string, String defaultLanguage) {
		return ChatColor.stripColor(translateString(string, Synergy.getDefaultLanguage()));
	}
	
	public void loadLocales() {
		
		if (!new File(Synergy.getSpigotInstance().getDataFolder(), "locales.yml").exists()) {
			Synergy.getLogger().info("Creating locales file...");
			try {
				Synergy.getSpigotInstance().saveResource("locales.yml", false);
			} catch (Exception c) { c.printStackTrace(); }
		}
		
        File localesFile = new File(Synergy.getSpigotInstance().getDataFolder(), "locales.yml");
        if (localesFile.exists()) {
            try {
            	Synergy.getSpigotInstance().getLocalesFile().load(localesFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
		
	    int count = 0;
	    for (String key : Synergy.getSpigotInstance().getLocalesFile().getKeys(false)) {
	        ConfigurationSection subSection = Synergy.getSpigotInstance().getLocalesFile().getConfigurationSection(key);
	        if (subSection != null) {
	            for (String language : subSection.getKeys(false)) {
	                if (subSection.isString(language)) {
	                    String translation = subSection.getString(language);
	                    HashMap<String, String> translationMap = Synergy.getSpigotInstance().getLocales().getOrDefault(language, new HashMap<>());
	                    translation = translation.replace("%nl%", System.lineSeparator());
	                    translationMap.put(key, Synergy.getUtils().processColors(translation));
	                    count++;
	                    Synergy.getSpigotInstance().getLocales().put(language, translationMap);
	                } else if (subSection.isList(language)) {
	                    List<String> translations = subSection.getStringList(language);
	                    StringBuilder sb = new StringBuilder();
	                    for (String translation : translations) {
	                        translation = new Utils().processColors(translation);
	                        sb.append(translation).append("\n");
	                    }
	                    if (sb.length() > 0) {
	                        sb.setLength(sb.length() - 1);
	                    }
	                    String combinedTranslations = sb.toString();
	                    combinedTranslations = combinedTranslations.replace("%nl%", System.lineSeparator());
	                    HashMap<String, String> translationMap = Synergy.getSpigotInstance().getLocales().getOrDefault(language, new HashMap<>());
	                    translationMap.put(key, combinedTranslations);
	                    count++;
	                    Synergy.getSpigotInstance().getLocales().put(language, translationMap);
	                } else {
	                    // Other types
	                }
	            }
	        }
	    }
	    Synergy.getLogger().info("There were "+count+" translations initialized!");
	}


}