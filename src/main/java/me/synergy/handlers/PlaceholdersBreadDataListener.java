package me.synergy.handlers;

import java.util.HashMap;

import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.synergy.brains.Synergy;
import net.md_5.bungee.api.ChatColor;

public class PlaceholdersBreadDataListener extends PlaceholderExpansion {

	public PlaceholdersBreadDataListener() {
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getAuthor() {
		return "mrbear22";
	}


	@Override
	public String getIdentifier() {
		return "synergy";
	}

	@Override
	public String getPlugin() {
		return null;
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	public String setPlaceholders(Player player, String placeholder) {
		return ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, placeholder));
	}
	
	@Override
	public String onPlaceholderRequest(Player p, String identifier) {
		HashMap<String, String> locales = Synergy.getLocalesManager().getLocales().get(Synergy.getBread(p.getUniqueId()).getLanguage());
		if (locales != null && locales.containsKey(identifier)) {
			return locales.get(identifier);
		}
		return identifier;
	}

}