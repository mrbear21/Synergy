package me.synergy.events;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import me.synergy.brains.Synergy;
import me.synergy.objects.BreadMaker;
import me.synergy.objects.DataObject;

public class SynergyEvent extends Event implements Listener {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private String identifier;

    private Map<String, String> options = new HashMap<>();

    private UUID uuid;

    private boolean waitForPlayer = false;

    public SynergyEvent(String identifier) {
        this.identifier = identifier;
    }

    public SynergyEvent(String identifier, UUID uuid, String options) {
        this.identifier = identifier;
        this.uuid = uuid;
        this.options = getOptionsAsMap(options);

	}

	public SynergyEvent() {
	}

	public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());
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

    public DataObject getOption(String option) {
        return new DataObject(options.get(option));
    }

    public boolean getWaitForPlayerIfOffline() {
        return this.waitForPlayer;
    }

	public SynergyEvent setPlayerUniqueId(UUID UUID) {
		this.uuid = UUID;
        return this;
	}

	public UUID getPlayerUniqueId() {
		return uuid;
	}

    public SynergyEvent setOption(String option, String value) {
        this.options.put(option, value);
        return this;
    }

    public SynergyEvent setWaitForPlayerIfOffline(boolean waitPlayer) {
        this.waitForPlayer = waitPlayer;
        return this;
    }

    private String getOptionsAsJson() {
        Gson gson = new Gson();
        return gson.toJson(options);
    }

    private Map<String, String> getOptionsAsMap(String options) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        return gson.fromJson(options, type);
    }

    public void send() {
        if (Synergy.getConfig().getBoolean("synergy-plugin-messaging.enabled")) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(identifier);
            out.writeUTF(uuid.toString());
            out.writeUTF(String.valueOf(waitForPlayer));
            out.writeUTF(getOptionsAsJson());

            Bukkit.getServer().sendPluginMessage(Synergy.getSpigot(), "net:synergy", out.toByteArray());
        } else if (getWaitForPlayerIfOffline() && Bukkit.getPlayer(this.uuid) == null) {
            ConfigurationSection objs = Synergy.getDataManager().getConfigurationSection("synergy-event-waiting." + this.uuid.toString() + "." + this.identifier);
            int obj = (objs != null) ? (objs.getKeys(false).size() + 1) : 1;
            Synergy.getDataManager().getConfig().set("synergy-event-waiting."+this.uuid.toString()+"."+this.identifier+"." + obj, getOptionsAsJson());
            Synergy.getDataManager().saveConfig();
        } else {
            triggerEvent();
        }
    }

    public void triggerEvent() {
        Bukkit.getScheduler().runTask(Synergy.getSpigot(), new Runnable() {
            @Override
			public void run() {
                Bukkit.getServer().getPluginManager().callEvent(new SynergyEvent(identifier, uuid, getOptionsAsJson()));
            }
        });
    }

	public BreadMaker getBread() {
		return new BreadMaker(getPlayerUniqueId());
	}

	public OfflinePlayer getOfflinePlayer() {
		return getPlayerUniqueId() == null ? null : Bukkit.getOfflinePlayer(getPlayerUniqueId());
	}

}