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

import me.synergy.commands.LanguageCommand;
import me.synergy.commands.SynergyCommand;
import me.synergy.commands.VoteCommand;
import me.synergy.events.SynergyEvent;
import me.synergy.handlers.MOTDListener;
import me.synergy.handlers.PlayerJoinListener;
import me.synergy.handlers.VoteListener;
import me.synergy.modules.ChatManager;
import me.synergy.modules.Config;
import me.synergy.modules.DataManager;
import me.synergy.modules.Discord;
import me.synergy.modules.Localizations;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class Spigot extends JavaPlugin implements PluginMessageListener {
	
    private static Spigot INSTANCE;
    private FileConfiguration LOCALESFILE;
    private FileConfiguration DATAFILE;
    private Map<String, HashMap<String, String>> LOCALES;
    private ProtocolManager PROTOCOLMANAGER;
    private static Economy econ;
    private static Permission perms;
    private static Chat chat;
    
    
    public void onEnable() {
        INSTANCE = this;
        Synergy.platform = "spigot";

        getServer().getMessenger().registerOutgoingPluginChannel((Plugin) this, "net:synergy");
        getServer().getMessenger().registerIncomingPluginChannel((Plugin) this, "net:synergy", this);

        PROTOCOLMANAGER = ProtocolLibrary.getProtocolManager();
        DATAFILE = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "data.yml"));
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
        new MOTDListener().initialize();
        new DataManager().initialize();
        new PlayerJoinListener().initialize();
        new LanguageCommand().initialize();
        
        setupEconomy();
        setupPermissions();
        setupChat();
        
        getLogger().info("Synergy is ready to be helpful for the all BreadMakers!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider <Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        setEconomy(rsp.getProvider());
        return (getEconomy() != null);
    }

    private boolean setupPermissions() {
        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider <Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            setPermissions(rsp.getProvider());
            return (getPermissions() != null);
        }
        return false;
    }

    private boolean setupChat() {
        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider <Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
            setChat(rsp.getProvider());
            return (getChat() != null);
        }
        return false;
    }
    
    private void setChat(Chat chat) {
		Spigot.chat = chat;
	}
    
	public Chat getChat() {
        return chat;
    }
    
	public Economy getEconomy() {
        return econ;
    }

    public static void setEconomy(Economy econ) {
        Spigot.econ = econ;
    }
    
    public Permission getPermissions() {
        return perms;
    }
    
    public static void setPermissions(Permission perms) {
        Spigot.perms = perms;
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
	
	public FileConfiguration getDataFile() {
		return DATAFILE;
	}
	
	public static Spigot getInstance() {
		return INSTANCE;
	}

}