package me.synergy.modules;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import me.synergy.brains.Synergy;

public class DataManager {

	private Object data;
	
	public DataManager(Object data) {
		this.data = data;
	}
	
	public DataManager() {
	}

	public void initialize() {
		
		if (!new File(Synergy.getSpigot().getDataFolder(), "data.yml").exists()) {
			Synergy.getLogger().info("Creating data file...");
			try {
				Synergy.getSpigot().saveResource("data.yml", false);
			} catch (Exception c) { c.printStackTrace(); }
		}
		
        File dataFile = new File(Synergy.getSpigot().getDataFolder(), "data.yml");
        if (dataFile.exists()) {
            try {
            	Synergy.getSpigot().getDataFile().load(dataFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        
        saveConfig();
	}
	
	public void setData(String key, String value) {
		Synergy.getSpigot().getDataFile().set(key, value);
		try {
			File dataFile = new File(Synergy.getSpigot().getDataFolder(), "data.yml");
			Synergy.getSpigot().getDataFile().save(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public DataManager getData(String key) {
		return new DataManager(Synergy.getSpigot().getDataFile().get(key));
	}
	
	public String getAsString() {
		return data != null ? (String) data : null;
	}
	
	public Integer getAsInteger() {
		return data != null ? (Integer) data : null;
	}
	
	public Boolean getAsBoolean() {
		return data != null ? (Boolean) data : null;
	}

	public UUID getAsUUID() {
		return data != null ? UUID.fromString(getAsString()) : null;
	}
	
	public ConfigurationSection getConfigurationSection(String path) {
		return Synergy.getSpigot().getDataFile().get(path) == null ? null : Synergy.getSpigot().getDataFile().getConfigurationSection(path);
		
	}

	public FileConfiguration getConfig() {
		return Synergy.getSpigot().getDataFile();
	}

	public void saveConfig() {
		try {
			File dataFile = new File(Synergy.getSpigot().getDataFolder(), "data.yml");
			Synergy.getSpigot().getDataFile().save(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isSet(String path) {
		return Synergy.getSpigot().getDataFile().get(path) != null;
	}

}
