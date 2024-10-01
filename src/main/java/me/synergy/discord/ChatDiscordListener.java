package me.synergy.discord;

import java.awt.Color;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.synergy.anotations.SynergyHandler;
import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;
import me.synergy.modules.OpenAi;
import me.synergy.utils.Translation;
import me.synergy.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChatDiscordListener extends ListenerAdapter {

	public ChatDiscordListener() {
        try {
	        if (!Synergy.getConfig().getBoolean("discord.enabled")) {
	            return;
	        }
	        
	        Synergy.getEventManager().registerEvents(this);

	        Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
	    } catch (Exception exception) {
	        Synergy.getLogger().error(String.valueOf(getClass().getSimpleName()) + " module failed to initialize: " + exception.getMessage());
	    	exception.printStackTrace();
	    }
	}
	
    @SynergyHandler
    public void onSynergyEvent(SynergyEvent event) {
    	
    	if (event.getIdentifier().equals("discord-log")) {
	        String logchat = Synergy.getConfig().getString("discord.channels.log");
            if (logchat.length() == 19) {
            	Synergy.getDiscord().getTextChannelById(logchat).sendMessage(event.getOption("message").getAsString()).queue();
            }
    	}
    	
        if (event.getIdentifier().equals("discord-broadcast")) {
        	String announcementschat = Synergy.getConfig().getString("discord.channels.broadcast");
        	if (announcementschat.length() == 19) {
        		MessageEmbed announcement = Discord.info(Synergy.translate(event.getOption("message").getAsString(), Translation.getDefaultLanguage()).getStripped());
        		if (Synergy.getConfig().getBoolean("discord.channels.merge-similar-embeds")) {
	        		try {
	        			Message message = Synergy.getDiscord().getTextChannelById(announcementschat).retrieveMessageById(Synergy.getDiscord().getTextChannelById(announcementschat).getLatestMessageId()).complete();
	        			if (ChronoUnit.MILLIS.between(message.getTimeCreated().toInstant(), Instant.now()) < 5 * 60 * 1000) {
	        				boolean containsRelevantText = message.getEmbeds().stream().anyMatch(e -> e.getTitle().contains(announcement.getTitle()));
	        				if (containsRelevantText) {
	        					List<MessageEmbed> embeds = new ArrayList<>(message.getEmbeds());
	    		                embeds.add(announcement);
	    		                message.editMessageEmbeds(embeds).queue();
	    		                return;
	        		        }
	        		    }
	        		} catch (Exception c) {}
        		}
        		Synergy.getDiscord().getTextChannelById(announcementschat).sendMessageEmbeds(announcement).queue();
        	 }
        }
        
        if (event.getIdentifier().equals("discord")) {
        	
	        String displayname = event.getOption("player").getAsString();
	        String message = event.getOption("message").getAsString();
	        String chat = event.getOption("chat").getAsString();

	        String globalchat = Synergy.getConfig().getString("discord.channels.global");
	        @SuppressWarnings("unused")
			String adminchat = Synergy.getConfig().getString("discord.channels.admin");

            String[] messageParts = Utils.splitMessage(message);
            for (String part: messageParts) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setAuthor(displayname, null, Discord.getBotName().equals(displayname) ? Synergy.getDiscord().getSelfUser().getAvatarUrl() : (Synergy.getConfig().getString("discord.avatar-link") + displayname));
                builder.setTitle(part, null);
                if (globalchat.length() == 19 && chat.equals("global")) {
                    builder.setColor(Color.decode("#f1c40f"));
                    Synergy.getDiscord().getTextChannelById(globalchat).sendMessageEmbeds(builder.build()).queue();
                }/*
                if (adminchat.length() == 19 && chat.equals("admin") && Bukkit.getPlayer(event.getPlayerUniqueId()).hasPermission("synergy.adminchat")) {
                    builder.setColor(Color.decode("#e74c3c"));
                    Synergy.getDiscord().getJda().getTextChannelById(adminchat).sendMessageEmbeds(builder.build()).queue();
                }*/
            }
        }
    }
	
    @Override
	public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        User user = event.getAuthor();
        String channelId = event.getChannel().getId();

        UUID uuid = Discord.getUniqueIdByDiscordId(user.getId());

        if (!event.getAuthor().isBot()) {

            if (channelId.equals(Synergy.getConfig().getString("discord.channels.global"))) {
                if (uuid == null) {
                	event.getChannel().sendMessageEmbeds(Discord.warning(Synergy.translate("<lang>synergy-you-have-to-link-account</lang>", Translation.getDefaultLanguage()).getStripped())).queue();
                    return;
                }
                if (Synergy.getBread(uuid).isMuted()) {
                	event.getMessage().replyEmbeds(Discord.warning(Synergy.translate("<lang>synergy-you-are-muted</lang>", Translation.getDefaultLanguage()).getStripped())).queue();
                	return;
                }

	            Synergy.createSynergyEvent("chat").setPlayerUniqueId(uuid).setOption("player", event.getAuthor().getEffectiveName())
	            	.setOption("chat", "discord").setOption("message", message.getContentDisplay()).send();

            }

            if (channelId.equals(Synergy.getConfig().getString("discord.channels.admin"))) {
                if (uuid == null) {
                	event.getChannel().sendMessageEmbeds(Discord.warning(Synergy.translate("<lang>synergy-you-have-to-link-account</lang>", Translation.getDefaultLanguage()).getStripped())).queue();
                    return;
                }
                Synergy.createSynergyEvent("chat").setPlayerUniqueId(uuid).setOption("player", event.getAuthor().getEffectiveName())
	            	.setOption("chat", "discord_admin").setOption("message", message.getContentDisplay()).send();
            }

            if (Synergy.getConfig().getBoolean("discord.gpt-bot.enabled")) {

                try {
                    boolean startsWithBotName = message.getContentDisplay().toLowerCase().startsWith(Discord.getBotName().toLowerCase());
                    boolean isGlobalChatChannel = channelId.equals(Synergy.getConfig().getString("discord.channels.global"));
                    boolean mentionedBot = message.getMentions().isMentioned((IMentionable) event.getJDA().getSelfUser(), Message.MentionType.USER);
                    boolean isReplyToBot = message.getReferencedMessage() != null && message.getReferencedMessage().getAuthor().equals(event.getJDA().getSelfUser());

                    if ((startsWithBotName && !isGlobalChatChannel) || mentionedBot || isReplyToBot) {
                        message.getChannel().sendTyping().queue();
                        String question = Synergy.getConfig().getString("discord.gpt-bot.personality")
                            .replace("%MESSAGE%", Utils.removeIgnoringCase(Discord.getBotName(), event.getMessage().getContentRaw())
                                .replace(event.getJDA().getSelfUser().getAsMention(), ""));
                        //String.valueOf((event.getMessage().getReferencedMessage() != null) ? event.getMessage().getReferencedMessage().getContentRaw() : "");
                        String answer = new OpenAi().newPrompt(question).get(0).getText().replace("\"", "");
                        message.reply(answer).queue();
                    }

                    if (startsWithBotName && isGlobalChatChannel) {
                        String question = Synergy.getConfig().getString("discord.gpt-bot.personality").replace("%MESSAGE%", Utils.removeIgnoringCase(Discord.getBotName(), message.getContentDisplay()));
                        String answer = (new OpenAi().newPrompt(question).get(0)).getText().replace("\"", "").trim();
                        Synergy.createSynergyEvent("chat").setOption("player", Discord.getBotName())
                    		.setOption("chat", "discord").setOption("message", answer).send();
                        Synergy.createSynergyEvent("discord").setOption("player", Discord.getBotName())
	                        .setOption("chat", "global").setOption("message", answer).send();
                    }

                } catch (Exception c) {
                    Synergy.getLogger().error(c.getMessage());
                    event.getMessage().replyEmbeds(Discord.warning(Synergy.translate("<lang>synergy-service-unavailable</lang>", Translation.getDefaultLanguage()).getStripped())).queue();
                }
            }

            if (Synergy.getConfig().getBoolean("discord.hightlights.enabled") &&
                Synergy.getConfig().getStringList("discord.hightlights.channels").contains(channelId) &&
                message.getAttachments().size() > 0) {
                message.addReaction(Emoji.fromUnicode(Synergy.getConfig().getString("discord.hightlights.reaction-emoji"))).complete();
                message.createThreadChannel(Synergy.translate("<lang>synergy-hightlights-comments</lang>", Translation.getDefaultLanguage()).getStripped()).queue();
            }
            return;
        }
    }
}
