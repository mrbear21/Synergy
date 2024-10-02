package me.synergy.integrations;

import org.bukkit.entity.Player;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

public class SaberFactionsAPI {

	public static Faction getFactionByPlayer(Player player) {
	    return Factions.getInstance().getAllFactions().stream()
	            .filter(faction -> faction.getOnlinePlayers().contains(player))
	            .findFirst().orElse(null);
	}

}
