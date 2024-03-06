package me.synergy.modules;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import me.synergy.brains.Spigot;
import me.synergy.events.SynergyPluginMessage;
import me.synergy.objects.BreadMaker;

public class Localizations implements Listener {

    private Spigot spigot;
    private ProtocolManager protocolManager;

    public Localizations(Spigot spigot) {
        this.spigot = spigot;
        this.protocolManager = spigot.getProtocolManager();
    }

	public void initialize() {
		try {
			if (!spigot.getConfig().getBoolean("localizations.enabled")) {
				return;
			}
			if (!spigot.isDependencyAvailable("ProtocolLib")) {
				spigot.getLogger().warning("ProtocolLib is required to initialize "+this.getClass().getSimpleName()+" module!");
				return;
			}
			
			Bukkit.getPluginManager().registerEvents(new Localizations(spigot), spigot);
			loadLocales();
	
			protocolManager.addPacketListener(
				new PacketAdapter(spigot, ListenerPriority.MONITOR, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
					@Override
		            public void onPacketSending(PacketEvent event) {

		                PacketContainer packet = event.getPacket();
		                BreadMaker bread = spigot.getBread(event.getPlayer().getName());

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

			protocolManager.addPacketListener(
			    new PacketAdapter(spigot, ListenerPriority.MONITOR, PacketType.Play.Server.SYSTEM_CHAT) {
			        @Override
			        public void onPacketSending(PacketEvent event) {
			            try {
			                PacketContainer packet = event.getPacket();
			                BreadMaker bread = spigot.getBread(event.getPlayer().getName());
			                String language = bread.getLanguage();
	                        HashMap<String, String> locales = spigot.getLocales().get(language);
	                        if (locales != null) {
				                List<WrappedChatComponent> components = packet.getChatComponents().getValues();
				                for (WrappedChatComponent component : components) {
				                    if (component != null) {
				                        locales = locales.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).collect(Collectors.toMap(
				                                Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
				                        locales.entrySet().forEach(l ->
				                                component.setJson(component.getJson().replace(l.getKey(), ChatColor.GOLD + l.getValue()).replace("%nl%", System.lineSeparator())));
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
			spigot.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
		} catch (Exception c) {
			spigot.getLogger().warning(this.getClass().getSimpleName()+" module failed to initialize:");
			c.printStackTrace();
		}
    }
    
	public String translateString(String string, String language) {

		HashMap<String, String> locales = spigot.getLocales().get(language);
		locales = locales.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new ));
		
		for (Entry<String, String> locale : locales.entrySet()) {
			string = string.replace(locale.getKey(), locale.getValue());
		}
		return string;
	}
	
	public void loadLocales() {
		
		if (!new File(spigot.getDataFolder(), "locales.yml").exists()) {
			spigot.getLogger().info("Creating locales file...");
			try {
				spigot.saveResource("locales.yml", false);
			} catch (Exception c) { c.printStackTrace(); }
		}
		
        File localesFile = new File(spigot.getDataFolder(), "locales.yml");
        if (localesFile.exists()) {
            try {
            	spigot.getLocalesFile().load(localesFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
		
	    int count = 0;
	    for (String key : spigot.getLocalesFile().getKeys(false)) {
	        ConfigurationSection subSection = spigot.getLocalesFile().getConfigurationSection(key);
	        if (subSection != null) {
	            for (String language : subSection.getKeys(false)) {
	                HashMap<String, String> translationMap = spigot.getLocales().getOrDefault(language, new HashMap<>());
	                translationMap.put(key, ChatColor.translateAlternateColorCodes('&', subSection.getString(language)));
	                count++;
	                spigot.getLocales().put(language, translationMap);
	            }
	        }
	    }
	    spigot.getLogger().info("There were "+count+" translations initialized!");
	}

    @EventHandler
    public void getMessage(SynergyPluginMessage e) {
        if (!e.getIdentifier().equals("locales")) {
            return;
        }

    }

}

