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
		if (Synergy.isRunningSpigot() && isOnline()) {
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
		return Synergy.isRunningSpigot() ? EssentialsAPI.essentialsIsPlayerMuted(getName()) : false;
	}

	public String getName() {
		if (Synergy.isRunningSpigot()) {
			return Synergy.getSpigot().getPlayerName(getUniqueId());
		}
		if (Synergy.isRunningBungee()) {
			return Synergy.getBungee().getPlayerName(getUniqueId());
		}
		return null;
	}

	public DataObject getData(String option) {
		return new DataObject(Synergy.getDataManager().get("players."+getUniqueId()+"."+option));
	}

	public void setData(String option, String value) {
		 Synergy.getDataManager().set("players."+getUniqueId()+"."+option, value);
	}
	
	public boolean isOnline() {
		if (Synergy.isRunningSpigot()) {
			return getUniqueId() == null ? false : Synergy.getSpigot().getPlayerByUniqueId(getUniqueId()) != null ? Synergy.getSpigot().getPlayerByUniqueId(getUniqueId()).isOnline() : false;
		}
		return false;
	}

    public boolean isAuthenticated() {
		if (Synergy.isRunningSpigot() && Synergy.isDependencyAvailable("Authme")) {
			return AuthmeAPI.isAuthenticated(Synergy.getSpigot().getPlayerByUniqueId(uuid));
		}
		return true;
    }

	public boolean hasPermission(String node) {
		if (Synergy.isRunningSpigot()) {
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
