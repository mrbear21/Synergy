package me.synergy.commands;

import me.synergy.brains.Synergy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SynergyCommand implements CommandExecutor {

    public void initialize() {
        Synergy.getSpigotInstance().getCommand("synergy").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (args[0]) {
            case "reload":
                if (sender.hasPermission("synergy.reload")) {
                	Synergy.getDiscord().shutdown();
                	Synergy.getSpigotInstance().reloadConfig();
                    Synergy.getLocalizations().initialize();
                    Synergy.getDataManager().initialize();
                    Synergy.getConfig().initialize();
                    Synergy.getDiscord().initialize();
                    sender.sendMessage("synergy-reloaded");
                    return true;
                }
                sender.sendMessage("synergy-no-permission");
                return true;
        }
        return true;
    }
}