package me.synergy.modules;

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
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import me.synergy.brains.Synergy;
import me.synergy.utils.Utils;

public class Config {
    private String configFile = "plugins/Synergy/config.yml";
    public static Map<String, Object> configValues;

    public void initialize() {
        try {

            loadConfig();

            addDefault("storage.type", "sqlite");
            addDefault("storage.host", "plugins/Synergy/data.db");
            addDefault("storage.user", "user");
            addDefault("storage.port", "3306");
            addDefault("storage.password", "password");
            addDefault("storage.database", "database");
            
            addDefault("localizations.enabled", true);
            addDefault("localizations.default-language", "en");

            addDefault("openai.enabled", false);
            addDefault("openai.token", "token");
            addDefault("openai.response-size", 64);
            addDefault("openai.temperature", 0.7);
            addDefault("openai.model", "gpt-3.5-turbo-instruct-0914");

            addDefault("discord.enabled", false);
            addDefault("discord.bot-token", "token");
            addDefault("discord.player-join-leave-messages", true);
            addDefault("discord.invite-link", "https://discord.gg/example");
            addDefault("discord.activities", new String[] {"Activity Example", "Another Activity Example"});
            addDefault("discord.avatar-link", "http://example.com:8153/head/%UUID%");
            addDefault("discord.channels.global", "00000000000000000");
            addDefault("discord.channels.admin", "00000000000000000");
            addDefault("discord.channels.log", "00000000000000000");
            addDefault("discord.channels.merge-similar-embeds", false);
            addDefault("discord.synchronization.sync-roles-from-discord-to-mc", false);
            addDefault("discord.synchronization.sync-roles-form-mc-to-discord", false);
            addDefault("discord.synchronization.use-vault", true);
            addDefault("discord.synchronization.custom-command-remove", "lp user %PLAYER% parent remove %GROUP%");
            addDefault("discord.synchronization.custom-command-add", "lp user %PLAYER% parent add %GROUP%");
            addDefault("discord.synchronization.roles.owner", "00000000000000000");
            addDefault("discord.synchronization.roles.admin", "00000000000000000");
            addDefault("discord.synchronization.roles.moder", "00000000000000000");
            addDefault("discord.synchronization.roles.default", "00000000000000000");
            addDefault("discord.synchronization.verified-role", "00000000000000000");
            addDefault("discord.gpt-bot.enabled", false);
            addDefault("discord.gpt-bot.name", "Stepan");
            addDefault("discord.gpt-bot.personality", "Act as a cat. Answer this question in a cat style: %MESSAGE%");
            addDefault("discord.hightlights.enabled", false);
            addDefault("discord.hightlights.comments", false);
            addDefault("discord.hightlights.channels", new String[] {"00000000000000000"});
            addDefault("discord.hightlights.reaction-emoji", "♥");

            addDefault("web-server.enabled", false);
            addDefault("web-server.domain", "example.com");
            addDefault("web-server.custom-texturepack", false);
            addDefault("web-server.custom-texturepack-url", "http://example.com:8153/example.zip");
            addDefault("web-server.port", 8153);

            addDefault("votifier.enabled", false);
            addDefault("votifier.rewards", new String[] {"eco give %PLAYER% 1"});
            addDefault("votifier.monitorings", new String[] {"https://example.com/vote/example"});

            if (Synergy.isRunningSpigot()) {

                addDefault("motd.enabled", true);
                addDefault("motd.message", "Message of The Day Example");
                addDefault("motd.max-players", 100);
                
                addDefault("placeholder-output-replacements.%faction_name%.%faction_name%", "[%faction_name%] ");
                addDefault("placeholder-output-replacements.%faction_name%.%none%", "");
                
                addDefault("localizations.color-themes.default.primary", "<#1abc9c>");
                addDefault("localizations.color-themes.default.secondary", "<#3498db>");
                addDefault("localizations.color-themes.default.success", "<#2ecc71>");
                addDefault("localizations.color-themes.default.danger", "<#e74c3c>");
                
                addDefault("localizations.pronouns", true);
            	
                addDefault("synergy-plugin-messaging.enabled", false);
                addDefault("synergy-plugin-messaging.servername", "SomeFabulousServer");
                addDefault("synergy-plugin-messaging.token", "Copy plugin-messaging-token from config.yml of Synergy in your Proxy folder");

                addDefault("chat-manager.enabled", true);
                addDefault("chat-manager.local-chat", true);
                addDefault("chat-manager.custom-color-tags.&p", "<#BDC581>");
                addDefault("chat-manager.chat-filter.blocked-words", new String[] {"fuck", "bitch"});
                addDefault("chat-manager.chat-filter.ignored-words", new String[] {"чіпідрос", "книга"});
                addDefault("chat-manager.chat-filter.blocked-words-tolerance-percentage", 38.5);
                addDefault("chat-manager.local-chat-radius", 500);
                addDefault("chat-manager.integrations.plotsquared-plot-chat", false);
                addDefault("chat-manager.integrations.factions-chat", false);
                addDefault("chat-manager.cross-server-global-chat", true);
                addDefault("chat-manager.play-sound-on-chat", false);
                
                addDefault("chat-manager.colors.global", "&e");
                addDefault("chat-manager.colors.local", "&f");
                addDefault("chat-manager.colors.discord", "&b");
                addDefault("chat-manager.colors.admin", "&c");
                addDefault("chat-manager.colors.discord_admin", "&c");
                
                addDefault("chat-manager.chat-tag.global", "[G]");
                addDefault("chat-manager.chat-tag.discord", "[Discord]");
                addDefault("chat-manager.chat-tag.discord_admin", "[Discord]");
                addDefault("chat-manager.chat-tag.admin", "[A]");
                addDefault("chat-manager.chat-tag.local", "[L]");
                
                addDefault("chat-manager.format", "%COLOR%%CHAT% %DISPLAYNAME%%COLOR%: %MESSAGE%");
                
                addDefault("chat-manager.custom-emojis.<3", "❤");
                addDefault("chat-manager.custom-emojis.:flip:", "(╯°益°）╯︵ ┻━┻");
                addDefault("chat-manager.custom-emojis.:v:", "✔");
                addDefault("chat-manager.custom-emojis.:x:", "✘");
                addDefault("chat-manager.custom-emojis.(c)", "©");
                addDefault("chat-manager.custom-emojis.:hi:", "(´• ω •`)ﾉ");
                addDefault("chat-manager.custom-emojis.:love:", "╰(❤ω❤)╯");
                addDefault("chat-manager.custom-emojis.:cry:", "o(╥﹏╥)o");
                addDefault("chat-manager.custom-emojis.(r)", "®");
                addDefault("chat-manager.custom-emojis.:hugs:", "⊂(￣▽￣)⊃");
                addDefault("chat-manager.custom-emojis.:hid:", "┬┴┤･ω･)ﾉ");

                Synergy.getSpigot().getConfig().options().copyDefaults(true);
                Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " file has been initialized!");

            }

            if (Synergy.isRunningVelocity() || Synergy.isRunningBungee()) {

                addDefault("synergy-plugin-messaging.enabled", true);
                addDefault("synergy-plugin-messaging.servername", "Proxy");
                addDefault("synergy-plugin-messaging.token", Utils.generateRandomString(50));

                Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " file has been initialized!");

            }

