package me.synergy.brains;

import java.util.UUID;

import me.synergy.events.SynergyEvent;
import me.synergy.modules.ChatManager;
import me.synergy.modules.Config;
import me.synergy.modules.DataManager;
import me.synergy.modules.Discord;
import me.synergy.modules.LocalesManager;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.Logger;
import me.synergy.utils.Utils;

public class Synergy {
    public static String platform;

    public static String getSynergyToken() {
        return getConfig().getString("synergy-plugin-messaging.token");
    }

    public static Spigot getSpigot() {
        return Spigot.getInstance();
    }

    public static Velocity getVelocity() {
        return Velocity.getInstance();
    }

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
        return isSpigot() ? getLocalesManager().translateString(string, getLocalesManager().getDefaultLanguage()) : string;
    }
    
    public static String translateStringColorStripped(String string) {
    	return isSpigot() ? getLocalesManager().translateStringColorStripped(string, getLocalesManager().getDefaultLanguage()) : string;
    }

    public static String getServerName() {
        return getConfig().getString("synergy-plugin-messaging.servername");
    }

    public static SynergyEvent createSynergyEvent(String identifier) {
        return new SynergyEvent(identifier);
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

    public static LocalesManager getLocalesManager() {
        return new LocalesManager();
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
        return isSpigot() ? getSpigot().getServer().getPluginManager().isPluginEnabled(plugin) : false;
    }

	public static DataManager getDataManager() {
		return new DataManager();
	}

	public static UUID getUniqueIdFromName(String username) {
		return isSpigot() ? getSpigot().getUniqueIdFromName(username) : null;
	}

    public static void debug(String string) {
        getLogger().info(string, true);
    }

	public static void executeConsoleCommand(String command) {
		if (isSpigot()) {
			getSpigot().executeConsoleCommand(command);
		}
	}
	
}