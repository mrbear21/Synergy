package me.synergy.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.theokanning.openai.completion.CompletionChoice;

import me.clip.placeholderapi.PlaceholderAPI;
import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;
import me.synergy.integrations.PlotSquaredAPI;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.Translation;
import me.synergy.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class ChatManager implements Listener, CommandExecutor, TabCompleter {

    public void initialize() {
        if (Synergy.getConfig().getBoolean("chat-manager.enabled")) {
            Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());
            Synergy.getSpigot().getCommand("chat").setExecutor(this);
            Synergy.getSpigot().getCommand("colors").setExecutor(this);
            Synergy.getSpigot().getCommand("emojis").setExecutor(this);
            Synergy.getSpigot().getCommand("chat").setExecutor(this);
            Synergy.getSpigot().getCommand("chat").setTabCompleter(this);
            Synergy.getSpigot().getCommand("chatfilter").setExecutor(this);
            Synergy.getSpigot().getCommand("chatfilter").setTabCompleter(this);
            Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
        }
    }
    
    private List<String> getChats() {
		List<String> chats = new ArrayList<String>();
		if (Synergy.getConfig().getBoolean("chat-manager.local-chat")) {
			chats.add("local");
		} else {
			chats.add("global");
		}
		if (Synergy.isDependencyAvailable("PlotSquared") && Synergy.getConfig().getBoolean("chat-manager.local-chat-per-plotsquared-plot")) {
			chats.add("plot");
		}
		return chats;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    	
		if (!sender.hasPermission("synergy."+command.getLabel().toLowerCase())) {
			return null;
		}
    	
    	if (command.getLabel().equals("chat")) {
	    	if (args.length < 2) {
		        return getChats();
	    	}
    	}
    	if (command.getLabel().equals("chatfilter")) {
    		if (args.length < 2) {
    			return Arrays.asList(new String[] {"block", "ignore", "remove"});
    		} 
	    	if (args.length > 0 && args[0].equalsIgnoreCase("remove")) {
	    		return Stream.concat(Utils.getBlockedWorlds().stream(), Utils.getIgnoredWorlds().stream()).toList();
	    	}
    	}
    	return null;
    }
    
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (!sender.hasPermission("synergy."+label.toLowerCase())) {
			sender.sendMessage("<lang>synergy-no-permission</lang>");
			return true;
		}
		
		if (label.equalsIgnoreCase("chatfilter")) {
			if (args.length > 1) {
				if (args[0].equalsIgnoreCase("block")) {
					Utils.addBlockedWord(args[1].toLowerCase());
					sender.sendMessage("<lang>synergy-word-blocked<arg>"+args[1].toLowerCase()+"</arg></lang>");
				}
				if (args[0].equalsIgnoreCase("ignore")) {
					Utils.addIgnoredWord(args[1].toLowerCase());
					sender.sendMessage("<lang>synergy-word-ignored<arg>"+args[1].toLowerCase()+"</arg></lang>");
				}
				if (args[0].equalsIgnoreCase("remove")) {
					Utils.removeBlockedWord(args[1].toLowerCase());
					Utils.removeIgnoredWord(args[1].toLowerCase());
					sender.sendMessage("<lang>synergy-word-removed<arg>"+args[1].toLowerCase()+"</arg></lang>");
				}
			} else {
				sender.sendMessage("<lang>synergy-command-usage</lang> /chatfilter block/ignore/remove <word>");
			}
		}
		
		if (label.equalsIgnoreCase("chat")) {

			if (args.length == 0) {
				sender.sendMessage("<lang>synergy-command-usage</lang> /chat <"+String.join("/", getChats())+">");
				return true;
			}
			
			if (getChats().contains(args[0].toLowerCase())) {
				Synergy.getBread(((Player) sender).getUniqueId()).setData("chat", args[0].toLowerCase());
				sender.sendMessage("<lang>synergy-selected-chat<arg>"+args[0].toLowerCase()+"</arg></lang>");
				return true;
			}
		}
		
		if (label.equalsIgnoreCase("colors")) {
		    ConfigurationSection tags = Synergy.getConfig().getConfigurationSection("chat-manager.custom-color-tags");
	        List<String> colors = new ArrayList<>();
	        for (String t : tags.getKeys(false)) {
	        	String color = Synergy.getConfig().getString("chat-manager.custom-color-tags."+t);
	            colors.add(color+t.replace("&", "$")/*String.join(color, t.split(""))*/);
	        }
	        if (sender instanceof Player) {
	            Utils.sendFakeBook((Player) sender, "Colors", String.join("\n", colors));
	        } else {
	            sender.sendMessage("Only the player can execute this command.");
	        }
		}
		
		if (label.equalsIgnoreCase("emojis")) {
		    ConfigurationSection tags = Synergy.getConfig().getConfigurationSection("chat-manager.custom-emojis");
		    int count = 0;
		    StringBuilder messageBuilder = new StringBuilder();
		    int maxEmojiLength = 0;
		    for (String e : tags.getKeys(false)) {
		        String emoji = Synergy.getConfig().getString("chat-manager.custom-emojis."+e);
		        maxEmojiLength = Math.max(maxEmojiLength, e.length() + emoji.length() + 3);
		    }
		    for (String e : tags.getKeys(false)) {
		        if (count == 2) {
		            sender.sendMessage(messageBuilder.toString());
		            messageBuilder = new StringBuilder();
		            count = 0;
		        }
		        String emoji = Synergy.getConfig().getString("chat-manager.custom-emojis."+e);
		        int padding = maxEmojiLength - e.length() - emoji.length();
		        messageBuilder.append(String.format("<lang>primary</lang>%s - <lang>secondary</lang>%s%" + padding + "s", e, emoji, ""));
		        count++;
		    }
		    if (messageBuilder.length() > 0) {
		        sender.sendMessage(messageBuilder.toString());
		    }
		}

		return true;
	}


	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
		
		BreadMaker bread = Synergy.getBread(event.getPlayer().getUniqueId());

        if (!event.isCancelled()) { 
        	event.setCancelled(true);
        }
        
        if (!bread.isAuthenticated()) {
        	return;
        }
        
		if (bread.isMuted()) {
        	return;
		}

        if (removeChatTypeSymbol(event.getMessage()).isEmpty()) {
        	event.getPlayer().sendMessage("<lang>synergy-message-cant-be-empty</lang>");
        	return;
        }
        
        String chat = bread.getData("chat").isSet() ? bread.getData("chat").getAsString() : Synergy.getConfig().getBoolean("chat-manager.local-chat") ? "local" : "global";
        chat = getChatTypeFromMessage(event.getMessage()) == null ? chat : getChatTypeFromMessage(event.getMessage());
        
        Synergy.createSynergyEvent("chat").setPlayerUniqueId(event.getPlayer().getUniqueId()).setOption("player", event.getPlayer()
        		.getName()).setOption("message", event.getMessage()).setOption("chat", chat).send();
        
        Synergy.createSynergyEvent("discord").setPlayerUniqueId(event.getPlayer().getUniqueId()).setOption("player", event.getPlayer().getName())
            .setOption("message", Utils.stripColorTags(event.getMessage())).setOption("chat", chat).send();
        
        String botName = Synergy.getDiscord().getBotName();
        if (removeChatTypeSymbol(event.getMessage()).toLowerCase().startsWith(botName.toLowerCase()) && chat.equals("global")) {
            String question = Synergy.getConfig().getString("discord.gpt-bot.personality").replace("%MESSAGE%", Utils.removeIgnoringCase(botName, removeChatTypeSymbol(event.getMessage())));
            String answer = ((CompletionChoice)(new OpenAi()).newPrompt(question).get(0)).getText().replace("\"", "").trim();
            answer = answer.isEmpty() ? Translation.translate("<lang>synergy-service-unavailable</lang>", Translation.getDefaultLanguage()) : answer;
            Synergy.createSynergyEvent("chat").setOption("player", botName).setOption("message", answer).setOption("chat", "discord").send();
            Synergy.createSynergyEvent("discord").setOption("player", botName).setOption("message", answer).setOption("chat", "global").send();
        }
    }

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
	    List<String> commands = Arrays.asList("/reg", "/register", "/l", "/login");
	    if (!commands.stream().anyMatch(event.getMessage()::startsWith)) {
	        Synergy.getLogger().discord("```[cmd] " + event.getPlayer().getName() + ": " + event.getMessage() + "```");
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
        		bread.sendMessage(Translation.translate(event.getOption("message").getAsString(), bread.getLanguage()).replace("%ARGUMENT%", event.getOption("argument").getAsString()));
        	}
        }
        
        if (event.getIdentifier().equals("chat")) {
        	
        	UUID uuid = event.getPlayerUniqueId();
	        String chatType = event.getOption("chat").getAsString();
	        String format = getFormattedChatMessage(event);
            Player sender = Bukkit.getPlayer(uuid);
	        int count = 0;
	        
	        for (Player recipient: Bukkit.getOnlinePlayers()) {
	            switch (chatType) {
	                case "discord":
	                case "global":
	                    recipient.sendMessage(format);
	                    playMsgSound(recipient);
	                    count++;
	                    break;
	                case "plot":
	                    if (Synergy.isDependencyAvailable("PlotSquared") && Synergy.getConfig().getBoolean("chat-manager.local-chat-per-plotsquared-plot")) {
	                    	if (recipient == sender || (PlotSquaredAPI.getCurrentPlot(sender) != null && PlotSquaredAPI.getPlayersOnPlot(PlotSquaredAPI.getCurrentPlot(sender)).contains(recipient))) {
		                        recipient.sendMessage(format);
		                        playMsgSound(recipient);
			                    count++;
	                    	}
	                    }
	                    break;
	                case "local":
	                    if (sender != null && sender.getWorld() == recipient.getWorld() && sender.getLocation().distance(recipient.getLocation()) <= getLocalChatRadius()) {
	                        recipient.sendMessage(format);
	                        playMsgSound(recipient);
		                    count++;
	                    }
	                    break;
	                case "admin":
	                case "discord_admin":
	                    if ((chatType.equals("admin") && recipient.hasPermission("synergy.chat.admin")) || chatType.startsWith("discord")) {
	                        recipient.sendMessage(format);
	                        playMsgSound(recipient);
		                    count++;
	                    }
	                    break;
	            }
	        }
	        
	        if (count == 1 && Arrays.asList(new String[] {"local", "plot"}).contains(chatType)) {
	        	event.getBread().sendMessage("<lang>synergy-noone-hears-you</lang>");
	        }
	        logChatMessage(format);
        }

    }

    public String hideSynergyTranslationKeys(String string) {
    	for (Entry<String, String> k : LocalesManager.getLocales().get(Translation.getDefaultLanguage()).entrySet()) {
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

        message = hideSynergyTranslationKeys(message);
        message = removeChatTypeSymbol(message);
        message = message.replace("<lang>", "").replace("</lang>", "");
        message = message.replace("҉", "*");
        if (sender == null || !event.getBread().hasPermission("synergy.colors")) {
        	message = Utils.stripColorTags(message);
        }      
        message = Utils.translateSmiles(message);
        message = Utils.censorBlockedWords(message);
        message = Utils.removeRepetitiveCharacters(message);
        
        format = format.replace("%DISPLAYNAME%", displayname);
        format = format.replace("%MESSAGE%", message);
        format = format.replace("%CHATLETTER%", String.valueOf(chatType.charAt(0)).toUpperCase());
        format = format.replace("%CHAT%", chatType.toLowerCase());
        format = format.replace("%PLOT%", chatType.toLowerCase().equals("plot") ? "[plot] " : "");
        format = format.replace("%COLOR%", getChatColor(chatType));
        format = format.replace("%RANDOM%", String.valueOf(new Random().nextInt(99)));
   
        return format;
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
        return null;
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