package me.synergy.handlers;

import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.synergy.brains.Synergy;
import me.synergy.objects.BreadMaker;
import net.md_5.bungee.api.ChatColor;

public class PlaceholdersLocalesListener extends PlaceholderExpansion {

	public PlaceholdersLocalesListener() {
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
		return "breadmaker";
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
		BreadMaker bread = Synergy.getBread(p.getUniqueId());
		return bread.getData(identifier).getAsString();
	}

}