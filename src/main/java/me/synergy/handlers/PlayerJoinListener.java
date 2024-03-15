package me.synergy.handlers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;

public class PlayerJoinListener implements Listener {
	
	public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigotInstance());
	}
	
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String player = event.getPlayer().getName();
        if (Synergy.getDataManager().getConfig().get("synergy-event-waiting." + player) != null) {
            ConfigurationSection identifiers = Synergy.getDataManager().getConfig().getConfigurationSection("synergy-event-waiting." + player);
            if (identifiers != null) {
                for (String identifier: identifiers.getKeys(false)) {
                    ConfigurationSection objs = Synergy.getDataManager().getConfig().getConfigurationSection("synergy-event-waiting." + player + "." + identifier);
                    if (objs != null) {
                        for (String obj: objs.getKeys(false)) {
                            if (Synergy.getDataManager().getConfig().isSet("synergy-event-waiting." + player + "." + identifier + "." + obj)) {
                            	Synergy.debug("synergy-event-waiting." + player + "." + identifier + "." + obj + " => " +Synergy.getDataManager().getConfig().getStringList("synergy-event-waiting." + player + "." + identifier + "." + obj));
                            	new SynergyEvent().triggerEvent(identifier, player, Synergy.getDataManager().getConfig().getStringList("synergy-event-waiting." + player + "." + identifier + "." + obj).toArray(new String[0]));
                            }
                        }
                    }
                }
            }
        }
        Synergy.getSpigotInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Synergy.getSpigotInstance(), new Runnable() {
            public void run() {
                Synergy.getDataManager().getConfig().set("synergy-event-waiting." + player, null);
                Synergy.getDataManager().saveConfig();
            }
        }, 0, 60);

        if (Synergy.getConfig().getBoolean("discord.synchronization.sync-roles-form-mc-to-discord")) { 
        	Synergy.createSynergyEvent("sync-roles-from-mc-to-discord").setPlayer(event.getPlayer().getName()).setArgument(Synergy.getSpigotInstance().getPermissions().getPrimaryGroup(event.getPlayer())).send();
        }
        
        if (Synergy.getConfig().getBoolean("discord.synchronization.sync-roles-from-discord-to-mc")) { 
        	Synergy.createSynergyEvent("sync-roles-from-discord-to-mc").setPlayer(event.getPlayer().getName()).send();
        }
    }
}
