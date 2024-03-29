package me.synergy.commands;

import me.synergy.brains.Synergy;
import me.synergy.objects.BreadMaker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class LanguageCommand implements CommandExecutor, TabCompleter {

    public void initialize() {
    	if (!Synergy.getConfig().getBoolean("localizations.enabled")) {
	    	return;
	    }
        Synergy.getSpigot().getCommand("language").setExecutor(this);
        Synergy.getSpigot().getCommand("language").setTabCompleter(this);
        
	}

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Set<String> languages = new HashSet<>(Synergy.getLocalesManager().getLanguages());
        languages.add("auto");
        return new ArrayList<>(languages);
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Player player = (Player) sender;
    	BreadMaker bread = new BreadMaker(player.getUniqueId());
    	Set<String> languages = Synergy.getLocalesManager().getLanguages();
    	if (args.length > 0 && languages.contains(args[0].toLowerCase())) {
    		bread.setData("language", args[0]);
    		bread.sendMessage(bread.translateString("synergy-selected-language").replace("%LANGUAGE%", args[0]));
    		return true;
    	} else if (args[0].equalsIgnoreCase("auto")) {
    		bread.setData("language", null);
    		bread.sendMessage(bread.translateString("synergy-selected-language").replace("%LANGUAGE%", args[0]));
    		return true;
    	}
		sender.sendMessage("synergy-command-usage /language "+languages);
        return true;
    }
}