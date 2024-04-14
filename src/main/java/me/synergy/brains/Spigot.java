package me.synergy.brains;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import me.synergy.commands.LanguageCommand;
import me.synergy.commands.SynergyCommand;
import me.synergy.commands.VoteCommand;
import me.synergy.events.SynergyEvent;
import me.synergy.handlers.LocalesListener;
import me.synergy.handlers.MOTDListener;
import me.synergy.handlers.PlaceholdersBreadDataListener;
import me.synergy.handlers.PlaceholdersLocalesListener;
import me.synergy.handlers.PlayerJoinListener;
import me.synergy.handlers.ResourcePackHandler;
import me.synergy.handlers.VoteListener;
import me.synergy.modules.ChatManager;
import me.synergy.modules.Config;
import me.synergy.modules.DataManager;
import me.synergy.modules.Discord;
import me.synergy.modules.LocalesManager;
import me.synergy.modules.WebServer;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Spigot extends JavaPlugin implements PluginMessageListener {
	
    private static Spigot INSTANCE;
    private FileConfiguration LOCALESFILE;
    private FileConfiguration DATAFILE;
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

        new Config().initialize();
        new SynergyCommand().initialize();
        new VoteCommand().initialize();
        new LocalesManager().initialize();
        new LocalesListener().initialize();
        new ChatManager().initialize();
        new Discord().initialize();
        new VoteListener().initialize();
        new SynergyEvent().initialize();
        new MOTDListener().initialize();
        new DataManager().initialize();
        new PlayerJoinListener().initialize();
        new LanguageCommand().initialize();
        new WebServer().initialize();
        new ResourcePackHandler().initialize();
        
        setupEconomy();
        setupPermissions();
        setupChat();
        
		if (Synergy.isDependencyAvailable("PlaceholderAPI")) {
			new PlaceholdersLocalesListener().register();
			new PlaceholdersBreadDataListener().register();
		}
        
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
        UUID uuid = UUID.nameUUIDFromBytes(in.readUTF().getBytes());
        @SuppressWarnings("unused")
		String waitForPlayer = in.readUTF();
		String options = in.readUTF();
        
        if (token.equals(Synergy.getSynergyToken())) {
        	Bukkit.getServer().getPluginManager().callEvent(new SynergyEvent(identifier, uuid, options));
        }
    }

    public void onDisable() {
        Synergy.getDiscord().shutdown();
        new WebServer().shutdown();
        getLogger().info("Synergy has stopped it's service!");
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

	public String getPlayerName(UUID uniqueId) {
		return uniqueId == null ? null : Bukkit.getOfflinePlayer(uniqueId) == null ? null : Bukkit.getOfflinePlayer(uniqueId).getName();
	}

	@SuppressWarnings("deprecation")
	public UUID getUniqueIdFromName(String username) {
		return username == null ? null : Bukkit.getOfflinePlayer(username) == null ? null : Bukkit.getOfflinePlayer(username).getUniqueId();
	}
	
	public String getPlayerLanguage(UUID uniqueId) {
		Player player = Bukkit.getPlayer(uniqueId);
		String language = player.getLocale().split("_")[0];
		return Synergy.getLocalesManager().getLanguages().contains(language) ? language : "en";
	}

	public void executeConsoleCommand(String command) {
		 Bukkit.dispatchCommand((CommandSender) Bukkit.getServer().getConsoleSender(), command);
	}

	public Player getPlayerByUniqueId(UUID uniqueId) {
		return Bukkit.getPlayer(uniqueId);
	}

	public boolean playerHasPermission(UUID uniqueId, String node) {
		return getPlayerByUniqueId(uniqueId) == null ? false : getPlayerByUniqueId(uniqueId).hasPermission(node);
	}
	
}