package me.synergy.brains;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

import me.synergy.discord.Discord;
import me.synergy.events.SynergyEvent;
import me.synergy.events.SynergyEventManager;
import me.synergy.modules.Config;
import me.synergy.modules.DataManager;
import me.synergy.modules.LocalesManager;
import me.synergy.objects.BreadMaker;
import me.synergy.objects.Locale;
import me.synergy.utils.Cooldown;
import me.synergy.utils.Logger;
import me.synergy.utils.Translation;
import net.dv8tion.jda.api.JDA;

public class Synergy {
    public static String platform;

    public static String getSynergyToken() {
        return getConfig().getString("synergy-plugin-messaging.token");
    }

    public static Spigot getSpigot() {
        return Spigot.getInstance();
    }

    public static Bungee getBungee() {
        return Bungee.getInstance();
	}
    
    public static Velocity getVelocity() {
        return Velocity.getInstance();
    }

    public static Boolean isRunningSpigot() {
        return platform.equals("spigot");
    }

    public static Boolean isRunningVelocity() {
        return platform.equals("velocity");
    }
    
	public static boolean isRunningBungee() {
        return platform.equals("bungee");
	}

    public static String getServerName() {
        return getConfig().getString("synergy-plugin-messaging.servername");
    }

    public static SynergyEvent createSynergyEvent(String identifier) {
        return new SynergyEvent(identifier);
    }

    public static JDA getDiscord() {
        return new Discord().getJda();
    }

    public static Translation getTranslation() {
        return new Translation();
    }

    public static Cooldown getCooldown(UUID uuid) {
        return new Cooldown(uuid);
    }

    public static LocalesManager getLocalesManager() {
        return new LocalesManager();
    }
    public static Config getConfig() {
        return new Config();
    }

    public static Logger getLogger() {
        return new Logger();
    }

    public static BreadMaker getBread(UUID uuid) {
        return new BreadMaker(uuid);
    }

    public static boolean isDependencyAvailable(String plugin) {
        return isRunningSpigot() ? getSpigot().getServer().getPluginManager().isPluginEnabled(plugin) : false;
    }

	public static DataManager getDataManager() {
		return new DataManager();
	}

	public static UUID getUniqueIdFromName(String username) {
		if (isRunningBungee()) {
			return getBungee().getProxy().getPlayer(username).getUniqueId();
		}
		if (isRunningSpigot()) {
			return getSpigot().getUniqueIdFromName(username);
		}
		return null;
	}

	public static void debug(String string) {
        getLogger().info(string, true);
    }

    public static Locale translate(String string, String language) {
    	return new Locale(string, language);
    }

	public static File getDataFolder() {
		File dataFolder = new File("plugins/Synergy");
		return dataFolder;
	}

	public static SynergyEventManager getEventManager() {
		return SynergyEventManager.getInstance();
	}

	public static void dispatchCommand(String command) {
		if (isRunningSpigot()) {
			getSpigot().dispatchCommand(command);
		}
	}
	
	public static void broadcastMessage(String string, BreadMaker bread) {
    	createSynergyEvent("broadcast").setPlayerUniqueId(bread.getUniqueId()).setOption("message", string).send();
	}

	public static UUID findUserUUID(String option, String value) {
		try {
			return getDataManager().findUserUUID(option, value);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}