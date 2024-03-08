package me.synergy.brains;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.HttpServer;

import me.synergy.commands.SynergyCommand;
import me.synergy.commands.VoteCommand;
import me.synergy.events.SynergyPluginEvent;
import me.synergy.handlers.VoteListener;
import me.synergy.objects.BreadMaker;
import net.dv8tion.jda.api.JDA;
import me.synergy.modules.ChatManager;
import me.synergy.modules.Discord;
import me.synergy.modules.Localizations;
import me.synergy.modules.SynergyConfig;
import me.synergy.modules.WebServer;

public class Spigot extends JavaPlugin implements PluginMessageListener {
	
	private static Spigot INSTANCE;
	private FileConfiguration LOCALESFILE;
    private Map<String, HashMap<String, String>> LOCALES;
	private ProtocolManager PROTOCOLMANAGER;
	public JDA jda;
	public HttpServer WEBSERVER;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		Synergy.platform = "spigot";

		getServer().getMessenger().registerOutgoingPluginChannel(this, "net:synergy");
		getServer().getMessenger().registerIncomingPluginChannel(this, "net:synergy", this);
		
		PROTOCOLMANAGER = ProtocolLibrary.getProtocolManager();
		LOCALESFILE = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "locales.yml"));
		LOCALES = new HashMap<String, HashMap<String, String>>();
		
		new SynergyConfig(this).initialize();
		new SynergyCommand(this).initialize();
		new VoteCommand(this).initialize();
		new Localizations(this).initialize();
		new ChatManager(this).initialize();
        new Discord(this).initialize();
        new WebServer(this).start();
        new VoteListener(this).initialize();
        new SynergyPluginEvent().initialize();

		getLogger().info("Synergy is ready to be helpful for the all BreadMakers!");
		
	}

    public ProtocolManager getProtocolManager() {
        return PROTOCOLMANAGER;
    }
	
    @Override
    public void onPluginMessageReceived(String channel, Player p, byte[] message) {
        if (!channel.equals("net:synergy")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String token = in.readUTF();
        String identifier = in.readUTF();
        String player = in.readUTF();
        List<String> argsList = new ArrayList<>();
        try {
            while (true) {
                argsList.add(in.readUTF());
            }
        } catch (Exception ignored) {}
        String[] args = argsList.toArray(new String[0]);
        
        if (token.equals(Synergy.getSynergyToken())) {
        	Bukkit.getServer().getPluginManager().callEvent(new SynergyPluginEvent(identifier, player, args));
        }
    }

	public void onDisable() {
		new WebServer(this).stop();
		getJda().shutdown();
		getLogger().info("Synergy has stopped it's service!");
	}
	
	public void log(String string) {
		log(string, true);
	}
	
	public void log(String string, boolean broadcast) {
		getLogger().info(Synergy.translateString(string));
		if (broadcast) {
			for (Player p : Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("synergy.sudo")).collect(Collectors.toList())) {
				p.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+"[Console] "+string);
			}
		}
	}
	
	public Map<String, HashMap<String, String>> getLocales() {
		return LOCALES;
	}
	
	public FileConfiguration getLocalesFile() {
		return LOCALESFILE;
	}
	
	public static Spigot getInstance() {
		return INSTANCE;
	}

	public BreadMaker getBread(String name) {
		return new BreadMaker(name);
	}

	public JDA getJda() {
		return jda;
	}

	public HttpServer getWeb() {
		return WEBSERVER;
	}

	public boolean isDependencyAvailable(String string) {
		return this.getServer().getPluginManager().isPluginEnabled(string);
	}
}


