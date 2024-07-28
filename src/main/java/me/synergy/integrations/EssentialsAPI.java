package me.synergy.integrations;

import org.bukkit.Bukkit;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import me.synergy.brains.Synergy;

public class EssentialsAPI {

	public static boolean essentialsIsPlayerMuted(String player) {
		if (Synergy.isDependencyAvailable("Essentials")) {
			Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
	        User user = essentials.getUser(player);
	        if (user != null && user.isMuted()) {
	            return true;
	        }
		}
        return false;
	}

}
