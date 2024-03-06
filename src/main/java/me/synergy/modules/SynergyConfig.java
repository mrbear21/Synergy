package me.synergy.modules;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Bukkit;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import me.synergy.brains.Spigot;
import me.synergy.brains.Velocity;
import me.synergy.utils.Utils;

public class SynergyConfig {

	private Spigot spigot;
	private Velocity bungee;
	private String configFile = "plugins/Synergy/config.yml";
	
    public SynergyConfig(Spigot spigot) {
    	this.spigot = spigot;
	}
    public SynergyConfig(Velocity bungee) {
    	this.bungee = bungee;
	}

    public void initialize() {
    	try {
	    	if (spigot != null) {
	    		
	    		if (!new File(spigot.getDataFolder(), "config.yml").exists()) {
	    			spigot.getLogger().info("Creating config file...");
	    			try {
	    				spigot.saveResource("config.yml", false);
	    			} catch (Exception c) { c.printStackTrace(); }
	    		}
	    		
		        spigot.getConfig().addDefault("synergy-plugin-messaging.enabled", false);
		        spigot.getConfig().addDefault("synergy-plugin-messaging.servername", Bukkit.getServer().getMotd());
		        spigot.getConfig().addDefault("synergy-plugin-messaging.token", "Copy plugin-messaging-token from config.yml of Synergy in your Proxy folder");
		        
		        spigot.getConfig().addDefault("localizations.enabled", true);
		        spigot.getConfig().addDefault("localizations.default-language", "en");
		        
		        spigot.getConfig().addDefault("chat-manager.enabled", true);
		        spigot.getConfig().addDefault("chat-manager.local-chat", true);
		        spigot.getConfig().addDefault("chat-manager.blocked-words", new String[] {"fuck", "bitch"});
		        spigot.getConfig().addDefault("chat-manager.blocked-words-tolerance-percentage", 38.5);
		        spigot.getConfig().addDefault("chat-manager.local-chat-radius", 500);
		        spigot.getConfig().addDefault("chat-manager.cross-server-global-chat", true);
		        spigot.getConfig().addDefault("chat-manager.colors.global-chat", "&e");
		        spigot.getConfig().addDefault("chat-manager.colors.local-chat", "&f");
		        spigot.getConfig().addDefault("chat-manager.colors.discord-chat", "&b");
		        spigot.getConfig().addDefault("chat-manager.colors.admin-chat", "&c");
		        spigot.getConfig().addDefault("chat-manager.colors.discord_admin-chat", "&c");
		        spigot.getConfig().addDefault("chat-manager.format", "[%CHAT%] %DISPLAYNAME%: %MESSAGE%");
		        
		        spigot.getConfig().addDefault("openai.enabled", false);
		        spigot.getConfig().addDefault("openai.token", "token");
		        spigot.getConfig().addDefault("openai.model", "gpt-3.5-turbo-instruct-0914");
		        
		        spigot.getConfig().addDefault("discord.enabled", false);
		        spigot.getConfig().addDefault("discord.bot-token", "token");
		        spigot.getConfig().addDefault("discord.channels.global-chat-channel", "00000000000000000");
		        spigot.getConfig().addDefault("discord.channels.admin-chat-channel", "00000000000000000");
		        spigot.getConfig().addDefault("discord.channels.console-channel", "00000000000000000");
		        spigot.getConfig().addDefault("discord.channels.log-channel", "00000000000000000");
		        spigot.getConfig().addDefault("discord.gpt-bot.enabled", false);
		        spigot.getConfig().addDefault("discord.gpt-bot.personality", "You are vile person. Give an answer in a mocking tone: %MESSAGE%");
		        spigot.getConfig().addDefault("web-server.enabled", false);
		        spigot.getConfig().addDefault("web-server.port", 8192);
		        spigot.getConfig().addDefault("votifier.enabled", false);
		        spigot.getConfig().addDefault("votifier.message", "synergy-voted-successfully");
		        spigot.getConfig().addDefault("votifier.rewards", new String[] {"eco give %PLAYER% 1"});
		        spigot.getConfig().options().copyDefaults(true);
		        spigot.saveConfig();
		        
		        spigot.getLogger().info(this.getClass().getSimpleName()+" file has been initialized!");

		    } else {
		    	
		    	bungee.getConfig().loadConfig();
		    	
		    	bungee.getConfig().addDefault("synergy-plugin-messaging.enabled", true);
		        spigot.getConfig().addDefault("synergy-plugin-messaging.servername", "Proxy");
		        bungee.getConfig().addDefault("synergy-plugin-messaging.token", new Utils().generateRandomString(50));
		        
		        bungee.getConfig().addDefault("default-language", "en");
		        
		        bungee.getConfig().addDefault("openai.enabled", false);
		        bungee.getConfig().addDefault("openai.token", "token");
		        bungee.getConfig().addDefault("openai.model", "gpt-3.5-turbo-instruct-0914");
		        
		        bungee.getConfig().addDefault("discord.enabled", false);
		        bungee.getConfig().addDefault("discord.bot-token", "token");
		        bungee.getConfig().addDefault("discord.global-chat-channel", "00000000000000000");
		        bungee.getConfig().addDefault("discord.admin-chat-channel", "00000000000000000");
		        bungee.getConfig().addDefault("discord.console-channel", "00000000000000000");
		        bungee.getConfig().addDefault("discord.log-channel", "00000000000000000");
		        bungee.getConfig().addDefault("web-server", false);
		        bungee.getConfig().addDefault("votifier.enabled", false);
		        bungee.getConfig().addDefault("votifier.port", 8192);
		        
		    	bungee.getLogger().info(this.getClass().getSimpleName()+" file has been initialized!");
		    }
	    	
		} catch (Exception c) {
			spigot.getLogger().warning(this.getClass().getSimpleName()+" file failed to initialize: "+c);
		}

    }
	    
