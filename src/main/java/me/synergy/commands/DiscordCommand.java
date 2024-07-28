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
import me.synergy.objects.BreadMaker;
import me.synergy.utils.Translation;

public class DiscordCommand implements CommandExecutor, TabCompleter, Listener {

	public void initialize() {
        if (!Synergy.getConfig().getBoolean("discord.enabled")) {
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());
        Synergy.getSpigot().getCommand("discord").setExecutor(this);
        Synergy.getSpigot().getCommand("discord").setTabCompleter(this);

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
    	BreadMaker bread = Synergy.getBread(player.getUniqueId());
    	if (!player.hasPermission("synergy.discord")) {
    		bread.sendMessage("<lang>synergy-no-permission</lang>");
    		return true;
    	}
        if (args.length == 0) {
        	bread.sendMessage(Translation.processLangTags("<lang>synergy-discord-invite</lang>", bread.getLanguage()).replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
            return true;
        }
        switch (args[0]) {
            case "link":
    	    	if (args.length < 2) {
    	    		bread.sendMessage("<lang>synergy-discord-link-cmd-usage</lang>");
    	    		return true;
    	    	}
            	Synergy.createSynergyEvent("make-discord-link").setPlayerUniqueId(player.getUniqueId()).setOption("tag", args[1]).send();
                break;
            case "confirm":
            	Synergy.createSynergyEvent("confirm-discord-link").setPlayerUniqueId(player.getUniqueId()).send();
                break;
            case "unlink":
            	Synergy.createSynergyEvent("remove-discord-link").setPlayerUniqueId(player.getUniqueId()).send();
                break;
        }
        return true;
    }

}