            saveConfig();

        } catch (Exception c) {
            Synergy.getLogger().warning(String.valueOf(getClass().getSimpleName()) + " file failed to initialize: " + c);
        }
    }

    private void loadConfig() {
        try {
            configValues = new HashMap<>();
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
	        	configValues.putAll(s);
	        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        try {
	        OutputStream output = new FileOutputStream(this.configFile);
	        DumperOptions options = new DumperOptions();
	        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
	        Yaml yaml = new Yaml(options);
	        yaml.dump(configValues, new OutputStreamWriter(output));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public Object get(String key) {
        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = configValues;
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
    public void set(String key, Object value) {
        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = configValues;
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

    private void addDefault(String key, String[] value) {
    	if (get(key) == null) {
            set(key, value);
    	}
        
    }

    private void addDefault(String key, String value) {
       if (get(key) == null) {
            set(key, value);
        }
    }

    private void addDefault(String key, double value) {
        if (get(key) == null) {
            set(key, Double.valueOf(value));
        }
    }

    private void addDefault(String key, boolean value) {
        if (get(key) == null) {
            set(key, Boolean.valueOf(value));
        }
    }

    private void addDefault(String key, int value) {
        if (get(key) == null) {
            set(key, Integer.valueOf(value));
        }
    }

    public boolean getBoolean(String key) {
        Object value = get(key);
        return value != null ? (Boolean) value : null;
    }

    public Integer getInt(String key) {
        Object value = get(key);
        return value != null ? (int) value : null;
    }

    public String getString(String key) {
        Object value = get(key);
        return value != null ? (String) value : null;
    }

    public String getString(String key, String defaultIfNull) {
        return (getString(key) != null) ? getString(key) : defaultIfNull;
    }

    public double getDouble(String key) {
        Object value = get(key);
        return value != null ? (double) value : null;
    }

    @SuppressWarnings("unchecked")
    public List <String> getStringList(String key) {
        Object value = get(key);
        return (value != null) ? (List <String>) value : null;
    }

    public boolean isSet(String key) {
        return (get(key) != null);
    }

	@SuppressWarnings("unchecked")
    public Map<String, Object> getConfigurationSection(String path) {
        if (path == null || path.isEmpty()) {
            return configValues;
        }
        String[] keys = path.split("\\.");
        Map<String, Object> section = configValues;
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