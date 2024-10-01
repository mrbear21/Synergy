package me.synergy.bungee.handlers;

import java.util.concurrent.TimeUnit;

import me.synergy.anotations.SynergyHandler;
import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;

public class ServersManager implements Listener {

	public void initialize() {
		Synergy.getEventManager().registerEvents(this);
        Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
	}
	
	@SynergyHandler
	public void onSynergyEvent(SynergyEvent event) {
	    if (event.getIdentifier().equals("server-stopping")) {
	        
	        ProxyServer.getInstance().getScheduler().schedule(Synergy.getBungee(), new Runnable() {
	            @Override
	            public void run() {
	                String serverName = event.getOption("server").getAsString();
	                ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(serverName);
	                
	                if (serverInfo != null && serverInfo.getPlayers().size() == 0) {
	                    Synergy.createSynergyEvent("discord-broadcast")
	                            .setOption("message", "🛑 Сервер " + serverName + " вимкнено")
	                            .send();
	                }
	            }
	        }, 15, TimeUnit.SECONDS);
	    }
	}
	
}
