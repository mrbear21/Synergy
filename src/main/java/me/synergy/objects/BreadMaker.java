package me.synergy.objects;

import me.synergy.brains.Synergy;

public class BreadMaker {

	private String name;
	
	public BreadMaker(String name) {
		this.setName(name);
	}

	public String getLanguage() {
		if (Synergy.getDataManager().getConfig().isSet("players."+getName()+".language")) {
			return Synergy.getDataManager().getConfig().getString("players."+getName()+".language");
		}
		return Synergy.getConfig().getString("localizations.default-language", "en");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
