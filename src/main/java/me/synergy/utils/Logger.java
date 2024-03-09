package me.synergy.utils;

import java.util.stream.Collectors;
import me.synergy.brains.Synergy;
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
    if (Synergy.isRunningSpigot().booleanValue()) {
      Synergy.getSpigotInstance().getLogger().info(string);
      if (broadcast)
        opLog(string); 
    } 
    if (Synergy.isRunningVelocity().booleanValue())
      Synergy.getVelocityInstance().getLogger().info(string); 
  }
  
  public void warning(String string) {
    if (Synergy.isRunningSpigot().booleanValue())
      Synergy.getSpigotInstance().getLogger().warning(Synergy.translateString(string)); 
    if (Synergy.isRunningVelocity().booleanValue())
      Synergy.getVelocityInstance().getLogger().warn(string); 
  }
  
  public void error(String string) {
    if (Synergy.isRunningSpigot().booleanValue())
      Synergy.getSpigotInstance().getLogger().severe(Synergy.translateString(string)); 
    if (Synergy.isRunningVelocity().booleanValue())
      Synergy.getVelocityInstance().getLogger().error(string); 
  }
}
