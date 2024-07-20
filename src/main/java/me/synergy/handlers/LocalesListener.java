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
import me.synergy.utils.Color;
import me.synergy.utils.Interactive;
import me.synergy.utils.Translation;

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
		                    		Synergy.getLogger().info("HOW IT LOOKS: "+component.getJson());
		                    		component.setJson(Translation.translate(component.getJson(), bread.getLanguage()));
		                    		Interactive.executeInteractive(component.getJson(), bread);
		                    	} catch (Exception c) { Synergy.getLogger().error(c.getLocalizedMessage()); }
	                    		Synergy.getLogger().info("HOW IT BECAME: "+component.getJson());

		                    	try {
		                    		component.setJson(Color.color(component.getJson(), bread.getTheme()));
		                    	} catch (Exception c) { Synergy.getLogger().error(c.getLocalizedMessage()); }
		                    	
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
        BreadMaker bread = Synergy.getBread(event.getPlayer().getUniqueId());
		String reason = Translation.translate(event.getReason(), bread.getLanguage());
		reason = Color.color(reason, bread.getTheme());
		reason = Interactive.removeInteractiveTags(reason);
		event.setReason(reason);
	}
}
