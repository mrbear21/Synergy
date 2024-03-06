package me.synergy.objects;

import me.synergy.brains.Synergy;

public class BreadMaker {

	private String name;
	
	public BreadMaker(String name) {
		this.setName(name);
	}

	public String getLanguage() {
		return Synergy.getConfig().getString("localizations.default-language", "en");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
