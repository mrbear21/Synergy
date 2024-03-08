package me.synergy.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import me.synergy.brains.Spigot;
import me.synergy.brains.Synergy;
import net.md_5.bungee.api.ChatColor;

public class VoteCommand implements CommandExecutor, Listener {

	private Spigot spigot;
	
    public VoteCommand(Spigot spigot) {
    	this.spigot = spigot;
	}

	public void initialize() {
		if (!spigot.getConfig().getBoolean("votifier.enabled")) {
			return;
		}
		spigot.getCommand("vote").setExecutor(new VoteCommand(spigot));
		Bukkit.getPluginManager().registerEvents(new VoteCommand(spigot), spigot);
		spigot.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
	}
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (sender.hasPermission("synergy.vote")) {
	    	List<String> monitorings = Synergy.getConfig().getStringList("votifier.monitorings");
	    	sender.sendMessage("meitene-vote-monitorings");
	    	monitorings.forEach(m -> sender.sendMessage(ChatColor.DARK_AQUA+" * "+ChatColor.UNDERLINE+m));
    	} else {
    		sender.sendMessage("synergy-no-permission");
    	}
        return true;
    }
    
}
