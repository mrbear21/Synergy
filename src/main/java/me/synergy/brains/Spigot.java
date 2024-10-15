package me.synergy.brains;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.synergy.commands.DiscordCommand;
import me.synergy.commands.LanguageCommand;
import me.synergy.commands.PronounCommand;
import me.synergy.commands.SynergyCommand;
import me.synergy.commands.ThemeCommand;
import me.synergy.commands.VoteCommand;
import me.synergy.discord.Discord;
import me.synergy.events.SynergyEvent;
import me.synergy.handlers.LocalesListener;
import me.synergy.handlers.ResourcePackHandler;
import me.synergy.handlers.ServerListPingListener;
import me.synergy.handlers.SpigotPlayerListener;
import me.synergy.handlers.VoteListener;
import me.synergy.integrations.EssentialsAPI;
import me.synergy.integrations.PlaceholdersAPI;
import me.synergy.integrations.PlanAPI;
import me.synergy.integrations.VaultAPI;
import me.synergy.modules.ChatManager;
import me.synergy.modules.Config;
import me.synergy.modules.DataManager;
import me.synergy.modules.LocalesManager;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.ToastMessage;
import me.synergy.web.WebServer;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Spigot extends JavaPlugin implements PluginMessageListener, Listener {

    private static Spigot INSTANCE;
    private ProtocolManager PROTOCOLMANAGER;
    private static Economy econ;
    private static Permission perms;
    private static Chat chat;

    @Override
	public void onEnable() {
        INSTANCE = this;
        Synergy.platform = "spigot";

        getServer().getMessenger().registerOutgoingPluginChannel(this, "net:synergy");
        getServer().getMessenger().registerIncomingPluginChannel(this, "net:synergy", this);

        new Config().initialize();
        new DataManager().initialize();
        new LocalesManager().initialize();
        new SynergyCommand().initialize();
        new VoteCommand().initialize();
        new PronounCommand().initialize();
        new LanguageCommand().initialize();
        new DiscordCommand().initialize();
        new ChatManager().initialize();
        new Discord().initialize();
        new ServerListPingListener().initialize();
        new SpigotPlayerListener().initialize();
        new WebServer().initialize();
        new ResourcePackHandler().initialize();
        new ThemeCommand().initialize();

		if (Synergy.isDependencyAvailable("ProtocolLib")) {
			PROTOCOLMANAGER = ProtocolLibrary.getProtocolManager();
	        new LocalesListener().initialize();
		}
        
		if (Synergy.isDependencyAvailable("Votifier")) {
			new VoteListener().initialize();
		}
        
		if (Synergy.isDependencyAvailable("Essentials")) {
			new EssentialsAPI().initialize();
		}
        
		if (Synergy.isDependencyAvailable("Vault")) {
			new VaultAPI().initialize();
	        setupEconomy();
	        setupPermissions();
	        //setupChat();
		}

		if (Synergy.isDependencyAvailable("Plan")) {
			new PlanAPI().initialize();
		}
        
		if (Synergy.isDependencyAvailable("PlaceholderAPI")) {
	        new PlaceholdersAPI().initialize();
		}
		
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("Synergy is ready to be helpful for the all BreadMakers!");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player p, byte[] message) {
        if (!channel.equals("net:synergy")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String token = in.readUTF();
	    String identifier = in.readUTF();
	    String stringUUID = in.readUTF();
	    
	    UUID uuid = null;
	    if (stringUUID != null && !stringUUID.isEmpty()) {
	        try {
	            uuid = UUID.fromString(stringUUID);
	        } catch (IllegalArgumentException e) { }
	    }
	    
	    String data = in.readUTF();
        if (token.equals(Synergy.getSynergyToken())) {
        	new SynergyEvent(identifier, uuid, data).fireEvent();
        	Bukkit.getServer().getPluginManager().callEvent(new me.synergy.bukkit.events.SynergyEvent(identifier, uuid, data));
        }
    }

    public void sendPluginMessage(byte[] data) {
    //	Bukkit.getServer().sendPluginMessage(this, "net:synergy", data);
    	Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
    	if (player != null) {
    		player.sendPluginMessage(this, "net:synergy", data);
    	}
    }
    
   
    @Override
	public void onDisable() {
        new Discord().shutdown();
        new WebServer().shutdown();
        getLogger().info("Synergy has stopped it's service!");
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
        RegisteredServiceProvider <Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
			return false;
		}
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

    @SuppressWarnings("unused")
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
		 Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
	}

	public Player getPlayerByUniqueId(UUID uniqueId) {
		return Bukkit.getPlayer(uniqueId);
	}

	public OfflinePlayer getOfflinePlayerByUniqueId(UUID uniqueId) {
		return Bukkit.getOfflinePlayer(uniqueId);
	}
	
	public boolean playerHasPermission(UUID uniqueId, String node) {
		return getPlayerByUniqueId(uniqueId) == null ? false : getPlayerByUniqueId(uniqueId).hasPermission(node);
	}

	public void dispatchCommand(String string) {
	    Bukkit.getScheduler().runTask(this, new Runnable() {
	        @Override
	        public void run() {
	            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), string);
	        }
	    });
	}

	public void executeInteractive(String json, BreadMaker bread) {
    	try {
	        Gson gson = new Gson();
	        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
	        if (jsonObject.has("extra")) {
	            JsonArray extraArray = jsonObject.getAsJsonArray("extra");
	            for (JsonElement element : extraArray) {
	                JsonObject extraObject = element.getAsJsonObject();
	                
	                if (extraObject.has("soundEvent")) {
	                    JsonObject soundEvent = extraObject.getAsJsonObject("soundEvent");
	                    if (soundEvent.has("sound")) {
	                    	Synergy.getSpigot().getPlayerByUniqueId(bread.getUniqueId()).playSound(Bukkit.getPlayer(bread.getUniqueId()), Sound.valueOf(soundEvent.get("sound").getAsString().toUpperCase()), 1,1);
	                    }
	                }
	                
	                if (extraObject.has("titleEvent")) {
	                    JsonObject titleEvent = extraObject.getAsJsonObject("titleEvent");

	                    Bukkit.getPlayer(bread.getUniqueId()).sendTitle(
                			titleEvent.has("title") ? Synergy.translate(titleEvent.get("title").getAsString(), bread.getLanguage()).setPlaceholders(bread).getLegacyColored(bread.getTheme()) : "",
                			titleEvent.has("subtitle") ? Synergy.translate(titleEvent.get("subtitle").getAsString(), bread.getLanguage()).setPlaceholders(bread).getLegacyColored(bread.getTheme()) : "",
                			3,
                			titleEvent.get("duration").getAsInt(),
                			3
                    	);
	                    
	                }
	                
	                if (extraObject.has("toastEvent")) {
	                    JsonObject toastEvent = extraObject.getAsJsonObject("toastEvent");
	                    if (toastEvent.has("text")) {
	                    	ToastMessage.displayTo(org.bukkit.Bukkit.getPlayer(bread.getUniqueId()), "sunflower", toastEvent.get("text").getAsString(), ToastMessage.Style.GOAL);
	                    }
	                }
	                
	            }
	        }
    	} catch (Exception c) {
    		//Synergy.getLogger().error("Error while executing interactive: " + c.getLocalizedMessage());
    	}
    }
	
    public void startSpigotMonitor() {
        new BukkitRunnable() {
            @Override
            public void run() {
            	new WebServer().monitorServer();
            }
        }.runTaskTimerAsynchronously(this, 0L, WebServer.MONITOR_INTERVAL_SECONDS * 20L);
    }

}