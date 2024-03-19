package me.synergy.objects;

import java.util.UUID;

import org.bukkit.Bukkit;

import me.synergy.brains.Synergy;

public class BreadMaker {

	private UUID uuid;
	
	public BreadMaker(UUID uuid) {
		this.setName(uuid);
	}

	public String getLanguage() {
		if (Synergy.getDataManager().getConfig().isSet("players."+getUniqueId()+".language")) {
			return Synergy.getDataManager().getConfig().getString("players."+getUniqueId()+".language");
		}
		return Synergy.getConfig().getString("localizations.default-language", "en");
	}

	public void sendMessage(String message) {
		Synergy.sendMessage(getUniqueId(), message);
	}
	
	public UUID getUniqueId() {
		return uuid;
	}

	public void setName(UUID name) {
		this.uuid = name;
	}

	public String getName() {
		return Bukkit.getOfflinePlayer(getUniqueId()).getName();
	}

}
