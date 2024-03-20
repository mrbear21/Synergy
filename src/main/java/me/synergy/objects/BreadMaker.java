package me.synergy.objects;

import java.util.UUID;

import me.synergy.brains.Synergy;

public class BreadMaker {

	private UUID uuid;
	
	public BreadMaker(UUID uuid) {
		this.uuid = uuid;
	}

	public String getLanguage() {
		if (Synergy.getDataManager().getConfig().isSet("players."+getUniqueId()+".language")) {
			return Synergy.getDataManager().getConfig().getString("players."+getUniqueId()+".language");
		}
		if (Synergy.isSpigot()) {
			return Synergy.getSpigot().getPlayerLanguage(getUniqueId());
		}
		return Synergy.getConfig().getString("localizations.default-language", "en");
	}

	public void sendMessage(String message) {
		Synergy.createSynergyEvent("system-chat").setUniqueId(uuid).setOption("message", translateString(message)).send();
	}
	
	public String translateString(String string) {
		return Synergy.isSpigot() ? Synergy.getLocalesManager().translateString(string, getLanguage()) : string;
	}
	
	public UUID getUniqueId() {
		return uuid;
	}
	
	public String getName() {
		return Synergy.isSpigot() ? Synergy.getSpigot().getPlayerName(getUniqueId()) : null;
	}

}
