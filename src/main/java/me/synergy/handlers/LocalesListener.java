package me.synergy.handlers;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import me.synergy.brains.Synergy;
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
			                List<WrappedChatComponent> components = packet.getChatComponents().getValues();
			                for (WrappedChatComponent component : components) {
		                    	try {
			                    	if (component.getJson().contains("<lang>")) {
			                    		component.setJson(LangTagProcessor.processLangTags(component.getJson(), bread.getLanguage()));
			                    	}
			                    	if (component.getJson().contains("<interactive>")) {
			                    		component.setJson(InteractiveTagProcessor.processInteractiveTags(component.getJson(), bread));
			                    	}
			                    	if (component.getJson().contains("<#")) {
			                    		component.setJson(ColorTagProcessor.processColorTags(component.getJson()));
			                    	}
			                    	component.setJson(Utils.processColors(component.getJson()));

			                    	//Synergy.getLogger().info("BEFORE: "+component.getJson());
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
