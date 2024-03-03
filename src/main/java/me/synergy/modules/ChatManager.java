package me.synergy.modules;

import java.awt.Color;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.clip.placeholderapi.PlaceholderAPI;
import me.synergy.brain.BrainSpigot;
import me.synergy.events.SynergyPluginMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

public class ChatManager implements Listener {

    private BrainSpigot spigot;
	private Player player;

    public ChatManager(BrainSpigot spigot) {
        this.spigot = spigot;
    }

	public void register() {
		if (spigot.getConfig().getBoolean("chat-manager.enabled")) {
			Bukkit.getPluginManager().registerEvents(new ChatManager(spigot), spigot);
			spigot.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
		}
	}
	
	
    @EventHandler
    public void getMessage(SynergyPluginMessage e) {
        if (!e.getIdentifier().equals("chat")) {
            return;
        }

        String player = e.getArgs()[0];
        String message = e.getArgs()[1];
        
		String format = getFormat();
		
		if (spigot.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			format = PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(player), format);
		}
		
		format = format.replace("%DISPLAYNAME%", player);
		format = format.replace("%MESSAGE%", removeChatSymbol(message));
		format = format.replace("%CHAT%", String.valueOf(getChat(message).charAt(0)).toUpperCase());
		format = format.replace("%COLOR%", spigot.getConfig().getString("chat-manager.colors."+getChat(message)+"-chat"));
		
		format = ChatColor.translateAlternateColorCodes('&', format);
        
    	for (Player p : Bukkit.getOnlinePlayers()) {
    		switch (getChat(message)) {
    			case "global":
    				p.sendMessage(format);
    				break;
    			case "local":
    				if (Bukkit.getPlayer(player).getLocation().distance(p.getLocation()) <= spigot.getConfig().getInt("chat-manager.local-chat-radius")) {
    					p.sendMessage(format);
    				}
    				break;
    			case "admin":
    				if (p.hasPermission("synergy.adminchat")) {
    					p.sendMessage(format);
    				}
    				break;
				case "discord":
					p.sendMessage(format);
					break;
    			case "discord_admin":
    				if (p.hasPermission("synergy.adminchat")) {
    					p.sendMessage(format);
    				}
    				break;
    		}
    	}
    }
	

	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		player = event.getPlayer();
		
		if (!event.isCancelled()) {
			event.setCancelled(true);
			
    		SynergyPluginMessage spm = new SynergyPluginMessage("chat");
	    	spm.setArguments(new String[] {event.getPlayer().getName(), event.getMessage()}).send(spigot);
			
			JDA jda = spigot.getJda();
			if (jda != null) {
				//jda.getTextChannelById(spigot.getConfig().getString("discord.chats.log-chat")).sendMessage("```["+chat+"] "+playername+": "+chatmessage+"```").queue();
				EmbedBuilder builder = new EmbedBuilder();
				builder.setAuthor(event.getPlayer().getDisplayName(), null, "https://minotar.net/helm/" + event.getPlayer().getDisplayName());
				builder.setTitle(event.getMessage(), null);
				if (getChat(event.getMessage()).equals("global")) {
					builder.setColor(Color.decode("#f1c40f"));
					jda.getTextChannelById(spigot.getConfig().getString("discord.global-chat-channel")).sendMessageEmbeds(builder.build()).queue();
				}
				if (getChat(event.getMessage()).equals("admin") && player.hasPermission("synergy.adminchat")) {
					builder.setColor(Color.decode("#e74c3c"));
					jda.getTextChannelById(spigot.getConfig().getString("discord.admin-chat-channel")).sendMessageEmbeds(builder.build()).queue();
				}
			}
		}
	}
	
	private String getChat(String message) {
		switch (String.valueOf(message.charAt(0))) {
			case "!":
				return "global";
			case "\\":
				return player.hasPermission("synergy.adminchat") ? "admin" : "local";
			case "$":
				return "discord_admin";
			case "@":
				return "discord";
		}
		return spigot.getConfig().getBoolean("chat-manager.global-chat") ? "local" : "global";
	}
	
	private String removeChatSymbol(String message) {

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
