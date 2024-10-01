package me.synergy.modules;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import me.synergy.anotations.SynergyHandler;
import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class DataManager {

    public DataManager() {
    }

    public void initialize() {
        loadData();
        Synergy.getEventManager().registerEvents(this);
        Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
    }

    private String dataFile = "plugins/Synergy/data.yml";
    public static Map<String, Object> dataValues;
    
    private void loadData() {
        try {
        	dataValues = new HashMap<>();
	        File file = new File(dataFile);
	        if (!file.exists()) {
	            file.getParentFile().mkdirs();
	            file.createNewFile();
	            try (InputStream defaultConfigStream = getClass().getClassLoader().getResourceAsStream("data.yml")) {
	                if (defaultConfigStream != null) {
	                    Path configFilePath = Path.of(dataFile);
	                    Files.copy(defaultConfigStream, configFilePath, StandardCopyOption.REPLACE_EXISTING);
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        InputStream input = new FileInputStream(file);
	        Yaml yaml = new Yaml();
	        Map<? extends String, ? extends Object> s = yaml.load(input);
	        if (s != null) {
	        	dataValues.putAll(s);
	        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        try {
            OutputStream output = new FileOutputStream(dataFile);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            yaml.dump(dataValues, new OutputStreamWriter(output));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public Object get(String key) {
        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = dataValues;
        for (String k : keys) {
            if (currentMap.containsKey(k) && currentMap.get(k) instanceof Map) {
                currentMap = (Map<String, Object>) currentMap.get(k);
            } else {
                Object value = currentMap.get(k);
                return value;
            }
        }
        return null;
    }

	@SynergyHandler
	public void onSynergyEvent(SynergyEvent event) {
		if (!event.getIdentifier().equals("set-data")) {
			return;
		}
		set(event.getOption("option").getAsString(), event.getOption("value").getAsString());
	}
	
    
	@SuppressWarnings("unchecked")
	public void set(String key, Object value) {
        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = dataValues;
        for (int i = 0; i < keys.length - 1; i++) {
            String currentKey = keys[i];
            if (currentKey == null || currentKey.trim().isEmpty()) {
                throw new IllegalArgumentException("Key cannot be null or empty");
            }
            currentMap.computeIfAbsent(currentKey, k -> new HashMap<>());
            currentMap = (Map<String, Object>) currentMap.get(currentKey);
        }
        String finalKey = keys[keys.length - 1];
        if (finalKey == null || finalKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (value == null) {
            currentMap.remove(finalKey); // Видаляємо ключ, якщо значення null
        } else if (value instanceof Map) {
            currentMap.put(finalKey, new HashMap<>((Map<String, Object>) value));
        } else {
            currentMap.put(finalKey, value);
        }
	    saveConfig();
	}


	public boolean isSet(String option) {
		return get(option) != null;
	}
    
	@SuppressWarnings("unchecked")
    public Map<String, Object> getConfigurationSection(String path) {
        if (path == null || path.isEmpty()) {
            return dataValues;
        }
        String[] keys = path.split("\\.");
        Map<String, Object> section = dataValues;
        for (String key : keys) {
            if (section == null) {
                return new HashMap<String, Object>();
            }
            if (!(section.get(key) instanceof Map)) {
                return new HashMap<String, Object>();
            }
            section = (Map<String, Object>) section.get(key);
        }
        return section != null ? section : new HashMap<String, Object>();
    }
	

}


/*
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
*/