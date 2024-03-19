package me.synergy.events;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.synergy.brains.Synergy;
import me.synergy.objects.BreadMaker;

public class SynergyEvent extends Event implements Listener {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private String identifier;

    private String[] args;

    private UUID player;

    private boolean waitForPlayer = false;

    public SynergyEvent(String identifier, String[] args) {
        this.identifier = identifier;
        this.args = args;
    }

    public SynergyEvent(String identifier) {
        this.identifier = identifier;
    }

    public SynergyEvent(String identifier, UUID player, String[] args) {
        this.identifier = identifier;
        this.args = args;
        this.player = player;
    }

    public SynergyEvent() {}

    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigotInstance());
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String[] getArgs() {
        return (this.args.length > 0) ? this.args : new String[0];
    }

    public String getArgument() {
        return getArgs().length > 0 ? getArgs()[0] : "N/A";
    }

    public boolean getWaitForPlayerIfOffline() {
        return this.waitForPlayer;
    }

	public SynergyEvent setUniqueId(UUID UUID) {
		this.player = UUID;
        return this;
	}

	public UUID getUniqueId() {
		return player;
	}
    
    public SynergyEvent setArguments(String[] args) {
        this.args = args;
        return this;
    }

    public SynergyEvent setArgument(String args) {
        this.args = new String[] {
            args
        };
        return this;
    }

    public SynergyEvent setWaitForPlayerIfOffline(boolean waitPlayer) {
        this.waitForPlayer = waitPlayer;
        return this;
    }

    public void send() {
        if (Synergy.getConfig().getBoolean("synergy-plugin-messaging.enabled")) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(identifier);
            out.writeUTF(player.toString());
            out.writeUTF(String.valueOf(waitForPlayer));
            for (String arg: args) {
                out.writeUTF(arg);
            }
            Bukkit.getServer().sendPluginMessage(Synergy.getSpigotInstance(), "net:synergy", out.toByteArray());
        } else if (getWaitForPlayerIfOffline() && Bukkit.getPlayer(this.player) == null) {
            ConfigurationSection objs = Synergy.getDataManager().getConfigurationSection("synergy-event-waiting." + this.player.toString() + "." + this.identifier);
            int obj = (objs != null) ? (objs.getKeys(false).size() + 1) : 1;
            Synergy.getDataManager().getConfig().set("synergy-event-waiting."+this.player.toString()+"."+this.identifier+"." + obj, this.args);
            Synergy.getDataManager().saveConfig();
        } else {
            triggerEvent(this.identifier, this.player, this.args);
        }
    }
    
    public void triggerEvent(final String identifier, final UUID player, final String[] args) {
        Bukkit.getScheduler().runTask((Plugin) Synergy.getSpigotInstance(), new Runnable() {
            public void run() {
                Bukkit.getServer().getPluginManager().callEvent(new SynergyEvent(identifier, player, args));
            }
        });
    }

	public BreadMaker getBread() {
		return new BreadMaker(getUniqueId());
	}

}