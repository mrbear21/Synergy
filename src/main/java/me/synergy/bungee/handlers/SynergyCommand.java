package me.synergy.bungee.handlers;

import me.synergy.brains.Bungee;
import me.synergy.discord.Discord;
import me.synergy.modules.Config;
import me.synergy.modules.DataManager;
import me.synergy.modules.LocalesManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class SynergyCommand extends Command {

    public void initialize() {
    	Bungee.getInstance().getProxy().getPluginManager().registerCommand(Bungee.getInstance(), new SynergyCommand());
    }

    public SynergyCommand() {
        super("synergy");
    }

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length > 0) {
			if (args[0].equals("reload")) {
				new Discord().shutdown();
				new Config().initialize();
				new DataManager().initialize();
				new LocalesManager().initialize();
				new Discord().initialize();
			}
		}
	}
    
}
