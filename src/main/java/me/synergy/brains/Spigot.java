package me.synergy.brains;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.synergy.commands.SynergyCommand;
import me.synergy.commands.VoteCommand;
import me.synergy.events.SynergyEvent;
import me.synergy.handlers.VoteListener;
import me.synergy.modules.ChatManager;
import me.synergy.modules.Config;
import me.synergy.modules.Discord;
import me.synergy.modules.Localizations;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class Spigot extends JavaPlugin implements PluginMessageListener {
	
    private static Spigot INSTANCE;
    private FileConfiguration LOCALESFILE;
    private Map<String, HashMap<String, String>> LOCALES;
    private ProtocolManager PROTOCOLMANAGER;

    public void onEnable() {
        INSTANCE = this;
        Synergy.platform = "spigot";

        getServer().getMessenger().registerOutgoingPluginChannel((Plugin) this, "net:synergy");
        getServer().getMessenger().registerIncomingPluginChannel((Plugin) this, "net:synergy", this);

        PROTOCOLMANAGER = ProtocolLibrary.getProtocolManager();
        LOCALESFILE = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "locales.yml"));
        LOCALES = new HashMap<String, HashMap<String, String>>();

        new Config().initialize();
        new SynergyCommand().initialize();
        new VoteCommand().initialize();
        new Localizations().initialize();
        new ChatManager().initialize();
        new Discord().initialize();
        new VoteListener().initialize();
        new SynergyEvent().initialize();
        
        getLogger().info("Synergy is ready to be helpful for the all BreadMakers!");
    }

    public ProtocolManager getProtocolManager() {
        return this.PROTOCOLMANAGER;
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
        @SuppressWarnings("unused")
		String waitForPlayer = in.readUTF();
        List<String> argsList = new ArrayList<>();
        try { while (true) {
                argsList.add(in.readUTF());
            }
        } catch (Exception ignored) {}
        String[] args = argsList.toArray(new String[0]);
        
        if (token.equals(Synergy.getSynergyToken())) {
        	Bukkit.getServer().getPluginManager().callEvent(new SynergyEvent(identifier, player, args));
        }
    }

    public void onDisable() {
        Synergy.getDiscord().shutdown();
        getLogger().info("Synergy has stopped it's service!");
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

}