package me.synergy.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class SynergyPluginMessage extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private String identifier;
    private String[] args;

    public SynergyPluginMessage(String identifier, String[] args) {
        this.identifier = identifier;
        this.args = args;
    }

	public SynergyPluginMessage(String identifier) {
        this.identifier = identifier;
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

	public SynergyPluginMessage setArguments(String[] args) {
        this.args = args;
        return this;
	}
	
	public SynergyPluginMessage setArgument(String args) {
        this.args = new String[] {args};
        return this;
	}

	public void send(Plugin spigot) {
		if (spigot.getConfig().getBoolean("synergy-plugin-messaging.enabled")) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
		    out.writeUTF(getIdentifier());
	        for (String arg : args) {
	    	    out.writeUTF(arg);
	        }
		    Bukkit.getServer().sendPluginMessage(spigot, "net:synergy", out.toByteArray());
		} else {
		    Bukkit.getScheduler().runTask(spigot, new Runnable() {
		    	@Override
		        public void run() {
					Bukkit.getServer().getPluginManager().callEvent(new SynergyPluginMessage(identifier, args));
		        }
		     });
		}
	}

}