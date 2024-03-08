package me.synergy.events;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.synergy.brains.Synergy;

public class SynergyPluginEvent extends Event implements Listener {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private String identifier;
    private String[] args;
	private String player;

    public SynergyPluginEvent(String identifier, String[] args) {
        this.identifier = identifier;
        this.args = args;
    }

	public SynergyPluginEvent(String identifier) {
        this.identifier = identifier;
	}

	public SynergyPluginEvent(String identifier, String player, String[] args) {
        this.identifier = identifier;
        this.args = args;
        this.player = player;
	}

	public SynergyPluginEvent() {
	}

	public void initialize() {
		Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigotInstance());
		Synergy.getSpigotInstance().getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
	
	}
	
	public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public String getIdentifier() {
        return this.identifier;
    }
    
    public String[] getArgs() {
        return this.args;
    }
    
    public String getPlayer() {
        return this.player;
    }

	public SynergyPluginEvent setArguments(String[] args) {
        this.args = args;
        return this;
	}
	
	public SynergyPluginEvent setArgument(String args) {
        this.args = new String[] {args};
        return this;
	}
	
	public SynergyPluginEvent setPlayer(String player) {
        this.player = player;
        return this;
	}

	public void send(Plugin spigot) {
		if (spigot.getConfig().getBoolean("synergy-plugin-messaging.enabled")) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF(Synergy.getSynergyToken());
		    out.writeUTF(identifier);
		    out.writeUTF(player);
	        for (String arg : args) {
	    	    out.writeUTF(arg);
	        }
		    Bukkit.getServer().sendPluginMessage(spigot, "net:synergy", out.toByteArray());
		} else {
			if (player == null || (player != null && Bukkit.getPlayer(player) != null)) {
			    Bukkit.getScheduler().runTask(spigot, new Runnable() {
			    	@Override
			        public void run() {
						Bukkit.getServer().getPluginManager().callEvent(new SynergyPluginEvent(identifier, player, args));
			        }
			     });
			} else {
				Synergy.getSpigotInstance().getConfig().set("tempdata."+player+"."+identifier, args);
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerLoginEvent event) {
		String player = event.getPlayer().getName();
		if (Synergy.getSpigotInstance().getConfig().get("tempdata."+player) != null) {
			ConfigurationSection tempData = Synergy.getSpigotInstance().getConfig().getConfigurationSection("tempdata." + player);
	        if (tempData != null) {
	            for (String identifier : tempData.getKeys(false)) {
	            	List<String> args = Synergy.getSpigotInstance().getConfig().getStringList("tempdata."+player+"."+identifier);
				    Bukkit.getScheduler().runTask(Synergy.getSpigotInstance(), new Runnable() {
				    	@Override
				        public void run() {
							Bukkit.getServer().getPluginManager().callEvent(new SynergyPluginEvent(identifier, player, args.toArray(new String[0])));
				        }
				     });
				    Synergy.getSpigotInstance().getConfig().set("tempdata."+player+"."+identifier, null);
	            }
	        }
		}
	}

}