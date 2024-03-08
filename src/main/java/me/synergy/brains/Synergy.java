package me.synergy.brains;

import me.synergy.modules.Localizations;
import me.synergy.modules.SynergyConfig;
import me.synergy.utils.Utils;
import me.synergy.events.SynergyPluginEvent;
import me.synergy.modules.ChatManager;
import me.synergy.modules.Discord;

public class Synergy {

	public static String platform; 
	
	public static String getSynergyToken() {
		return getConfig().getString("synergy-plugin-messaging.token");
	}
	
	public static Spigot getSpigotInstance() {
		return Spigot.getInstance();
	}
	
	public static Velocity getVelocityInstance() {
		return Velocity.getInstance();
	}
	
	public static Boolean isRunningSpigot() {
		if (platform.equals("spigot")) {
			return true;
		}
		return false;
	}
	
	public static Boolean isRunningVelocity() {
		if (platform.equals("velocity")) {
			return true;
		}
		return false;
	}
	
	public static String translateString(String string) {
		if (isRunningSpigot()) {
			return new Localizations(getSpigotInstance()).translateString(string, getConfig().getString("localizations.default-language"));
		}
		return string;
	}

	public static String getServerName() {
		return getConfig().getString("synergy-plugin-messaging.servername");
	}

	public static SynergyPluginEvent createSynergyEvent(String identifier) {
		return new SynergyPluginEvent(identifier);
	}
	
	public static Discord getDiscord() {
		return new Discord(getSpigotInstance());
	}
	
	public static ChatManager getChatManager() {
		return new ChatManager(getSpigotInstance());
	}
	
	public static Localizations getLocalizations() {
		return new Localizations(getSpigotInstance());
	}	
	
	public static SynergyPluginEvent buildSynergyMessage(String identifier) {
		return new SynergyPluginEvent(identifier);
	}	
	
	public static SynergyConfig getConfig() {
		if (isRunningSpigot()) {
			return new SynergyConfig(getSpigotInstance());
		}
		return new SynergyConfig(getVelocityInstance());
	}

	public static Utils getUtils() {
		return new Utils();
	}
	
	
}
