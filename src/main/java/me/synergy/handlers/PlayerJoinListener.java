package me.synergy.handlers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.synergy.brains.Synergy;
import me.synergy.discord.Discord;
import me.synergy.discord.RolesDiscordListener;

public class PlayerJoinListener implements Listener {

	private static HashMap<UUID, Long> PLAYERS = new HashMap<>();

    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());
        Synergy.getEventManager().registerEvents(this);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
    	if (PLAYERS.containsKey(event.getPlayer().getUniqueId()) && System.currentTimeMillis() - PLAYERS.get(event.getPlayer().getUniqueId()) < 1000) {
    		event.getPlayer().performCommand("spawn");
        	Synergy.getLogger().discord("```Player "+event.getPlayer().getName()+" has been teleported to spawn ```");
    	}
    	Synergy.getLogger().discord("```Player "+event.getPlayer().getName()+" has left ```");
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
    	Synergy.getLogger().discord("```Player "+event.getPlayer().getName()+" has been kicked with the reason: "+event.getReason()+"```");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (Discord.getDiscordIdByUniqueId(uuid) != null) {
        	RolesDiscordListener.addVerifiedRole(Discord.getDiscordIdByUniqueId(uuid));

            if (Synergy.getConfig().getBoolean("discord.synchronization.sync-roles-form-mc-to-discord")) {
                Synergy.createSynergyEvent("sync-roles-from-mc-to-discord").setPlayerUniqueId(event.getPlayer().getUniqueId()).setOption("group", Synergy.getSpigot().getPermissions().getPrimaryGroup(event.getPlayer())).send();
            }

            if (Synergy.getConfig().getBoolean("discord.synchronization.sync-roles-from-discord-to-mc")) {
                Synergy.createSynergyEvent("sync-roles-from-discord-to-mc").setPlayerUniqueId(event.getPlayer().getUniqueId()).send();
            }
        }

    	PLAYERS.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());

    	Synergy.getLogger().discord("```Player "+event.getPlayer().getName()+" has joined with IP "+event.getPlayer().getAddress()+" ```");

    }
}