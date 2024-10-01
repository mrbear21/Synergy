package me.synergy.bungee.handlers;

import java.awt.Color;

import me.synergy.brains.Bungee;
import me.synergy.brains.Synergy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerJoinListener implements Listener {

	public void initialize() {
		Bungee.getInstance().getProxy().getPluginManager().registerListener(Bungee.getInstance(), this);
        Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
	}
	
    @EventHandler
    public void onPlayerLogin(PostLoginEvent event) {
        if (!Synergy.getConfig().getBoolean("discord.enabled")) {
            return;
        }

    	ProxiedPlayer player = event.getPlayer();
    	String channel = Synergy.getConfig().getString("discord.channels.global-chat-channel");
    	if (player.getDisplayName().equals("mrbear25") ) {return;}
    	EmbedBuilder embed = new EmbedBuilder();
    	embed.setColor(Color.decode("#81ecec"));
    	embed.setAuthor(player.getDisplayName()+" приєднався", null, Synergy.getConfig().getString("discord.avatar-link")+player.getDisplayName());

    	Synergy.getDiscord().getTextChannelById(channel).sendMessageEmbeds(embed.build()).queue();
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        if (!Synergy.getConfig().getBoolean("discord.enabled")) {
            return;
        }

    	ProxiedPlayer player = event.getPlayer();
    	String channel = Synergy.getConfig().getString("discord.channels.global-chat-channel");
    	if (player.getDisplayName().equals("mrbear25") ) {return;}
    	EmbedBuilder embed = new EmbedBuilder();
    	embed.setColor(Color.decode("#fab1a0"));
    	embed.setAuthor(player.getDisplayName()+" відключився", null, Synergy.getConfig().getString("discord.avatar-link")+player.getDisplayName());
    	Synergy.getDiscord().getTextChannelById(channel).sendMessageEmbeds(embed.build()).queue();
    }
	
}
