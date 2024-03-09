package me.synergy.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.synergy.brains.Synergy;
import net.md_5.bungee.api.ChatColor;

public class VoteCommand implements CommandExecutor {

    public VoteCommand() {

    }

    public void initialize() {
        if (!Synergy.getConfig().getBoolean("votifier.enabled")) {
        	return;
        }
        Synergy.getSpigotInstance().getCommand("vote").setExecutor(this);
        Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("synergy.vote")) {
            List<String> monitorings = Synergy.getConfig().getStringList("votifier.monitorings");
            sender.sendMessage("synergy-vote-monitorings");
            monitorings.forEach(m -> sender.sendMessage(ChatColor.DARK_AQUA + " * " + ChatColor.UNDERLINE + m));
        } else {
            sender.sendMessage("synergy-no-permission");
        }
        return true;
    }
}