package me.synergy.objects;

import java.util.UUID;

import me.synergy.brains.Synergy;
import me.synergy.integrations.AuthmeAPI;
import me.synergy.integrations.EssentialsAPI;

public class BreadMaker {

	private UUID uuid;
	
	public BreadMaker(UUID uuid) {
		this.uuid = uuid;
	}

	public String getLanguage() {
		if (getData("language").isSet()) {
			return getData("language").getAsString();
		}
		if (Synergy.isSpigot() && isOnline()) {
			return Synergy.getSpigot().getPlayerLanguage(getUniqueId());
		}
		return Synergy.getConfig().getString("localizations.default-language", "en");
	}

	public void sendMessage(String message) {
		Synergy.createSynergyEvent("system-chat").setPlayerUniqueId(uuid).setOption("message", message).send();
	}
	
	public UUID getUniqueId() {
		return uuid;
	}
	
	public boolean isMuted() {
		return EssentialsAPI.essentialsIsPlayerMuted(getName());
	}
	
	public String getName() {
		return Synergy.isSpigot() ? Synergy.getSpigot().getPlayerName(getUniqueId()) : null;
	}
	
	public DataObject getData(String option) {
		return Synergy.getDataManager().getData("players."+"."+getUniqueId()+"."+option);
	}

	public void setData(String option, String value) {
		 Synergy.getDataManager().setData("players."+"."+getUniqueId()+"."+option, value);
	}
	
	public boolean isOnline() {
		if (Synergy.isSpigot()) {
			return getUniqueId() == null ? false : Synergy.getSpigot().getPlayerByUniqueId(getUniqueId()) != null ? Synergy.getSpigot().getPlayerByUniqueId(getUniqueId()).isOnline() : false;
		}
		return false;
	}

    public boolean isAuthenticated() {
		if (Synergy.isSpigot() && Synergy.isDependencyAvailable("Authme")) {
			return AuthmeAPI.isAuthenticated(Synergy.getSpigot().getPlayerByUniqueId(uuid));
		}
		return true;
    }
	
	public boolean hasPermission(String node) {
		if (Synergy.isSpigot()) {
			return Synergy.getSpigot().playerHasPermission(getUniqueId(), node);
		}
		return false;
	}

	public String getTheme() {
		if (getData("theme").isSet()) {
			return getData("theme").getAsString();
		}
		return "default";
	}
}
