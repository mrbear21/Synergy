package me.synergy.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.synergy.brains.Spigot;
import me.synergy.events.SynergyPluginMessage;
import me.synergy.modules.Discord;
import me.synergy.modules.Localizations;
import me.synergy.modules.SynergyConfig;
import me.synergy.modules.WebServer;

public class SynergyCommand implements CommandExecutor, Listener {

	private Spigot spigot;
	
    public SynergyCommand(Spigot spigot) {
    	this.spigot = spigot;
	}

	public void initialize() {
		try {
			spigot.getCommand("synergy").setExecutor(new SynergyCommand(spigot));
			Bukkit.getPluginManager().registerEvents(new SynergyCommand(spigot), spigot);
			spigot.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
		} catch (Exception c) {
			spigot.getLogger().warning(this.getClass().getSimpleName()+" module failed to initialize: "+c);
		}
	}
    
    @EventHandler
    public void getMessage(SynergyPluginMessage e) {
        if (!e.getIdentifier().equals("hello")) {
            return;
        }
        for (String s : e.getArgs()) {
        	Bukkit.broadcastMessage(s);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    	switch (args[0]) {
    		case "reload":

	            if (sender.hasPermission("synergy.reload")) {
	                new WebServer(spigot).stop();
	        		spigot.getJda().shutdown();
	            	spigot.reloadConfig();
	                new Localizations(spigot).initialize();
	                new SynergyConfig(spigot).initialize();
	                new WebServer(spigot).start();
	                new Discord(spigot).initialize();
	                sender.sendMessage("synergy-reloaded");
	                return true;
	            } else {
	                sender.sendMessage("synergy-no-permission");
	                return true;
	            }

    		case "broadcast":
    			
    	    	SynergyPluginMessage spm = new SynergyPluginMessage("hello");
    	    	spm.setArguments(Arrays.copyOfRange(args, 1, args.length)).send(spigot);
    	    	break;

    	}
    	

    	
        return true;
    }
    
}
