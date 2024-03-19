package me.synergy.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import me.synergy.brains.Synergy;

public class DiscordCommand implements CommandExecutor, TabCompleter, Listener {

	public void initialize() {
        if (!Synergy.getConfig().getBoolean("discord.enabled")) {
            return;
        }
        
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigotInstance());
        Synergy.getSpigotInstance().getCommand("discord").setExecutor(this);
        Synergy.getSpigotInstance().getCommand("discord").setTabCompleter(this);
        
	}

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("link", "unlink", "confirm");
        } else if (args.length == 2 && args[0].equals("link")) {
            return Synergy.getDiscord().getAllUserTags().stream()
                    .filter(u -> u.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	Player player = (Player) sender;
        if (args.length == 0) {
            sender.sendMessage(Synergy.translateStringColorStripped("synergy-discord-invite").replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
            return true;
        }
        switch (args[0]) {
            case "link":
    	    	if (args.length < 2) {
    	    		sender.sendMessage("synergy-discord-link-cmd-usage");
    	    		return true;
    	    	}
            	Synergy.createSynergyEvent("make-discord-link").setUniqueId(player.getUniqueId()).setArgument(args[1]).send();
                break;
            case "confirm":
            	Synergy.createSynergyEvent("confirm-discord-link").setUniqueId(player.getUniqueId()).send();
                break;
            case "unlink":
            	Synergy.createSynergyEvent("remove-discord-link").setUniqueId(player.getUniqueId()).send();
                break;
        }
        return true;
    }
	
}
