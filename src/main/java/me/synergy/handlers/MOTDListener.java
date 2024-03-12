package me.synergy.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import me.synergy.brains.Synergy;

public class MOTDListener implements Listener{
	
    public void initialize() {
        Synergy.getSpigotInstance().getServer().getPluginManager().registerEvents(this, Synergy.getSpigotInstance());
    }
	
	@EventHandler
    public void on(ServerListPingEvent event) {
		if (Synergy.getConfig().getBoolean("motd.enabled")) {
			event.setMotd(Synergy.getUtils().processColors(Synergy.getConfig().getString("motd.message")));
			event.setMaxPlayers(Synergy.getConfig().getInt("motd.max-players"));
		}
    }
}
