package me.synergy.bukkit.events;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

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

    public SynergyEvent(String identifier) {
        this.identifier = identifier;
    }

    public SynergyEvent(String identifier, UUID uuid, String options) {
        this.identifier = identifier;
        this.uuid = uuid;
        this.options = getOptionsAsMap(options);

	}

	public SynergyEvent(String identifier, UUID uuid, Map<String, String> options) {
        this.identifier = identifier;
        this.uuid = uuid;
        this.options = options;
	}

	public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());
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

    public DataObject getOption(String option) {
        return new DataObject(options.get(option));
    }

	public UUID getPlayerUniqueId() {
		return uuid;
	}

    private Map<String, String> getOptionsAsMap(String options) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        return gson.fromJson(options, type);
    }
    
	public BreadMaker getBread() {
		return new BreadMaker(getPlayerUniqueId());
	}

	public OfflinePlayer getOfflinePlayer() {
		return getPlayerUniqueId() == null ? null : Bukkit.getOfflinePlayer(getPlayerUniqueId());
	}

	public String getOptionsAsJson() {
        Gson gson = new Gson();
        return gson.toJson(options);
    }

}



