package me.synergy.brain;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import me.synergy.modules.Discord;
import me.synergy.objects.Config;
import net.dv8tion.jda.api.JDA;

@SuppressWarnings("unused")
@Plugin(id = "synergy", name = "Synergy", version = "0.0.1-SNAPSHOT",
url = "archi.quest", description = "Basic tools and messaging plugin", authors = {"mrbear22"})
public class BrainVelocity {

    private ProxyServer server;
    private Logger logger;
	public JDA jda;
    public Map<String, Object> configValues;
	public Object config;
    
    public static final String CHANNEL_NAME = "net:synergy";
    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(CHANNEL_NAME);

    @Inject
    public BrainVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.info("Synergy is ready to be helpful for all beadmakers!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(IDENTIFIER);
        
        new Config(this).register();
        new Discord(this).initialize();
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

	public Config getConfig() {
		return new Config(this);
	}
    
}
