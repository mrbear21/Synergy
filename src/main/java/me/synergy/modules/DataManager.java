package me.synergy.modules;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import me.synergy.brains.Synergy;
import me.synergy.objects.DataObject;

public class DataManager {

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
    			Synergy.getSpigot().getDataFile().save(dataFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
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

	public DataObject getData(String key) {
		return new DataObject(Synergy.getSpigot().getDataFile().get(key));
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
		return Synergy.getSpigot().getDataFile().isSet(path);
	}

}
