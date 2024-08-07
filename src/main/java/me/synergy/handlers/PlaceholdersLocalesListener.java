package me.synergy.handlers;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.synergy.brains.Synergy;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.Color;
import me.synergy.utils.Interactive;
import me.synergy.utils.Translation;
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
		return "translation";
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
	public String onPlaceholderRequest(Player player, String identifier) {

		BreadMaker bread = Synergy.getBread(player.getUniqueId());

    	String output = Translation.translate("<lang>"+identifier+"</lang>", bread.getLanguage());
    	output = Interactive.removeInteractiveTags(output);
    	output = Color.processThemeTags(output, bread.getTheme());

		return output;
	}

}