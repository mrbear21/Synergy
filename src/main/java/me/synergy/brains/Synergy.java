package me.synergy.brains;

import me.synergy.modules.Localizations;
import me.synergy.modules.SynergyConfig;

public class Synergy {

	public static String platform; 
	
	public static Spigot getSpigotInstance() {
		return Spigot.getInstance();
	}
	
	public static Velocity getVelocityInstance() {
		return Velocity.getInstance();
	}
	
	public static SynergyConfig getConfig() {
		if (getSpigotInstance() != null) {
			return new SynergyConfig(getSpigotInstance());
		}
		return new SynergyConfig(getVelocityInstance());
	}
	
	public static Boolean isRunningSpigot() {
		try {
			if (platform.equals("spigot")) {
				return true;
			}
		} catch (Exception ignore) {}
		return false;
	}
	
	public static Boolean isRunningVelocity() {
		try {
			if (platform.equals("velocity")) {
				return true;
			}
		} catch (Exception ignore) {}
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
	
}
