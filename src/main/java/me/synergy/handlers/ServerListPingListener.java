package me.synergy.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import me.synergy.brains.Synergy;
import me.synergy.utils.PlaceholdersProcessor;
import me.synergy.utils.Utils;

public class ServerListPingListener implements Listener{
	
    public void initialize() {
        Synergy.getSpigot().getServer().getPluginManager().registerEvents(this, Synergy.getSpigot());
    }
	
	@EventHandler
    public void onPing(ServerListPingEvent event) {
		if (Synergy.getConfig().getBoolean("motd.enabled")) {
			String motd = Utils.processColors(Synergy.getConfig().getString("motd.message"));
			motd = PlaceholdersProcessor.processPlaceholders(null, motd);
			event.setMotd(motd);
			event.setMaxPlayers(Synergy.getConfig().getInt("motd.max-players"));
		}
    }
}
