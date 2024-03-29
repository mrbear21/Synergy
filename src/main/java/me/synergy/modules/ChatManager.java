package me.synergy.modules;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
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
import me.synergy.objects.BreadMaker;
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
				sender.sendMessage(Utils.processColors(t)+t);
			}
		}
		
		return true;
	}


	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {

        if (!event.isCancelled()) {
            event.setCancelled(true);

            Synergy.createSynergyEvent("chat").setPlayerUniqueId(event.getPlayer().getUniqueId()).setOption("player", event.getPlayer()
            		.getName()).setOption("message", event.getMessage()).setOption("chat", getChatTypeFromMessage(event.getMessage())).send();
            
            Synergy.createSynergyEvent("discord").setPlayerUniqueId(event.getPlayer().getUniqueId()).setOption("player", event.getPlayer().getName())
	            .setOption("message", Utils.stripColorTags(event.getMessage())).setOption("chat", getChatTypeFromMessage(event.getMessage())).send();
            
            String botName = Synergy.getDiscord().getBotName();
            if (removeChatTypeSymbol(event.getMessage()).toLowerCase().startsWith(botName.toLowerCase()) && getChatTypeFromMessage(event.getMessage()).equals("global")) {
                String question = Synergy.getConfig().getString("discord.gpt-bot.personality").replace("%MESSAGE%", Utils.removeIgnoringCase(botName, removeChatTypeSymbol(event.getMessage())));
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
        	if (Bukkit.getPlayer(event.getPlayerUniqueId()) != null) {
        		Bukkit.getPlayer(event.getPlayerUniqueId()).sendMessage(event.getOption("message").getAsString());
        	}
        }
        if (event.getIdentifier().equals("announcement")) {
        	for (Player p : Bukkit.getOnlinePlayers()) {
        		BreadMaker bread = Synergy.getBread(p.getUniqueId());
        		bread.sendMessage(bread.translateString(event.getOption("message").getAsString()).replace("%ARGUMENT%", event.getOption("argument").getAsString()));
        	}
        }
        
        if (event.getIdentifier().equals("chat")) {
        	
        	UUID uuid = event.getPlayerUniqueId();
	        String chatType = event.getOption("chat").getAsString();
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
	                    if ((chatType.equals("admin") && recipient.hasPermission("synergy.chat.admin")) || chatType.startsWith("discord")) {
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
        String chatType = event.getOption("chat").getAsString();
        String message = event.getOption("message").getAsString();
        OfflinePlayer sender = event.getOfflinePlayer();
        String displayname = sender != null ? sender.getName() : event.getOption("player").getAsString();

        if (chatType.contains("discord")) {
        	format = format.replace(Utils.detectPlayernamePlaceholder(format, "%DISPLAYNAME%"), displayname);
        }
        
        if (Synergy.isDependencyAvailable("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(sender, format);
        }

        message = removeSynergyTranslationKeys(message);
        message = removeChatTypeSymbol(message);
        message = message.replace("</lang>", "<lang>");
        if (sender == null || !event.getBread().hasPermission("synergy.chat.color")) {
        	message = Utils.stripColorTags(message);
        }      
        message = Utils.translateSmiles(message);
        message = Utils.censorBlockedWords(message, getBlockedWorlds());
        
        format = format.replace("%DISPLAYNAME%", displayname);
        format = format.replace("%MESSAGE%", message);
        format = format.replace("%CHAT%", String.valueOf(chatType.charAt(0)).toUpperCase());
        format = format.replace("%COLOR%", getChatColor(chatType));
        format = format.replace("%RANDOM%", String.valueOf(new Random().nextInt(99)));
        format = Utils.processColors(format);
        
        return format;
    }

	private List<String> getBlockedWorlds() {
		return Synergy.getSpigot().getConfig().getStringList("chat-manager.blocked-words");
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