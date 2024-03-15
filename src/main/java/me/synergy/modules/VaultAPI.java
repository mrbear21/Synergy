package me.synergy.modules;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import me.synergy.brains.Synergy;
import net.milkbowl.vault.permission.Permission;

public class VaultAPI {

    public List<OfflinePlayer> getPlayersWithPermission(String permissionNode) {
        List<OfflinePlayer> playersWithPermission = new ArrayList<>();
        if (Synergy.getSpigotInstance().getPermissions() != null) {
            for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) {
                if (Synergy.getSpigotInstance().getPermissions().playerHas(null, player, permissionNode)) {
                    playersWithPermission.add(player);
                }
            }
        }
        return playersWithPermission;
    }
    
    public OfflinePlayer getPlayerWithExactPermission(String permissionNode) {
    	Permission permission = Synergy.getSpigotInstance().getPermissions();
        if (permission != null) {
            for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) {
                if (permission.playerHas(null, player, permissionNode) || permission.playerHas(null, player, permissionNode + ".*")) {
                    if (permission.playerHas(null, player, permissionNode) && !permission.playerHas(null, player, permissionNode + ".*")) {
                        return player;
                    }
                }
            }
        }
        return null; // No player found with the exact permission
    }
	
}
