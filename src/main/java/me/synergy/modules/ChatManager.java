package me.synergy.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import me.clip.placeholderapi.PlaceholderAPI;
import me.synergy.anotations.SynergyHandler;
import me.synergy.brains.Spigot;
import me.synergy.brains.Synergy;
import me.synergy.discord.Discord;
import me.synergy.events.SynergyEvent;
import me.synergy.integrations.PlotSquaredAPI;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.BookMessage;
import me.synergy.utils.Cooldown;
import me.synergy.utils.Translation;
import me.synergy.utils.Utils;

public class ChatManager implements Listener, CommandExecutor, TabCompleter {

    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());
        Synergy.getEventManager().registerEvents(this);
        
        if (Synergy.getConfig().getBoolean("chat-manager.enabled")) {
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
		List<String> chats = new ArrayList<>();
		if (Synergy.getConfig().getBoolean("chat-manager.local-chat")) {
			chats.add("local");
		} else {
			chats.add("global");
		}
		if (Synergy.getConfig().getBoolean("chat-manager.integrations.plotsquared-plot-chat")) {
			chats.add("plot");
		}
		if (Synergy.getConfig().getBoolean("chat-manager.integrations.factions-chat")) {
			chats.add("faction");
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
		    Map<String, Object> tags = Synergy.getConfig().getConfigurationSection("chat-manager.custom-color-tags");
	        List<String> colors = new ArrayList<>();
	        for (Entry<String, Object> t : tags.entrySet()) {
	        	String color = Synergy.getConfig().getString("chat-manager.custom-color-tags."+t.getKey());
	            colors.add(color+t.getKey().replace("&", "$")/*String.join(color, t.split(""))*/);
	        }
	        if (sender instanceof Player) {
	            BookMessage.sendFakeBook((Player) sender, "Colors", String.join("\n", colors));
	        } else {
	            sender.sendMessage("Only the player can execute this command.");
	        }
		}

		if (label.equalsIgnoreCase("emojis")) {
		    Set<String> tags = Synergy.getConfig().getConfigurationSection("chat-manager.custom-emojis").keySet();
		    int count = 0;
		    StringBuilder messageBuilder = new StringBuilder();
		    int maxEmojiLength = 0;
		    for (String e : tags) {
		        String emoji = Synergy.getConfig().getString("chat-manager.custom-emojis."+e);
		        maxEmojiLength = Math.max(maxEmojiLength, e.length() + emoji.length() + 3);
		    }
		    for (String e : tags) {
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

        boolean crossServerGlobalChat = Synergy.getConfig().getBoolean("chat-manager.cross-server-global-chat");
        boolean synergyChatEnabled = Synergy.getConfig().getBoolean("chat-manager.enabled");
		BreadMaker bread = Synergy.getBread(event.getPlayer().getUniqueId());
		Cooldown cooldown = Synergy.getCooldown(event.getPlayer().getUniqueId());
		String chat = synergyChatEnabled ? getChatTypeFromMessage(event.getMessage()) : "global";

		if (chat == null) {
		    chat = bread.getData("chat").isSet() ? bread.getData("chat").getAsString() 
		        : Synergy.getConfig().getBoolean("chat-manager.local-chat") ? "local" : "global";
		}

        if (!bread.isAuthenticated() || bread.isMuted()) {
        	return;
		}

        if (cooldown.hasCooldown("chat")) {
        	event.getPlayer().sendMessage("<lang>synergy-cooldown</lang>");
        	return;
        }
        
        if (removeChatTypeSymbol(event.getMessage()).isEmpty()) {
        	event.getPlayer().sendMessage("<lang>synergy-message-cant-be-empty</lang>");
        	return;
        }

        if (crossServerGlobalChat) {
	        Synergy.createSynergyEvent("discord").setPlayerUniqueId(event.getPlayer().getUniqueId()).setOption("player", event.getPlayer().getName())
	        .setOption("message", removeChatTypeSymbol(Utils.stripColorTags(event.getMessage()))).setOption("chat", chat).send();
        }

        if (!synergyChatEnabled) {
        	return;
        }
        
        cooldown.setCooldown("chat", 2);
        
        if (!event.isCancelled()) {
        	event.setCancelled(true);
        }
        
        SynergyEvent synergyEvent = Synergy.createSynergyEvent("chat").setPlayerUniqueId(event.getPlayer().getUniqueId()).setOption("player", event.getPlayer().getName())
	        	.setOption("message", event.getMessage()).setOption("chat", chat);
        
        if (crossServerGlobalChat) {
        	synergyEvent.send();
        } else {
        	synergyEvent.fireEvent();
        }
        
        String botName = Discord.getBotName();
        if (removeChatTypeSymbol(event.getMessage()).toLowerCase().startsWith(botName.toLowerCase()) && chat.equals("global")) {
            String question = Synergy.getConfig().getString("discord.gpt-bot.personality").replace("%MESSAGE%", Utils.removeIgnoringCase(botName, removeChatTypeSymbol(event.getMessage())));
            String answer = (new OpenAi()).newPrompt(question).get(0).getText().replace("\"", "").trim();
            answer = answer.isEmpty() ? Translation.translate("<lang>synergy-service-unavailable</lang>", Translation.getDefaultLanguage()) : answer;
            Synergy.createSynergyEvent("chat").setOption("player", botName).setOption("message", answer).setOption("chat", "discord").send();
            Synergy.createSynergyEvent("discord").setOption("player", botName).setOption("message", answer).setOption("chat", "global").send();
        }
        
        Bukkit.getLogger().info("[" + chat + "] " + bread.getName() + ": " + event.getMessage());
        Synergy.getLogger().discord("```["+Synergy.getServerName()+"] [" + chat + "] " + bread.getName() + ": " + event.getMessage() + "```");
    }

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Cooldown cooldown = Synergy.getCooldown(event.getPlayer().getUniqueId());
		if (!cooldown.hasCooldown("command")) {
		    List<String> commands = Arrays.asList("/reg", "/register", "/l", "/login");
		    if (!commands.stream().anyMatch(event.getMessage()::startsWith)) {
		        Synergy.getLogger().discord("```["+Synergy.getServerName()+"] [cmd] " + event.getPlayer().getName() + ": " + event.getMessage() + "```");
		    }
		}
	}

    @SynergyHandler
    public void onSynergyPluginMessage(SynergyEvent event) {

        if (event.getIdentifier().equals("system-chat")) {
        	if (Bukkit.getPlayer(event.getPlayerUniqueId()) != null) {
        		Bukkit.getPlayer(event.getPlayerUniqueId()).sendMessage(event.getOption("message").getAsString());
        	}
        }
        if (event.getIdentifier().equals("broadcast")) {
        	for (Player p : Bukkit.getOnlinePlayers()) {
        		BreadMaker bread = Synergy.getBread(p.getUniqueId());
        		bread.sendMessage(Translation.translate(event.getOption("message").getAsString(), bread.getLanguage()));
        	}
        }

        if (event.getIdentifier().equals("chat")) {

        	UUID uuid = event.getPlayerUniqueId();
	        String chat = event.getOption("chat").getAsString();
	        String format = getFormattedChatMessage(event);
            Player sender = Bukkit.getPlayer(uuid);
	        int count = 0;

	        for (Player recipient: Bukkit.getOnlinePlayers()) {
	            switch (chat) {
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
	                    if ((chat.equals("admin") && recipient.hasPermission("synergy.chat.admin")) || chat.startsWith("discord")) {
	                        recipient.sendMessage(format);
	                        playMsgSound(recipient);
		                    count++;
	                    }
	                    break;
	               
	                case "faction":
	                	if (Synergy.isDependencyAvailable("PlaceholderAPI") && PlaceholderAPI.setPlaceholders(recipient, "%faction_name%").equals(PlaceholderAPI.setPlaceholders(sender, "%faction_name%"))) {
	                		recipient.sendMessage(format);
	                        playMsgSound(recipient);
		                    count++;
	                	}
	            }
	        }

	        if (count == 1 && Arrays.asList(new String[] {"local", "plot"}).contains(chat)) {
	        	event.getBread().sendMessage("<lang>synergy-noone-hears-you</lang>");
	        }
        }
    }

    public String hideSynergyTranslationKeys(String string) {
    	for (Entry<String, String> k : LocalesManager.getLocales().get(Translation.getDefaultLanguage()).entrySet()) {
    		string = string.replace(k.getKey(), k.getKey().replace("-", "–"));
    	}
    	string = string.replace("<lang>", "").replace("</lang>", "");
		return string;
	}

	private String getFormattedChatMessage(SynergyEvent event) {
        OfflinePlayer sender = Spigot.getInstance().getOfflinePlayerByUniqueId(event.getPlayerUniqueId());
        String format = getFormat();
        String chatType = event.getOption("chat").getAsString();
        String message = event.getOption("message").getAsString();
        String displayname = sender.getName() != null ? sender.getName() : event.getOption("player").getAsString();

        if (Synergy.isDependencyAvailable("PlaceholderAPI")) {
        	format = Utils.replacePlaceholderOutputs(sender, format);
            format = PlaceholderAPI.setPlaceholders(sender, format);
        }

        message = hideSynergyTranslationKeys(message);
        message = removeChatTypeSymbol(message);
        if (sender == null || !event.getBread().hasPermission("synergy.colors")) {
        	message = Utils.stripColorTags(message);
        }
        message = Utils.translateSmiles(message);
        message = Utils.censorBlockedWords(message);
        message = Utils.removeRepetitiveCharacters(message);

        format = format.replace("%DISPLAYNAME%", displayname);
        format = format.replace("%MESSAGE%", message);
        format = format.replace("%CHAT%", getChatTag(chatType.toLowerCase()));
        format = format.replace("%COLOR%", getChatColor(chatType));
        format = format.replace("%RANDOM%", String.valueOf(new Random().nextInt(99)));

        return format;
    }

    private String getChatTag(String chatType) {
        return Synergy.getConfig().getString("chat-manager.chat-tag." + chatType, chatType);
	}

	public void playMsgSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
    }

    private String getChatColor(String chatType) {
        return Synergy.getConfig().getString("chat-manager.colors." + chatType);
    }

    private int getLocalChatRadius() {
        return Synergy.getConfig().getInt("chat-manager.local-chat-radius");
    }

    public String getChatTypeFromMessage(String message) {
        switch (String.valueOf(message.charAt(0))) {
            case "!":
                return "global";
            case "\\":
                return "admin";
            case "=":
                return "faction";
        }
        return null;
    }

    public String removeChatTypeSymbol(String message) {
        if (message.length() > 0 && Arrays.asList(new String[] {"!", "\\", "="}).contains(String.valueOf(message.charAt(0)))) {
        	return message.substring(1);
        }
        return message;
    }

    private String getFormat() {
        return Synergy.getConfig().getString("chat-manager.format");
    }

}