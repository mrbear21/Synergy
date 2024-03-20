package me.synergy.modules;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.theokanning.openai.completion.CompletionChoice;

import me.clip.placeholderapi.PlaceholderAPI;
import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;
import me.synergy.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class ChatManager implements Listener, CommandExecutor {

    public void initialize() {
        if (Synergy.getConfig().getBoolean("chat-manager.enabled")) {
            Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());
            Synergy.getSpigot().getCommand("chat").setExecutor(this);
            Synergy.getSpigot().getCommand("colors").setExecutor(this);
            Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
        }
    }
    
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (label.equalsIgnoreCase("colors")) {
			ConfigurationSection tags = Synergy.getSpigot().getConfig().getConfigurationSection("chat-manager.custom-color-tags");
			for (String t : tags.getKeys(false)) {
				sender.sendMessage(Synergy.getUtils().processColors(t)+t);
			}
		}
		
		return true;
	}


	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {

        if (!event.isCancelled()) {
            event.setCancelled(true);

            Synergy.createSynergyEvent("chat").setUniqueId(event.getPlayer().getUniqueId()).setOption("player", event.getPlayer()
            		.getName()).setOption("message", event.getMessage()).setOption("chat", getChatTypeFromMessage(event.getMessage())).send();
            
            Synergy.createSynergyEvent("discord").setUniqueId(event.getPlayer().getUniqueId()).setOption("player", event.getPlayer().getName())
	            .setOption("message", event.getMessage()).setOption("chat", getChatTypeFromMessage(event.getMessage())).send();
            
            String botName = Synergy.getDiscord().getBotName();
            if (removeChatTypeSymbol(event.getMessage()).toLowerCase().startsWith(botName.toLowerCase()) && getChatTypeFromMessage(event.getMessage()).equals("global")) {
                String question = Synergy.getConfig().getString("discord.gpt-bot.personality").replace("%MESSAGE%", Synergy.getUtils().removeIgnoringCase(botName, removeChatTypeSymbol(event.getMessage())));
                String answer = ((CompletionChoice)(new OpenAi()).newPrompt(question).get(0)).getText().replace("\"", "").trim();
                answer = answer.isEmpty() ? Synergy.translateString("synergy-service-unavailable") : answer;
                Synergy.createSynergyEvent("chat").setOption("player", botName).setOption("message", answer).setOption("chat", "discord").send();
                Synergy.createSynergyEvent("discord").setOption("player", botName).setOption("message", answer).setOption("chat", "global").send();
            }
        }
	
    }

    @EventHandler
    public void onSynergyPluginMessage(SynergyEvent event) {

        if (event.getIdentifier().equals("system-chat")) {
        	if (Bukkit.getPlayer(event.getUniqueId()) != null) {
        		Bukkit.getPlayer(event.getUniqueId()).sendMessage(event.getOption("message"));
        	}
        }
        
        if (event.getIdentifier().equals("chat")) {
        	
        	UUID uuid = event.getUniqueId();
	        String chatType = event.getOption("chat");
	        String format = getFormattedChatMessage(event);
	
	        for (Player recipient: Bukkit.getOnlinePlayers()) {
	            switch (chatType) {
	                case "discord":
	                case "global":
	                    recipient.sendMessage(format);
	                    playMsgSound(recipient);
	                    break;
	                case "local":
	                    Player sender = Bukkit.getPlayer(uuid);
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

    }

    public String removeSynergyTranslationKeys(String string) {
    	for (Entry<String, String> k : Synergy.getLocalesManager().getLocales().get(Synergy.getLocalesManager().getDefaultLanguage()).entrySet()) {
    		string = string.replace(k.getKey(), k.getKey().replace("-", "–"));
    	}
		return string;
	}

	private String getFormattedChatMessage(SynergyEvent event) {
        String format = getFormat();
        String chatType = event.getOption("chat");
        String message = removeSynergyTranslationKeys(event.getOption("message"));
        OfflinePlayer sender = event.getOfflinePlayer();
        String displayname = sender != null ? sender.getName() : event.getOption("player");

        if (chatType.contains("discord")) {
        	format = format.replace(detectPlayernamePlaceholder(format, "%DISPLAYNAME%"), displayname);
        }
        
        if (Synergy.isDependencyAvailable("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(sender, format);
        }

        message = removeChatTypeSymbol(message);
        message = censorBlockedWords(message, getBlockedWorlds());
        message = new Utils().translateSmiles(message);
        
        format = format.replace("%DISPLAYNAME%", displayname);
        format = format.replace("%MESSAGE%", message);
        format = format.replace("%CHAT%", String.valueOf(chatType.charAt(0)).toUpperCase());
        format = format.replace("%COLOR%", getChatColor(chatType));

        format = Synergy.getUtils().processColors(format);
        
        return format;
    }

    public String censorBlockedWords(String sentence, List < String > blockedWords) {
        double tolerance = Synergy.getConfig().getDouble("chat-manager.blocked-words-tolerance-percentage");
        for (String blockedWord: blockedWords) {
            String match = "";
            int start = 0, end = 0;
            for (int i = 0; i < sentence.length(); i++) {
                if (Character.isAlphabetic(sentence.charAt(i))) {
                    if (blockedWord.charAt(0) == sentence.charAt(i) || blockedWord.startsWith(String.valueOf(match) + sentence.charAt(i)) || (match.length() > 0 && match.charAt(match.length()-1) == sentence.charAt(i))) {
                        if (match.isEmpty())
                            start = i;
                        match = String.valueOf(match) + sentence.charAt(i);
                        end = i;
                    } else {
                        match = "";
                    }
                    if (blockedWord.equals(match)) {
                        String word = findWordInRange(sentence, start, end);
                        double percentage = (double) match.length() / (double) word.length() * 100;
                        if (tolerance < percentage || !word.contains(blockedWord)) 
                            sentence = censorPartOfSentence(sentence, start, end); 
                    }
                }
            }
        }
        return sentence;
    }

    public static String findWordInRange(String sentence, int start, int end) {
        String[] words = sentence.split("\\s+");
        for (String word : words) {
            int wordStart = sentence.indexOf(word);
            int wordEnd = wordStart + word.length() - 1;
            
            if (start >= wordStart && end <= wordEnd) {
                return word;
            }
        }
        return sentence.substring(start, end);
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

    
    public static String censorPartOfSentence(String sentence, int start, int end) {
        if (end-start < 2) {
            return sentence;
        }
        char[] charArray = sentence.toCharArray();
        for (int i = 1; i < charArray.length - 1; i++) {
        	if (i > start && i < end && Character.isAlphabetic(sentence.charAt(i))) {
        		charArray[i] = '*';
        	}
        }
        return new String(charArray);
    }

	private List<String> getBlockedWorlds() {
		return Synergy.getSpigot().getConfig().getStringList("chat-manager.blocked-words");
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
        player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
    }

    private String getChatColor(String chatType) {
        return Synergy.getConfig().getString("chat-manager.colors." + chatType + "-chat");
    }

    private int getLocalChatRadius() {
        return Synergy.getConfig().getInt("chat-manager.local-chat-radius");
    }

    private void logChatMessage(String format) {
        Bukkit.getLogger().info(ChatColor.stripColor(format));
    }

    public String getChatTypeFromMessage(String message) {
        switch (String.valueOf(message.charAt(0))) {
            case "!":
                return "global";
            case "\\":
                return "admin";
        }
        return Synergy.getConfig().getBoolean("chat-manager.local-chat") ? "local" : "global";
    }

    public String removeChatTypeSymbol(String message) {
        if (message.length() > 0 && Arrays.asList(new String[] {"!", "\\"}).contains(String.valueOf(message.charAt(0)))) {
        	return message.substring(1);
        }
        return message;
    }

    private String getFormat() {
        return Synergy.getConfig().getString("chat-manager.format");
    }

}