package me.synergy.integrations;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.synergy.brains.Synergy;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.Color;
import me.synergy.utils.Endings;
import me.synergy.utils.Interactive;
import me.synergy.utils.Translation;
import me.synergy.utils.Endings.Pronoun;
import net.md_5.bungee.api.ChatColor;

public class PlaceholdersAPI {

	public void initialize() {
		new LocalesListener().register();
		new BreadDataListener().register();
	}
	
    public static String processPlaceholders(Player player, String string) {
    	if (Synergy.isDependencyAvailable("PlaceholderAPI")) {
    		return PlaceholderAPI.setPlaceholders(player, string);
    	}
    	return string;
    }
    
    public class BreadDataListener extends PlaceholderExpansion {

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
    		return bread.getData(identifier).isSet() ? bread.getData(identifier).getAsString() : "none";
    	}

    }
    
	public class LocalesListener extends PlaceholderExpansion {

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
		    BreadMaker bread = (player == null) ? null : Synergy.getBread(player.getUniqueId());
		    String language = (bread == null) ? Translation.getDefaultLanguage() : bread.getLanguage();
		    String theme = (bread == null) ? Color.getDefaultTheme() : bread.getTheme();
		    Pronoun pronoun = (bread == null) ? Pronoun.HE : bread.getPronoun();
		    
		    String output = Translation.translate("<lang>" + identifier + "</lang>", language);
		    output = Endings.processEndings(output, pronoun);
		    output = Interactive.removeInteractiveTags(output);
		    output = Color.processThemeTags(output, theme);
		    
		    return output;
		}

	}
}
