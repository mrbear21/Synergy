package me.synergy.integrations;

import org.bukkit.Bukkit;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;

public class TownyAdvancedAPI {

	public String getSmt() {
		for (Town town : TownyAPI.getInstance().getTowns()) {
		}
		return null;
	}

}
