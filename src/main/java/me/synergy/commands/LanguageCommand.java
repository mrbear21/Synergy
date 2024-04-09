package me.synergy.commands;

import me.synergy.brains.Synergy;
import me.synergy.modules.LocalesManager;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.LangTagProcessor;
import me.synergy.utils.Utils;

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
        Synergy.getSpigot().getCommand("lang").setExecutor(this);
        Synergy.getSpigot().getCommand("language").setTabCompleter(this);
        Synergy.getSpigot().getCommand("lang").setTabCompleter(this);
        
	}

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    	if (args.length < 2) {
	        Set<String> languages = new HashSet<>(Synergy.getLocalesManager().getLanguages());
	        languages.add("auto");
	        return new ArrayList<>(languages);
    	}
    	return null;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Player player = (Player) sender;
    	BreadMaker bread = new BreadMaker(player.getUniqueId());
    	Set<String> languages = Synergy.getLocalesManager().getLanguages();
    	if (args.length > 0 && languages.contains(args[0].toLowerCase())) {
    		bread.setData("language", args[0]);
    		bread.sendMessage(bread.translateString("<lang>synergy-selected-language</lang>").replace("%LANGUAGE%", args[0]));
    		return true;
    	} else if (args.length > 0 && args[0].equalsIgnoreCase("auto")) {
    		bread.setData("language", null);
    		bread.sendMessage(bread.translateString("<lang>synergy-selected-language</lang>").replace("%LANGUAGE%", args[0]));
    		return true;
    	} else if (args.length > 0) {
    		sender.sendMessage("<lang>synergy-command-usage</lang> /language "+languages);
    		return true;
    	}
    	Utils.sendFakeBook((Player) sender, "Languages", new String[] { LangTagProcessor.processLangTags("<lang>synergy-languages</lang>", LocalesManager.getDefaultLanguage()) });
        return true;
    }
}