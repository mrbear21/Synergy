package me.synergy.brains;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import me.synergy.events.SynergyVelocityEvent;
import me.synergy.modules.Discord;
import me.synergy.modules.Config;
import net.dv8tion.jda.api.JDA;

@SuppressWarnings("unused")
@Plugin(id = "synergy", name = "Synergy", version = "0.0.1-SNAPSHOT",
url = "archi.quest", description = "Basic tools and messaging plugin", authors = {"mrbear22"})
public class Velocity {

    private ProxyServer server;
    private static Logger logger;
    public Map<String, Object> configValues;
	public Object config;
	private static Velocity INSTANCE;
    
    public static final String CHANNEL_NAME = "net:synergy";
    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(CHANNEL_NAME);

    @Inject
    public Velocity(ProxyServer server, Logger logger) {
        this.server = server;
        Velocity.setLogger(logger);

        logger.info("Synergy is ready to be helpful for all beadmakers!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    	INSTANCE = this;
    	Synergy.platform = "velocity";
	    server.getChannelRegistrar().register(IDENTIFIER);
	    new Config().initialize();
	  //  new Discord(this).register();
    }

    @Subscribe
    public void onPluginMessageFromSpigot(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(IDENTIFIER)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String token = Synergy.getSynergyToken();
        String identifier = in.readUTF();
        String player = in.readUTF();
        String waitForPlayer = in.readUTF();
        List<String> argsList = new ArrayList<>();
        try {
            while (true) {
                argsList.add(in.readUTF());
            }
        } catch (Exception ignored) {}
        String[] args = argsList.toArray(new String[0]);
        
        if (!Boolean.valueOf(waitForPlayer)) {
	        server.getEventManager().fire(new SynergyVelocityEvent(identifier, player, args));
	        
	        ByteArrayDataOutput out = ByteStreams.newDataOutput();
	        out.writeUTF(token);
	        out.writeUTF(identifier);
	        out.writeUTF(player);
	        out.writeUTF(waitForPlayer);
	        for (String arg : argsList) {
		        out.writeUTF(arg);
	        }
	        
	        for (RegisteredServer registeredServer : server.getAllServers()) {
	            registeredServer.sendPluginMessage(IDENTIFIER, out.toByteArray());
	        }
	        
        } else {
        	
        }


        
    }
    
	@Subscribe(order = PostOrder.EARLY)
	public void onEvent(SynergyVelocityEvent e) {
        if (!e.getIdentifier().equals("chat")) {
            return;
        }
		getLogger().info(e.getIdentifier());
	}
    
    public static Logger getLogger() {
    	return logger;
    }
    
    public ProxyServer getProxy() {
    	return server;
    }

	public Config getConfig() {
		return new Config();
	}

	public static Velocity getInstance() {
		return INSTANCE;
	}

	public static void setLogger(Logger logger) {
		Velocity.logger = logger;
	}
    
}
