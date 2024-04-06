package me.synergy.handlers;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import me.synergy.brains.Synergy;
import me.synergy.modules.LocalesManager;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.ColorTagProcessor;
import me.synergy.utils.InteractiveTagProcessor;
import me.synergy.utils.LangTagProcessor;
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
		                        if (LocalesManager.getLocales().get(bread.getLanguage()) != null) {
					                List<WrappedChatComponent> components = packet.getChatComponents().getValues();
					                for (WrappedChatComponent component : components) {
					                    if (component != null) {
					                    	try {
						                    	component.setJson(LangTagProcessor.processLangTags(component.getJson(), bread.getLanguage()));
						                    	//Synergy.getLogger().info("BEFORE: "+component.getJson());
						                    	component.setJson(InteractiveTagProcessor.processInteractiveTags(component.getJson()));
						                    	//Synergy.getLogger().info("AFTER: "+component.getJson());
						                    	component.setJson(ColorTagProcessor.processColorTags(component.getJson()));
						                    	component.setJson(Utils.processColors(component.getJson()));
					                    	} catch (Exception c) {
					                    		c.printStackTrace();
					                    	}
					                        packet.getChatComponents().write(components.indexOf(component), component);    
				                        }
				                    }
				                }
				            } catch (Exception e) {
				                Synergy.getLogger().error(e.getMessage());
				            }
				        }
				    }
				);
/*
			Synergy.getSpigot().getProtocolManager().addPacketListener(
				    new PacketAdapter(Synergy.getSpigot(), ListenerPriority.MONITOR, PacketType.Play.Server.SYSTEM_CHAT) {
				        @Override
				        public void onPacketSending(PacketEvent event) {
				            try {
				                PacketContainer packet = event.getPacket();
				                
				                // Отримуємо текстове повідомлення з пакету System Chat Message
				                String actionBarText = packet.getChatComponents().read(0).getJson();
				                Synergy.getLogger().info("Action Bar Text: " + actionBarText);
				                
				            } catch (Exception e) {
				                Synergy.getLogger().error(e.getMessage());
				            }
				        }
				    }
				);
*/
			
			
			Synergy.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
		} catch (Exception c) {
			Synergy.getLogger().error(this.getClass().getSimpleName()+" module failed to initialize:");
			c.printStackTrace();
		}
    }
}
