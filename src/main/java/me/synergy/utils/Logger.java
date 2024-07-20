package me.synergy.utils;

import java.util.stream.Collectors;
import me.synergy.brains.Synergy;
import me.synergy.brains.Velocity;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Logger {
	
  private void opLog(String string) {
    for (Player p : Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("synergy.sudo")).collect(Collectors.toList()))
      p.sendMessage(ChatColor.GRAY +""+ ChatColor.ITALIC + "[Console] " + string); 
  }
  
  public void info(String string) {
    info(string, false);
  }
  
  public void info(String string, boolean broadcast) {
	try {
	    if (Synergy.isSpigot()) {
	      Synergy.getSpigot().getLogger().info(string);
	      if (broadcast) {
	        opLog(string);
	      }
	    } 
	    if (Synergy.isRunningVelocity()) {
	      Velocity.getLogger().info(string); 
	    }
	} catch (Exception c) {
		System.out.print(string);
	}
  }
  
  public void warning(String string) {
	try {
	    if (Synergy.isSpigot()) {
	      Synergy.getSpigot().getLogger().warning(string); 
	    }
	    if (Synergy.isRunningVelocity()) {
	    	Velocity.getLogger().warn(string); 
	    }
	} catch (Exception c) {
		System.out.print(string);
	}
  }
  
  public void error(String string) {
	try {
	    if (Synergy.isSpigot()) {
	      Synergy.getSpigot().getLogger().severe(string); 
	    }
	    if (Synergy.isRunningVelocity()) {
	    	Velocity.getLogger().error(string); 
	    }
	} catch (Exception c) {
		System.out.print(string);
	}
  }
  
  public void discord(String string) {
	Synergy.createSynergyEvent("discord-log").setOption("message", string).send();
  }
}
