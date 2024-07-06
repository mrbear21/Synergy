package me.synergy.handlers;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
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
import me.synergy.utils.ColorTagProcessor;
import me.synergy.utils.InteractiveTagProcessor;
import me.synergy.utils.LangTagProcessor;
import me.synergy.utils.PlaceholdersProcessor;
import me.synergy.utils.Utils;

public class LocalesListener implements Listener {
	
	public void initialize() {
		try {
			
			if (!Synergy.getConfig().getBoolean("localizations.enabled")) {
				return;
			}
			if (!Synergy.isDependencyAvailable("ProtocolLib")) {
				Synergy.getLogger().warning("ProtocolLib is required to initialize "+this.getClass().getSimpleName()+" module!");
				return;
			}
			
			Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());

			Synergy.getSpigot().getProtocolManager().addPacketListener(
			    new PacketAdapter(Synergy.getSpigot(), ListenerPriority.MONITOR, PacketType.Play.Server.SYSTEM_CHAT) {
			        @Override
			        public void onPacketSending(PacketEvent event) {
			            try {
			                PacketContainer packet = event.getPacket();
			                BreadMaker bread = Synergy.getBread(event.getPlayer().getUniqueId());
			                List<WrappedChatComponent> components = packet.getChatComponents().getValues();

		                    //Synergy.getLogger().info("Received action bar message from player: " + event.getPacket().getStrings());
		                    
			                for (WrappedChatComponent component : components) {
		                    	try {
			                    	if (component.getJson().contains("<lang>")) {
			                    		component.setJson(LangTagProcessor.processLangTags(component.getJson(), bread.getLanguage()));
			                    		component.setJson(PlaceholdersProcessor.processPlaceholders(event.getPlayer(), component.getJson()));
			                    		component.setJson(LangTagProcessor.processPortableLangTags(component.getJson(), bread.getLanguage()));
			                    	}
			                    	//Synergy.getLogger().info("BEFORE: "+component.getJson());
			                    	if (component.getJson().contains("<interactive>")) {
			                    		component.setJson(InteractiveTagProcessor.processInteractiveTags(component.getJson(), bread));
			                    	}
			                    	
			                    	component.setJson(ColorTagProcessor.processThemeTags(component.getJson(), bread.getTheme()));
			                    	
			                    	component.setJson(ColorTagProcessor.processColorReplace(component.getJson(), bread.getTheme()));
			                    	
			                    	if (component.getJson().contains("<#")) {
			                    		//component.setJson(Utils.applyGradient(component.getJson()));
			                    		component.setJson(ColorTagProcessor.processColorTags(component.getJson()));
			                    	}
			                    	component.setJson(Utils.processColors(component.getJson()));
			                    	//Synergy.getLogger().info("AFTER: "+component.getJson());
		                    	} catch (Exception c) {
		                    		c.printStackTrace();
		                    	}
		                        packet.getChatComponents().write(components.indexOf(component), component);    
		                    }
			            } catch (Exception e) {
			                Synergy.getLogger().error(e.getMessage());
			            }
			        }
			    }
			);	

            /*if (itemMeta.hasLore()) {
            List<String> lore = itemMeta.getLore();
            for (int i1 = 0; i1 < lore.size(); i1++) {
                translate = bread.getLocales().translateString(lore.get(i1));
                lore.set(i1, translate);
                itemMeta.setLore(lore);
            }
        }*/
			
			Synergy.getSpigot().getProtocolManager().addPacketListener(
				new PacketAdapter(Synergy.getSpigot(), ListenerPriority.MONITOR, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
						@Override
			            public void onPacketSending(PacketEvent event) {
			                PacketContainer packet = event.getPacket();
			                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
			                    ItemStack itemStack = packet.getItemModifier().read(0);
			                    if (itemStack != null) {
			                        ItemMeta itemMeta = itemStack.getItemMeta();
			                        if (itemMeta != null) {
			                            BreadMaker bread = Synergy.getBread(event.getPlayer().getUniqueId());
			                            String translate = itemMeta.getDisplayName() != null ? LangTagProcessor.processLangTags(itemMeta.getDisplayName(), bread.getLanguage()) : itemMeta.getDisplayName();
			                            itemMeta.setDisplayName(translate);
			                            itemStack.setItemMeta(itemMeta);
			                            packet.getItemModifier().write(0, itemStack);
			                        }
			                    }
			                } else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
			                    StructureModifier<ItemStack[]> itemArrayModifier = packet.getItemArrayModifier();
			                    for (int i = 0; i < itemArrayModifier.size(); i++) {
			                        ItemStack[] itemStacks = itemArrayModifier.read(i);
			                        if (itemStacks != null) {
			                            for (int j = 0; j < itemStacks.length; j++) {
			                                ItemStack itemStack = itemStacks[j]; // Use correct index j instead of i
			                                if (itemStack != null) {
			                                    ItemMeta itemMeta = itemStack.getItemMeta();
			                                    if (itemMeta != null) {
			                                        BreadMaker bread = Synergy.getBread(event.getPlayer().getUniqueId());
			                                        String translate = itemMeta.getDisplayName() != null ? LangTagProcessor.processLangTags(itemMeta.getDisplayName(), bread.getLanguage()) : itemMeta.getDisplayName();
			                                        itemMeta.setDisplayName(translate);
			                                        itemStack.setItemMeta(itemMeta);
			                                        itemStacks[j] = itemStack; // Update the itemStack back in the array
			                                    }
			                                }
			                            }
			                            itemArrayModifier.write(i, itemStacks); // Write back the modified array
			                        }
			                    }
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
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		String reason = LangTagProcessor.processLangTags(event.getReason(), Synergy.getBread(event.getPlayer().getUniqueId()).getLanguage());
		reason = Utils.processColors(reason);
		reason = ColorTagProcessor.removeColorTags(reason);
		reason = InteractiveTagProcessor.removeInteractiveTags(reason);
		event.setReason(reason);
	}
}
