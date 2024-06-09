package me.synergy.integrations;

import org.bukkit.entity.Player;

import fr.xephi.authme.api.v3.AuthMeApi;

public class AuthmeAPI {
	
    public static boolean isAuthenticated(Player name) {
        return AuthMeApi.getInstance().isAuthenticated(name);
    }
}
