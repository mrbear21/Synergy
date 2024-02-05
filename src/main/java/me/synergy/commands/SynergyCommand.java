package me.synergy.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.synergy.brain.BrainSpigot;
import me.synergy.events.SynergyPluginMessage;
import me.synergy.modules.Localizations;

public class SynergyCommand implements CommandExecutor, Listener {

	private BrainSpigot spigot;
	
    public SynergyCommand(BrainSpigot spigot) {
    	this.spigot = spigot;
	}

	public void register() {
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
	            	spigot.reloadConfig();
	            	
	                File localesFile = new File(spigot.getDataFolder(), "locales.yml");
	                if (localesFile.exists()) {
	                    try {
	                    	spigot.getLocalesFile().load(localesFile);
	                    } catch (IOException | InvalidConfigurationException e) {
	                        e.printStackTrace();
	                    }
	                }
	                new Localizations(spigot).initializeLocalizations();
	                
	                sender.sendMessage("Translations and configuration files reloaded successfully!");
	                return true;
	            } else {
	                sender.sendMessage("You don't have permission to use this command.");
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
