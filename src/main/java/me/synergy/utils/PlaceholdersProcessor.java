package me.synergy.utils;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.synergy.brains.Synergy;

public class PlaceholdersProcessor {

    public static String processPlaceholders(Player player, String string) {
    	if (Synergy.isDependencyAvailable("PlaceholderAPI")) {
    		return PlaceholderAPI.setPlaceholders(player, string);
    	}
    	return string;
    }
    
}
