package me.synergy.handlers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;

public class PlayerJoinListener implements Listener {

	private static HashMap<UUID, Long> PLAYERS = new HashMap<UUID, Long>();
	
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());
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
        if (Synergy.getDataManager().getConfig().get("synergy-event-waiting." + uuid) != null) {
            ConfigurationSection identifiers = Synergy.getDataManager().getConfig().getConfigurationSection("synergy-event-waiting." + uuid);
            if (identifiers != null) {
                for (String identifier: identifiers.getKeys(false)) {
                    ConfigurationSection objs = Synergy.getDataManager().getConfig().getConfigurationSection("synergy-event-waiting." + uuid + "." + identifier);
                    if (objs != null) {
                        for (String obj: objs.getKeys(false)) {
                            if (Synergy.getDataManager().getConfig().isSet("synergy-event-waiting." + uuid + "." + identifier + "." + obj)) {
                                new SynergyEvent(identifier, uuid, Synergy.getDataManager().getConfig().getString("synergy-event-waiting." + uuid + "." + identifier + "." + obj)).triggerEvent();
                            }
                        }
                    }
                }
            }
        }
        Synergy.getSpigot().getServer().getScheduler().scheduleSyncRepeatingTask(Synergy.getSpigot(), new Runnable() {
            public void run() {
                Synergy.getDataManager().getConfig().set("synergy-event-waiting." + uuid, null);
                Synergy.getDataManager().saveConfig();
            }
        }, 0, 60);

        if (Synergy.getDiscord().getDiscordIdByUniqueId(uuid) != null) {
        	
        	Synergy.getDiscord().addVerifiedRole(Synergy.getDiscord().getDiscordIdByUniqueId(uuid));
        	
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