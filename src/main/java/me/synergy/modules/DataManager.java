package me.synergy.modules;

import java.io.File;
import java.io.IOException;

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
		
		if (!new File(Synergy.getSpigotInstance().getDataFolder(), "data.yml").exists()) {
			Synergy.getLogger().info("Creating data file...");
			try {
				Synergy.getSpigotInstance().saveResource("data.yml", false);
			} catch (Exception c) { c.printStackTrace(); }
		}
		
        File dataFile = new File(Synergy.getSpigotInstance().getDataFolder(), "data.yml");
        if (dataFile.exists()) {
            try {
            	Synergy.getSpigotInstance().getDataFile().load(dataFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
	}
	
	public void setData(String key, String value) {
		Synergy.getSpigotInstance().getDataFile().set(key, value);
		try {
			File dataFile = new File(Synergy.getSpigotInstance().getDataFolder(), "data.yml");
			Synergy.getSpigotInstance().getDataFile().save(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public DataManager getData(String key) {
		return new DataManager(Synergy.getSpigotInstance().getDataFile().get(key));
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

	public ConfigurationSection getConfigurationSection(String path) {
		return Synergy.getSpigotInstance().getDataFile().getConfigurationSection(path);
		
	}

	public FileConfiguration getConfig() {
		return Synergy.getSpigotInstance().getDataFile();
	}

	public void saveConfig() {
		try {
			File dataFile = new File(Synergy.getSpigotInstance().getDataFolder(), "data.yml");
			Synergy.getSpigotInstance().getDataFile().save(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
