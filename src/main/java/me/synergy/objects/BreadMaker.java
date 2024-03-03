package me.synergy.objects;

import me.synergy.brain.BrainSpigot;

public class BreadMaker {

	private BrainSpigot spigot;
	private String name;
	
	public BreadMaker(BrainSpigot spigot, String name) {
		this.spigot = spigot;
		this.setName(name);
	}

	public String getLanguage() {
		return spigot.getConfig().getString("localizations.default-language");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
