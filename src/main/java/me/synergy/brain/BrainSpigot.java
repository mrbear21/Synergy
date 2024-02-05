package me.synergy.brain;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import me.synergy.commands.SynergyCommand;
import me.synergy.events.SynergyPluginMessage;
import me.synergy.objects.BreadMaker;
import me.synergy.objects.Config;
import me.synergy.modules.Localizations;

public class BrainSpigot extends JavaPlugin implements PluginMessageListener {
	
	private BrainSpigot INSTANCE;
	
	private FileConfiguration LOCALESFILE;

    private Map<String, HashMap<String, String>> LOCALES;

	private ProtocolManager PROTOCOLMANAGER;
	
	@Override
	public void onEnable() {
		INSTANCE = this;

		getServer().getMessenger().registerOutgoingPluginChannel(this, "net:synergy");
		getServer().getMessenger().registerIncomingPluginChannel(this, "net:synergy", this);
		
		PROTOCOLMANAGER = ProtocolLibrary.getProtocolManager();
		LOCALESFILE = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "locales.yml"));
		LOCALES = new HashMap<String, HashMap<String, String>>();
		
		new Config(this).register();
		new SynergyCommand(this).register();
		new Localizations(this).register();

		getLogger().info("Synergy is ready to be helpful for the all BreadMakers!");
		
	}

    public ProtocolManager getProtocolManager() {
        return PROTOCOLMANAGER;
    }
	
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("net:synergy")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String identifier = in.readUTF();
        List<String> argsList = new ArrayList<>();
        try {
            while (true) {
                argsList.add(in.readUTF());
            }
        } catch (Exception ignored) {}
        String[] args = argsList.toArray(new String[0]);
        Bukkit.getServer().getPluginManager().callEvent(new SynergyPluginMessage(identifier, args));
    }

	public void onDisable() {
		getLogger().info("Synergy has stopped it's service!");
	}
	
	public void log(String string) {
		log(string, true);
	}
	
	public void log(String string, boolean broadcast) {
		getLogger().info(string);
		if (broadcast) {
			for (Player p : Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("synergy.sudo")).collect(Collectors.toList())) {
				p.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+"[Console] "+string);
			}
		}
	}
	
	public Map<String, HashMap<String, String>> getLocales() {
		return LOCALES;
	}
	
	public FileConfiguration getLocalesFile() {
		return getLOCALESFILE();
	}
	
	public BrainSpigot getInstance() {
		return INSTANCE;
	}

	public BreadMaker getBread(String name) {
		return new BreadMaker(this, name);
	}

	public FileConfiguration getLOCALESFILE() {
		return LOCALESFILE;
	}

	public void setLOCALESFILE(FileConfiguration lOCALESFILE) {
		LOCALESFILE = lOCALESFILE;
	}

}