    private void loadConfig() {
        try {
	    	bungee.configValues = new HashMap<>();
	        File file = new File(configFile);
	        if (!file.exists()) {
	            file.getParentFile().mkdirs();
	            file.createNewFile();
	            try (InputStream defaultConfigStream = getClass().getClassLoader().getResourceAsStream("config.yml")) {
	                if (defaultConfigStream != null) {
	                    Path configFilePath = Path.of(configFile);
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
	        	bungee.configValues.putAll(s);
	        }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

    private void saveConfig() {
        try {
            OutputStream output = new FileOutputStream(configFile);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            yaml.dump(bungee.configValues, new OutputStreamWriter(output));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    private Object getValue(String key) {
        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = bungee.configValues;
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
    
    @SuppressWarnings("unchecked")
    private void setValue(String key, Object value) {
        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = bungee.configValues;
        for (int i = 0; i < keys.length - 1; i++) {
            currentMap.computeIfAbsent(keys[i], k -> new HashMap<>());
            currentMap = (Map<String, Object>) currentMap.get(keys[i]);
        }
        if (value instanceof Map) {
            currentMap.put(keys[keys.length - 1], new HashMap<>((Map<String, Object>) value));
        } else {
            currentMap.put(keys[keys.length - 1], value);
        }
        saveConfig();
    }

    public void addDefault(String key, String value) {
    	if (getValue(key) == null) {
    		setValue(key, value);
    	}
    }
    
    public void addDefault(String key, boolean value) {
    	if (getValue(key) == null) {
    		setValue(key, value);
    	}
    }
    
    public void addDefault(String key, int value) {
    	if (getValue(key) == null) {
    		setValue(key, value);
    	}
    }
    
    public boolean getBoolean(String key) {
    	if (bungee != null) {
	    	Object value = getValue(key);
	    	return value != null ? (boolean) value : null;
    	}
    	return spigot.getConfig().getBoolean(key);
    }

    public Integer getInt(String key) {
    	if (bungee != null) {
	    	Object value = getValue(key);
	    	return value != null ? (int) value : null;
    	}
    	return spigot.getConfig().getInt(key);
    }
    
    public String getString(String key) {
    	if (bungee != null) {
	    	Object value = getValue(key);
	    	return value != null ? (String) value : null;
		}
		return spigot.getConfig().getString(key);
    }
    
	public String getString(String key, String defaultIfNull) {
		return getString(key) != null ? getString(key) : defaultIfNull;
	}
	
}
