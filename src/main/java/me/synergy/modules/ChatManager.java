package me.synergy.modules;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
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
import net.md_5.bungee.api.ChatColor;

public class ChatManager implements Listener, CommandExecutor {

    public void initialize() {
        if (Synergy.getConfig().getBoolean("chat-manager.enabled")) {
            Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigotInstance());
            Synergy.getSpigotInstance().getCommand("chat").setExecutor(this);
            Synergy.getSpigotInstance().getCommand("colors").setExecutor(this);
            Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
        }
    }
    
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (label.equalsIgnoreCase("colors")) {
			ConfigurationSection tags = Synergy.getSpigotInstance().getConfig().getConfigurationSection("chat-manager.custom-color-tags");
			for (String t : tags.getKeys(false)) {
				sender.sendMessage(Synergy.getUtils().processColors(t)+t);
			}
		}
		
		return true;
	}


	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {

        if (event.isCancelled()) {
            Synergy.getLogger().info("Chat event cancelled by another plugin");
        } else {
            Synergy.createSynergyEvent("chat").setPlayer(event.getPlayer().getName()).setArguments(new String[] {event.getMessage()}).send();
            Synergy.createSynergyEvent("discord").setPlayer(event.getPlayer().getName()).setArguments(new String[] {event.getMessage()}).send();
            
            String botName = Synergy.getDiscord().getBotName();
            if (removeChatTypeSymbol(event.getMessage()).toLowerCase().startsWith(botName.toLowerCase()) && getChatTypeFromMessage(event.getMessage()).equals("global")) {
                String question = Synergy.getConfig().getString("discord.gpt-bot.personality").replace("%MESSAGE%", Synergy.getUtils().removeIgnoringCase(botName, removeChatTypeSymbol(event.getMessage())));
                String answer = ((CompletionChoice)(new OpenAi()).newPrompt(question).get(0)).getText().replace("\"", "").trim();
                Synergy.createSynergyEvent("chat").setPlayer(botName.replace(" ", "_")).setArguments(new String[] {"@" + answer}).send();
                Synergy.createSynergyEvent("discord").setPlayer(botName.replace(" ", "_")).setArguments(new String[] {"!" + answer}).send();
            }
            event.setCancelled(true);
        }
	
    }

    @EventHandler
    public void onSynergyPluginMessage(SynergyEvent event) {

        if (event.getIdentifier().equals("system-chat")) {
        	Bukkit.getPlayer(event.getPlayer()).sendMessage(event.getArgument());
        }
        
        if (event.getIdentifier().equals("chat")) {
        	
	        String player = event.getPlayer();
	        String message = removeSynergyTranslationKeys(event.getArgs()[0]);
	        String chatType = getChatTypeFromMessage(message);
	        String format = getFormattedChatMessage(player, message);
	
	        for (Player recipient: Bukkit.getOnlinePlayers()) {
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

    }

    public String removeSynergyTranslationKeys(String string) {
    	for (Entry<String, String> k : Synergy.getSpigotInstance().getLocales().get(Synergy.getDefaultLanguage()).entrySet()) {
    		string = string.replace(k.getKey(), k.getKey().replace("-", "–"));
    	}
		return string;
	}

	private String getFormattedChatMessage(String sender, String message) {
        String format = getFormat();
        String chatType = getChatTypeFromMessage(message);
        
        if (chatType.contains("discord")) {
        	format = format.replace(detectPlayernamePlaceholder(format, "%DISPLAYNAME%"), sender);
        }
        
        if (Synergy.isDependencyAvailable("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(sender), format);
        }

        message = removeChatTypeSymbol(message);
        message = censorBlockedWords(message, getBlockedWorlds());

        format = format.replace("%DISPLAYNAME%", sender);
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
            case "$":
                return "discord_admin";
            case "@":
                return "discord";
        }
        return Synergy.getConfig().getBoolean("chat-manager.local-chat") ? "local" : "global";
    }

    public String removeChatTypeSymbol(String message) {
        if (Arrays.asList(new String[] {"!", "\\", "$", "@"}).contains(String.valueOf(message.charAt(0)))) {
        	return message.substring(1);
        }
        return message;
    }

    private String getFormat() {
        return Synergy.getConfig().getString("chat-manager.format");
    }

}