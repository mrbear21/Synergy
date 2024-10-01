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
import me.synergy.utils.BookMessage;
import me.synergy.utils.Translation;

public class ThemeCommand implements CommandExecutor, TabCompleter {

	public ThemeCommand() {}

    public void initialize() {
    	if (!Synergy.getConfig().getBoolean("localizations.enabled")) {
	    	return;
	    }
        Synergy.getSpigot().getCommand("theme").setExecutor(this);
        Synergy.getSpigot().getCommand("theme").setTabCompleter(this);

	}

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    	if (args.length < 2) {
	        Set<String> themes = Synergy.getConfig().getConfigurationSection("localizations.color-themes").keySet();
	        return new ArrayList<>(themes);
    	}
    	return null;
    }

    @Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Player player = (Player) sender;
    	BreadMaker bread = new BreadMaker(player.getUniqueId());
    	Set<String> themes = Synergy.getConfig().getConfigurationSection("localizations.color-themes").keySet();
    	if (args.length > 0 && themes.contains(args[0].toLowerCase())) {
    		bread.setData("theme", args[0].toLowerCase());
    		sender.sendMessage(Translation.processLangTags("<lang>synergy-selected-theme</lang>", bread.getLanguage()).replace("%THEME%", args[0]));
    		return true;
    	} else if (args.length > 0 && args[0].equalsIgnoreCase("auto")) {
    		bread.setData("theme", null);
    		sender.sendMessage(Translation.processLangTags("<lang>synergy-selected-theme</lang>", bread.getLanguage()).replace("%THEME%", args[0]));
    		return true;
    	} else if (args.length > 0) {
    		sender.sendMessage("<lang>synergy-command-usage</lang> /theme "+themes);
    		return true;
    	}

    	BookMessage.sendFakeBook(player, "Themes", "<lang>synergy-themes</lang>");
        return true;
    }
}