package me.synergy.modules;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.clip.placeholderapi.PlaceholderAPI;
import me.synergy.brains.Spigot;
import me.synergy.brains.Synergy;
import me.synergy.events.SynergyPluginMessage;
import net.md_5.bungee.api.ChatColor;

public class ChatManager implements Listener {

    private Spigot spigot;

    public ChatManager(Spigot spigot) {
        this.spigot = spigot;
    }

	public void initialize() {
		if (spigot.getConfig().getBoolean("chat-manager.enabled")) {
			Bukkit.getPluginManager().registerEvents(new ChatManager(spigot), spigot);
			spigot.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {

		if (!event.isCancelled()) {
			event.setCancelled(true);
    		new SynergyPluginMessage("chat").setArguments(new String[] {event.getPlayer().getName(), event.getMessage()}).send(spigot);
    		new SynergyPluginMessage("discord").setArguments(new String[] {event.getPlayer().getName(), event.getMessage()}).send(spigot);
		}
	}
	
    @EventHandler
    public void onSynergyPluginMessage(SynergyPluginMessage event) {
        if (!event.getIdentifier().equals("chat")) {
            return;
        }

        String player = event.getArgs()[0];
        String message = event.getArgs()[1];
        String chatType = getChatTypeFromMessage(message);
        String format = getFormattedChatMessage(player, message);

        for (Player recipient : Bukkit.getOnlinePlayers()) {
            switch (chatType) {
            	case "discord":
                case "global":
                    recipient.sendMessage(format);
                    playMsgSound(recipient);
                    break;
                case "local":
                    Player sender = Bukkit.getPlayer(player);
                    if (sender != null && sender.getLocation().distance(recipient.getLocation()) <= getLocalChatRadius()) {
                        recipient.sendMessage(format);
                        playMsgSound(recipient);
                    }
                    break;
                case "admin":
                case "discord_admin":
                    if ((chatType.equals("admin") && recipient.hasPermission("synergy.adminchat")) || chatType.startsWith("discord")) {
                        recipient.sendMessage(format);
                        playMsgSound(recipient);
                    }
                    break;
            }
        }
        logChatMessage(format);

    }
	
    private String getFormattedChatMessage(String sender, String message) {
        String format = getFormat();
        String chatType = getChatTypeFromMessage(message);
        
        if (chatType.contains("discord")) {
        	format = format.replace(detectPlayernamePlaceholder(format, "%DISPLAYNAME%"), sender);
        }
        
        if (spigot.isDependencyAvailable("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(sender), format);
        }

        message = removeChatTypeSymbol(message);
        message = censorBlockedWords(message, getBlockedWorlds());

        format = format.replace("%DISPLAYNAME%", sender);
        format = format.replace("%MESSAGE%", message);
        format = format.replace("%CHAT%", String.valueOf(chatType.charAt(0)).toUpperCase());
        format = format.replace("%COLOR%", getChatColor(chatType));

        format = processColors(format);
        
        return format;
    }

	public static String censorBlockedWords(String sentence, List<String> blockedWords) {
        return List.of(sentence.split("\\s+")).stream()
                .map(word -> {
                    for (String blockedWord : blockedWords) {
                        if (word.toLowerCase().contains(blockedWord)) {
                            double percentage = (double) blockedWord.length() / word.length() * 100;
                            if (Synergy.getSpigotInstance().getConfig().getDouble("chat-manager.blocked-words-tolerance-percentage") < percentage) {
                            	return censorWord(word);
                            }
                        }
                    }
                    return word;
                })
                .collect(Collectors.joining(" "));
    }
    
    public static String censorWord(String word) {
        if (word.length() <= 2) {
            return word;
        }
        
        char[] charArray = word.toCharArray();
        for (int i = 1; i < charArray.length - 1; i++) {
            charArray[i] = '*';
        }
        
        return new String(charArray);
    }
    
	private List<String> getBlockedWorlds() {
		return Synergy.getSpigotInstance().getConfig().getStringList("chat-manager.blocked-words");
	}
    
	public String detectPlayernamePlaceholder(String text, String defaultIfNull) {

	    int startIdx = text.indexOf("%");
	    if (startIdx == -1) {
	        return defaultIfNull;
	    }
	    int nameIdx = text.indexOf("name", startIdx);
	    if (nameIdx == -1) {
	        return defaultIfNull;
	    }
	    int leftPercentIdx = text.lastIndexOf("%", nameIdx);
	    int rightPercentIdx = text.indexOf("%", nameIdx + 1);
	    if (leftPercentIdx == -1 || rightPercentIdx == -1) {
	        return defaultIfNull;
	    }
	    return text.substring(leftPercentIdx, rightPercentIdx + 1);
	}
	
    public void playMsgSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
    }
	
	private String getChatColor(String chatType) {
	    return spigot.getConfig().getString("chat-manager.colors." + chatType + "-chat");
	}
	
	private int getLocalChatRadius() {
	    return spigot.getConfig().getInt("chat-manager.local-chat-radius");
	}
	
	private void logChatMessage(String format) {
	    Bukkit.getLogger().info(ChatColor.stripColor(format));
	}
	
    public static String processColors(String string) {
    	Pattern pattern = Pattern.compile("<(#[A-Fa-f0-9]{6})>");
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            string = string.replace(matcher.group(), "" + ChatColor.of(matcher.group(1)));
        }
        string = ChatColor.translateAlternateColorCodes('&', string);
        return string;
    }

	public String getChatTypeFromMessage(String message) {
		switch (String.valueOf(message.charAt(0))) {
			case "!":
				return "global";
			case "\\":
				return "admin";
			case "$":
				return "discord_admin";
			case "@":
				return "discord";
		}
		return Synergy.getSpigotInstance().getConfig().getBoolean("chat-manager.local-chat") ? "local" : "global";
	}
	
	public String removeChatTypeSymbol(String message) {

		switch (String.valueOf(message.charAt(0))) {
			case "!":
				return message.substring(1);
			case "\\":
				return message.substring(1);
			case "$":
				return message.substring(1);
			case "@":
				return message.substring(1);
		}
		return message;
	}
	
	
	private String getFormat() {
		return spigot.getConfig().get("chat-manager.format") != null ? spigot.getConfig().getString("chat-manager.format") : "[%CHAT%] %DISPLAYNAME%: %MESSAGE%";
	}
	
}
