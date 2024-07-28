package me.synergy.handlers;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import me.synergy.brains.Synergy;
import me.synergy.modules.WebServer;

public class ResourcePackListener implements Listener {

    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Synergy.getBread(event.getPlayer().getUniqueId()).setData("resourcepack", null);
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            Synergy.getBread(event.getPlayer().getUniqueId()).setData("resourcepack", "yes");
            return;
        }
        Synergy.getBread(event.getPlayer().getUniqueId()).setData("resourcepack", "no");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (Synergy.getConfig().getBoolean("web-server.custom-texturepack")) {
        	String address = WebServer.getFullAddress()+"/resourcepack/"+Synergy.getConfig().getString("web-server.custom-texturepack-file");
        	event.getPlayer().setResourcePack(address);
        }
    }
}