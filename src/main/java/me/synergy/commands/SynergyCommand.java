package me.synergy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.synergy.brains.Synergy;

public class SynergyCommand implements CommandExecutor {

    public void initialize() {
        Synergy.getSpigot().getCommand("synergy").setExecutor(this);
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (args[0]) {
            case "reload":
                if (sender.hasPermission("synergy.reload")) {
                	Synergy.getDiscord().shutdown();
                	Synergy.getSpigot().reloadConfig();
                    Synergy.getLocalesManager().initialize();
                    Synergy.getDataManager().initialize();
                    Synergy.getConfig().initialize();
                    Synergy.getDiscord().initialize();
                    sender.sendMessage("<lang>synergy-reloaded</lang>");
                    return true;
                }
                sender.sendMessage("<lang>synergy-no-permission</lang>");
                return true;
            case "action":
            	
            	return true;
        }
        return true;
    }
}