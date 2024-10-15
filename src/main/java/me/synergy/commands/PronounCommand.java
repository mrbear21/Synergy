package me.synergy.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.synergy.brains.Synergy;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.Endings;

public class PronounCommand implements CommandExecutor, TabCompleter {

	public PronounCommand() {}

    public void initialize() {
    	if (!Synergy.getConfig().getBoolean("localizations.pronouns")) {
	    	return;
	    }
        Synergy.getSpigot().getCommand("pronoun").setExecutor(this);
        Synergy.getSpigot().getCommand("iamboy").setExecutor(this);
        Synergy.getSpigot().getCommand("iamgirl").setExecutor(this);
        Synergy.getSpigot().getCommand("gender").setExecutor(this);
        Synergy.getSpigot().getCommand("pronoun").setTabCompleter(this);
	}
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    	if (args.length < 2) {
	        Set<String> pronouns = Endings.getPronounsAsStringSet();
	        return new ArrayList<>(pronouns);
    	}
    	return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("<lang>synergy-command-not-player</lang>");
            return true;
        }
        
        Player player = (Player) sender;
        BreadMaker bread = new BreadMaker(player.getUniqueId());
        switch (label.toLowerCase()) {
            case "pronoun":
            case "gender":
                Set<String> pronouns = Endings.getPronounsAsStringSet();
                if (args.length > 0 && pronouns.contains(args[0].toUpperCase())) {
                    bread.setData("pronoun", args[0].toUpperCase());
                } else if (args.length == 0) {
                    sender.sendMessage("<lang>synergy-command-usage</lang> /pronoun " + pronouns);
                    return true;
                } else {
                    sender.sendMessage("<lang>synergy-invalid-pronoun</lang>"); 
                    return true;
                }
                break;

            case "iamgirl":
                bread.setData("pronoun", "SHE");
                break;

            case "iamboy":
                bread.setData("pronoun", "HE");
                break;

            default:
                sender.sendMessage("<lang>synergy-unknown-command</lang>");
                return true;
        }
        sender.sendMessage("<lang>synergy-your-pronoun<arg>" + bread.getPronoun() + "</arg></lang>");
        return true;
    }
    
}