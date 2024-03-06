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

import me.synergy.events.SynergyVelocityPluginMessage;
import me.synergy.modules.Discord;
import me.synergy.modules.SynergyConfig;
import net.dv8tion.jda.api.JDA;

@SuppressWarnings("unused")
@Plugin(id = "synergy", name = "Synergy", version = "0.0.1-SNAPSHOT",
url = "archi.quest", description = "Basic tools and messaging plugin", authors = {"mrbear22"})
public class Velocity {

    private ProxyServer server;
    private Logger logger;
	public JDA jda;
    public Map<String, Object> configValues;
	public Object config;
	private static Velocity INSTANCE;
    
    public static final String CHANNEL_NAME = "net:synergy";
    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(CHANNEL_NAME);

    @Inject
    public Velocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.info("Synergy is ready to be helpful for all beadmakers!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    	INSTANCE = this;
    	Synergy.platform = "velocity";
	    server.getChannelRegistrar().register(IDENTIFIER);
	    new SynergyConfig(this).initialize();
	  //  new Discord(this).register();
    }

    @Subscribe
    public void onPluginMessageFromSpigot(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(IDENTIFIER)) {
            return;
        }
        
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.write(event.getData());
        byte[] messageData = out.toByteArray();
        for (RegisteredServer registeredServer : server.getAllServers()) {
            registeredServer.sendPluginMessage(IDENTIFIER, messageData);
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String identifier = in.readUTF();
        List<String> argsList = new ArrayList<>();
        try {
            while (true) {
                argsList.add(in.readUTF());
            }
        } catch (Exception ignored) {}
        String[] args = argsList.toArray(new String[0]);
        server.getEventManager().fire(new SynergyVelocityPluginMessage(identifier, args));
        
    }
    
	@Subscribe(order = PostOrder.EARLY)
	public void onEvent(SynergyVelocityPluginMessage e) {
        if (!e.getIdentifier().equals("chat")) {
            return;
        }
		getLogger().info(e.getIdentifier());
	}
    
    public JDA getJda() {
    	return jda;
    }

    public Logger getLogger() {
    	return logger;
    }
    
    public ProxyServer getProxy() {
    	return server;
    }

	public SynergyConfig getConfig() {
		return new SynergyConfig(this);
	}

	public static Velocity getInstance() {
		return INSTANCE;
	}
    
}
