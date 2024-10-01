package me.synergy.brains;

import java.util.UUID;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import me.synergy.bungee.handlers.PlayerJoinListener;
import me.synergy.bungee.handlers.SynergyCommand;
import me.synergy.discord.Discord;
import me.synergy.events.SynergyEvent;
import me.synergy.modules.Config;
import me.synergy.modules.DataManager;
import me.synergy.modules.LocalesManager;
import me.synergy.modules.WebServer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class Bungee extends Plugin implements Listener {

	private static Bungee INSTANCE;
	
	@Override
	public void onEnable() {
		
    	INSTANCE = this;
    	Synergy.platform = "bungee";
    	
	    new Config().initialize();
	    new DataManager().initialize();
        new LocalesManager().initialize();
	    new Discord().initialize();
	    new PlayerJoinListener().initialize();
	    new SynergyCommand().initialize();
	    new WebServer().initialize();

	    getProxy().getPluginManager().registerListener(this, this);
	    
	    getProxy().registerChannel("net:synergy");
	    
		getLogger().info("Synergy is ready to be helpful for all beadmakers!");
	}

	public void onDisable() {
		getLogger().info("Synergy has stopped it's service!");

	}
	
	public Config getConfig() {
		return new Config();
	}

	public static Bungee getInstance() {
		return INSTANCE;
	}

	@EventHandler
	public void onPluginMessage(PluginMessageEvent event) {
	    if (!event.getTag().equals("net:synergy")) {
	        return;
	    }
	    
	    ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
	    String identifier = in.readUTF();
	    String stringUUID = in.readUTF();
	    
	    UUID uuid = null;
	    if (stringUUID != null && !stringUUID.isEmpty()) {
	        try {
	            uuid = UUID.fromString(stringUUID);
	        } catch (IllegalArgumentException e) {
	            //getLogger().warning("Received invalid UUID string: " + stringUUID);
	        }
	    }
	    
	    String data = in.readUTF();
	    
	    new SynergyEvent(identifier, uuid, data).send();
	    new SynergyEvent(identifier, uuid, data).fireEvent();
	}

	public String getPlayerName(UUID uniqueId) {
		return getProxy().getPlayer(uniqueId).getDisplayName();
	}
	
}