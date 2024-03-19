package me.synergy.brains;

import java.util.Set;
import java.util.UUID;

import me.synergy.events.SynergyEvent;
//import me.synergy.events.SynergyVelocityEvent;
import me.synergy.modules.ChatManager;
import me.synergy.modules.Config;
import me.synergy.modules.DataManager;
import me.synergy.modules.Discord;
import me.synergy.modules.Localizations;
import me.synergy.modules.VaultAPI;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.Logger;
import me.synergy.utils.Utils;

public class Synergy {
    public static String platform;

    public static String getSynergyToken() {
        return getConfig().getString("synergy-plugin-messaging.token");
    }

    public static Spigot getSpigotInstance() {
        return Spigot.getInstance();
    }

    //public static Velocity getVelocityInstance() {
    //    return Velocity.getInstance();
    //}

    public static boolean isSpigot() {
        try {
            Class.forName("org.bukkit.Bukkit");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Boolean isRunningVelocity() {
        return Boolean.valueOf(platform.equals("velocity"));
    }

    public static String translateString(String string) {
        return isSpigot() ? getLocalizations().translateString(string, getDefaultLanguage()) : string;
    }
    
    public static String translateStringColorStripped(String string) {
    	return isSpigot() ? getLocalizations().translateStringColorStripped(string, getDefaultLanguage()) : string;
    }

	public static String translateString(String string, UUID uuid) {
		return isSpigot() ? getLocalizations().translateString(string, new BreadMaker(uuid).getLanguage()) : string;
	}
    
    public static String getDefaultLanguage() {
        return getConfig().getString("localizations.default-language");
    }

    public static String getServerName() {
        return getConfig().getString("synergy-plugin-messaging.servername");
    }

    public static SynergyEvent createSynergyEvent(String identifier) {
        return new SynergyEvent(identifier);
    }

    public static void sendMessage(UUID player, String message) {
        createSynergyEvent("system-chat").setUniqueId(player).setArgument(message).send();
    }
    
    //public static SynergyVelocityEvent createSynergyVelocityEvent(String identifier) {
    //    return new SynergyVelocityEvent(identifier);
    //}

    public static Discord getDiscord() {
        return new Discord();
    }

    public static ChatManager getChatManager() {
        return new ChatManager();
    }

    public static Localizations getLocalizations() {
        return new Localizations();
    }

    public static SynergyEvent buildSynergyMessage(String identifier) {
        return new SynergyEvent(identifier);
    }

    public static Config getConfig() {
        return new Config();
    }

    public static Utils getUtils() {
        return new Utils();
    }

    public static Logger getLogger() {
        return new Logger();
    }

    public static BreadMaker getBread(UUID uuid) {
        return new BreadMaker(uuid);
    }

    public static boolean isDependencyAvailable(String plugin) {
        return isSpigot() ? getSpigotInstance().getServer().getPluginManager().isPluginEnabled(plugin) : false;
    }

    public static void debug(String string) {
        getLogger().info(string, true);
    }

    public static VaultAPI getVault() {
        return new VaultAPI();
    }

	public static DataManager getDataManager() {
		return new DataManager();
	}

	public static Set<String> getLanguages() {
		return getSpigotInstance().getLocales().keySet();
	}

	
}