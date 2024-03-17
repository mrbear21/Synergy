package me.synergy.commands;

import me.synergy.brains.Synergy;

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LanguageCommand implements CommandExecutor {

    public void initialize() {
    	if (!Synergy.getConfig().getBoolean("localizations.enabled")) {
	    	return;
	    }
        Synergy.getSpigotInstance().getCommand("language").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Set<String> languages = Synergy.getLanguages();
    	if (args.length > 0 && languages.contains(args[0])) {
    		Synergy.getDataManager().setData("players."+sender.getName()+".language", args[0]);
    		sender.sendMessage(Synergy.translateString("synergy-selected-language", sender.getName()).replace("%LANGUAGE%", args[0]));
    		return true;
    	}
		sender.sendMessage("synergy-command-usage /language "+languages);
        return true;
    }
}