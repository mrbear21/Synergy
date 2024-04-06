package me.synergy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.synergy.brains.Synergy;
import me.synergy.modules.LocalesManager;
import me.synergy.utils.LangTagProcessor;
import me.synergy.utils.Utils;

public class SynergyCommand implements CommandExecutor {

    public void initialize() {
        Synergy.getSpigot().getCommand("synergy").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (args[0]) {
            case "reload":
                if (sender.hasPermission("synergy.reload")) {
                	Synergy.getDiscord().shutdown();
                	Synergy.getSpigot().reloadConfig();
                    Synergy.getLocalesManager().initialize();
                    Synergy.getDataManager().initialize();
                    Synergy.getConfig().initialize();
                    Synergy.getDiscord().initialize();
                    sender.sendMessage("<lang>synergy-reloaded</lang>");
                    return true;
                }
                sender.sendMessage("<lang>synergy-no-permission</lang>");
                return true;
            case "test":
            	String input = LangTagProcessor.processLangTags("<lang>test</lang>", LocalesManager.getDefaultLanguage());
            	Utils.sendFakeBook((Player) sender, "Colors", input);
        }
        return true;
    }
}