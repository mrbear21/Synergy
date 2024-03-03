package me.synergy.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import com.theokanning.openai.messages.Message.MessageBuilder;

import me.synergy.brain.BrainSpigot;
import me.synergy.brain.BrainVelocity;
import me.synergy.events.SynergyPluginMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Discord extends ListenerAdapter {

	private BrainVelocity bungee;
	private BrainSpigot spigot;

	private JDA jda;
	
	public Discord(BrainVelocity bungee) {
		this.bungee = bungee;
	}
	
	public Discord(BrainSpigot spigot) {
		this.spigot = spigot;
	}
	
	private final static GatewayIntent[] INTENTS = {GatewayIntent.SCHEDULED_EVENTS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES};
    
	public void register() {

		boolean enabled = false;
		
		if (bungee != null && bungee.getConfig().getBoolean("discord.enabled")) {
			enabled = true;
		}
			
		if (spigot != null && spigot.getConfig().getBoolean("discord.enabled")) {
			enabled = true;
		}
		
		if (enabled) {

			String token = bungee != null ? bungee.getConfig().getString("discord.bot-token") : spigot.getConfig().getString("discord.bot-token");
	
			if (token == null || token.equals("token")) {
				return;
			}
	
	        jda = JDABuilder.create(token, Arrays.asList(INTENTS))
			.enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
			.disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.ONLINE_STATUS, CacheFlag.STICKER)
			.setStatus(OnlineStatus.ONLINE)
	        .setMemberCachePolicy(MemberCachePolicy.ALL)
			.addEventListeners(bungee != null ? new Discord(bungee) : new Discord(spigot))
			.setBulkDeleteSplittingEnabled(true)
			.build();
			
			if (spigot != null) {
				spigot.jda = jda;
				spigot.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
			}
			
			if (bungee != null) {
				bungee.jda = jda;
				bungee.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
			} 
		
		    CommandListUpdateAction commands = jda.updateCommands();
	
		    commands.addCommands(
			        Commands.slash("погроза", "Кинути користувачу погрозу в приватні повідомлення.")
			            .addOptions(new OptionData(OptionType.USER, "user", "жертва")
			                .setRequired(true))
			            .setGuildOnly(true)
			            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
			);
		    
		    
		    commands.addCommands(
			        Commands.slash("post", "Створити новий пост.")
			        	.addOptions(new OptionData(OptionType.STRING, "title", "заголовок").setRequired(true))
			            .addOptions(new OptionData(OptionType.STRING, "text", "текст").setRequired(true))
		        		.addOptions(new OptionData(OptionType.STRING, "author", "автор"))
			            .addOptions(new OptionData(OptionType.CHANNEL, "channel", "канал"))
			            .addOptions(new OptionData(OptionType.STRING, "image", "url на картинку"))
			            .addOptions(new OptionData(OptionType.STRING, "color", "#колір"))
			            .addOptions(new OptionData(OptionType.STRING, "thumbnail", "url на картинку"))
			            .addOptions(new OptionData(OptionType.MENTIONABLE, "mention", "згадка"))
			            .addOptions(new OptionData(OptionType.STRING, "edit", "айді на повідомлення (редагувати вже відправлене повідомлення)"))
			            .setGuildOnly(true)
			            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
			);
		    
		    
		    commands.addCommands(
			        Commands.slash("list", "Список хліборобів онлайн")
			            .setGuildOnly(true)
			);
		    
		    commands.addCommands(
		        Commands.slash("say", "Бот напише те, що ви йому скажете").addOption(OptionType.STRING, "content", "Що бот повинен сказати", true)
		    );
	
		    commands.addCommands(
		        Commands.slash("prune", "Очистити повідомлення у цьому каналі")
		            .addOption(OptionType.INTEGER, "amount", "Скільки повідомлень очистити (за замовчуванням 100)")
		            .setGuildOnly(true)
		            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
		    );
		
		    commands.queue();

		}
		
	}
		
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
	    if (event.getGuild() == null)
	        return;
	    switch (event.getName())
	    {
	    case "погроза":
			event.reply("Погрозу відіслано!").queue();
	        User user = event.getOption("user").getAsUser();
			погроза(user);
			break;
	    case "say":
	    	event.reply(event.getOption("content").getAsString()).queue();
	        break;
	    case "prune":
	        prune(event);
	        break;
	    case "post":
	        post(event);
	        break;
	    case "list":
	        list(event);
	        break;
	    default:
	        event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
	    }
		
	}

	String list = "";
	private void list(SlashCommandInteractionEvent event) {
		/*
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Хлібороби онлайн:");
		list = "";

        bungee.getProxy().getAllServers().forEach((server) -> {
		    list = list + "\n**" + server.getServerInfo().getName() + ": **";
		    if (server.getPlayersConnected().size() > 0) {
		        List<String> players = new ArrayList<>();
		        server.getPlayersConnected().forEach(player -> players.add(player.getUsername()));
		        list = list + String.join(", ", players);
		    } else {
		        list = list + "ні душі";
		    }
		});
		builder.setDescription(list);
		builder.setColor(Color.decode("#a29bfe"));
		MessageBuilder message = new MessageBuilder();
		message.setEmbeds(builder.build());
		event.reply(message.build()).queue();
		*/
	}

	private void post(SlashCommandInteractionEvent event) {
		/*
		String title = event.getOption("title").getAsString();
		String text = event.getOption("text").getAsString().replace("\\n", System.lineSeparator());
		String author = event.getOption("author") != null ? event.getOption("author").getAsString() : null;
		TextChannel channel = event.getOption("channel") != null ? event.getOption("channel").getAsTextChannel() : event.getTextChannel();
		String image = event.getOption("image") != null ? event.getOption("image").getAsString() : null;
		String thumbnail = event.getOption("thumbnail") != null ? event.getOption("thumbnail").getAsString() : null;
		String color = event.getOption("color") != null ? event.getOption("color").getAsString() : "#a29bfe";
		String edit = event.getOption("edit") != null ? event.getOption("edit").getAsString() : null;
		IMentionable mention = event.getOption("mention") != null ? event.getOption("mention").getAsMentionable() : null;
		
		EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor(author, null, "https://minotar.net/helm/"+author);
			builder.setTitle(title);
			builder.setDescription(text);
			builder.setThumbnail(thumbnail);
			builder.setColor(Color.decode(color));
			builder.setImage(image);
		MessageBuilder message = new MessageBuilder();
		
		if (mention != null) {
			message.append(mention).setEmbeds(builder.build());
		} else {
			message.setEmbeds(builder.build());
		}
		
		if (edit == null) {
			channel.sendMessage(message.build()).queue();
		} else {
			channel.retrieveMessageById(edit).complete().editMessage(message.build()).queue();
		}
    	event.reply("Пост опубліковано!").queue();
    	*/
	}


	public void погроза(User user) {
		user.openPrivateChannel().complete().sendMessage("https://cdn.discordapp.com/attachments/994920082927013989/994920564953198655/download.jpg").queue();
        
	}
	
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
	    String[] id = event.getComponentId().split(":");
	    String authorId = id[0];
	    String type = id[1];
	    if (!authorId.equals(event.getUser().getId()))
	        return;
	    event.deferEdit().queue();
	
	    MessageChannel channel = event.getChannel();
	    switch (type)
	    {
	        case "prune":
	            int amount = Integer.parseInt(id[2]);
	            event.getChannel().getIterableHistory()
	                .skipTo(event.getMessageIdLong())
	                .takeAsync(amount)
	                .thenAccept(channel::purgeMessages);
	        case "delete":
	            event.getHook().deleteOriginal().queue();
	    }
	}
	
	public void prune(SlashCommandInteractionEvent event) {
	    OptionMapping amountOption = event.getOption("amount");
	    int amount = amountOption == null ? 100 : (int) Math.min(200, Math.max(2, amountOption.getAsLong()));
	    String userId = event.getUser().getId();
	    event.reply("Це видалить " + amount + " повідомлень.\nВи впевнені?")
	        .addActionRow(
	            Button.secondary(userId + ":delete", "Ні!"),
	            Button.danger(userId + ":prune:" + amount, "Так!"))
	        .queue();
	}

    @EventHandler
    public void getMessage(SynergyPluginMessage e) {
        if (!e.getIdentifier().equals("discord")) {
            return;
        }
        for (String s : e.getArgs()) {
        	Bukkit.broadcastMessage(s);
        }
    }
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		Member memder = event.getMember();
		
		if (!event.getAuthor().isBot()) {
			
			String channelId = event.getChannel().getId();
			
			if (channelId.equals(spigot.getConfig().getString("discord.global-chat-channel"))) {
				SynergyPluginMessage spm = new SynergyPluginMessage("chat");
		    	spm.setArguments(new String[] {memder.getNickname(), "@"+message.getContentDisplay()}).send(spigot);
			}
			
			if (channelId.equals(spigot.getConfig().getString("discord.admin-chat-channel"))) {
				SynergyPluginMessage spm = new SynergyPluginMessage("chat");
		    	spm.setArguments(new String[] {memder.getNickname(), "$"+message.getContentDisplay()}).send(spigot);
			}
			

			
			return;
		}
		
	}
	
}