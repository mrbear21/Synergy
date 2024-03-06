package me.synergy.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.theokanning.openai.completion.CompletionChoice;

import me.synergy.brains.Spigot;
import me.synergy.brains.Velocity;
import me.synergy.brains.Synergy;
import me.synergy.events.SynergyPluginMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Discord extends ListenerAdapter implements Listener {

	private Velocity bungee;
	private Spigot spigot;

	private JDA jda;
	
	public Discord(Velocity bungee) {
		this.bungee = bungee;
	}
	
	public Discord(Spigot spigot) {
		this.spigot = spigot;
	}
	
	private final static GatewayIntent[] INTENTS = {GatewayIntent.SCHEDULED_EVENTS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES};
    
	public void initialize() {
		try {

			if (Synergy.isRunningSpigot()) {
				Bukkit.getPluginManager().registerEvents(new Discord(spigot), spigot);
			}
			
			if (!Synergy.getConfig().getBoolean("discord.enabled")) {
				return;
			}

			String token = Synergy.getConfig().getString("discord.bot-token");
	
	        jda = JDABuilder.create(token, Arrays.asList(INTENTS))
			.enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
			.disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.ONLINE_STATUS, CacheFlag.STICKER)
			.setStatus(OnlineStatus.ONLINE)
	        .setMemberCachePolicy(MemberCachePolicy.ALL)
			.addEventListeners(this)
			.setBulkDeleteSplittingEnabled(true)
			.build();
			
			if (Synergy.isRunningSpigot()) {
				spigot.jda = jda;
				spigot.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
			}
			
			if (Synergy.isRunningVelocity()) {
				bungee.jda = jda;
				bungee.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
			} 
		
		    CommandListUpdateAction commands = jda.updateCommands();

		    commands.addCommands(
			        Commands.slash("post", Synergy.translateString("synergy-create-post"))
			        	.addOptions(new OptionData(OptionType.STRING, "title", "Title").setRequired(true))
			            .addOptions(new OptionData(OptionType.STRING, "text", "Text").setRequired(true))
		        		.addOptions(new OptionData(OptionType.STRING, "author", "Author"))
			            .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Channel"))
			            .addOptions(new OptionData(OptionType.STRING, "image", "Image url"))
			            .addOptions(new OptionData(OptionType.STRING, "color", "#Color"))
			            .addOptions(new OptionData(OptionType.STRING, "thumbnail", "Image url"))
			            .addOptions(new OptionData(OptionType.MENTIONABLE, "mention", "Mention"))
			            .addOptions(new OptionData(OptionType.STRING, "edit", "Message ID (edit a message that has already been sent)"))
			            .setGuildOnly(true)
			            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
			);
		    
		    commands.addCommands(
			        Commands.slash("list", Synergy.translateString("synergy-online-players-list"))
			            .setGuildOnly(true)
			);
		    
		    commands.addCommands(
			        Commands.slash("link", Synergy.translateString("synergy-link-minecraft"))
			            .setGuildOnly(true)
			);
		    
		    commands.addCommands(
		        Commands.slash("prune", Synergy.translateString("synergy-prune-messages"))
		            .addOption(OptionType.INTEGER, "amount", "Amount")
		            .setGuildOnly(true)
		            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
		    );
		
		    commands.queue();
		} catch (Exception c) {
			
		}
		
	}
		
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
	    if (event.getGuild() == null)
	        return;
	    switch (event.getName())
	    {
	    case "prune":
	        prune(event);
	        break;
	    case "post":
	        post(event);
	        break;
	    case "list":
	        list(event);
	        break;
	    case "link":
	    	link(event);
	        break;
	    default:
	        event.reply(Synergy.translateString("synergy-service-unavailable")).setEphemeral(true).queue();
	    }
		
	}

	private void link(SlashCommandInteractionEvent event) {
        TextInput subject = TextInput.create("username", Synergy.translateString("synergy-link-minecraft-your-username"), TextInputStyle.SHORT)
                .setPlaceholder("Steve")
                .setMinLength(3)
                .setMaxLength(28)
                .build();
        Modal modal = Modal.create("minecraftlink", Synergy.translateString("synergy-link-minecraft"))
                .addComponents(ActionRow.of(subject))
                .build();
        event.replyModal(modal).queue();
	}
	
    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (event.getModalId().equals("minecraftlink")) {
            String subject = event.getValue("username").getAsString();
            spigot.log(subject);
            event.reply(Synergy.translateString("synergy-link-minecraft-confirmation")).setEphemeral(true).queue();
        }
    }

	String list = "";
	private void list(SlashCommandInteractionEvent event) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(Synergy.translateString("synergy-online-players-list"));
		list = "";
		if (Synergy.isRunningVelocity()) {
	    /*    bungee.getProxy().getAllServers().forEach((server) -> {
			    list = list + "\n**" + server.getServerInfo().getName() + ": **";
			    if (server.getPlayersConnected().size() > 0) {
			        List<String> players = new ArrayList<>();
			        server.getPlayersConnected().forEach(player -> players.add(player.getUsername()));
			        list = list + String.join(", ", players);
			    } else {
			        list = list + "ні душі";
			    }
			});*/
		} else {
		    list = list + "\n**" + Synergy.getServerName() + ": **";
	        List<String> players = new ArrayList<>();
	        Bukkit.getOnlinePlayers().forEach(player -> players.add(player.getName()));
	        list = list + String.join(", ", players);
		}
		builder.setDescription(list);
		builder.setColor(Color.decode("#a29bfe"));
		event.replyEmbeds(builder.build()).queue();
	}

	private void post(SlashCommandInteractionEvent event) {
		
		String title = event.getOption("title").getAsString();
		String text = event.getOption("text").getAsString().replace("\\n", System.lineSeparator());
		String author = event.getOption("author") != null ? event.getOption("author").getAsString() : null;
		TextChannel channel = event.getOption("channel") != null ? event.getOption("channel").getAsChannel().asTextChannel() : event.getChannel().asTextChannel();
		String image = event.getOption("image") != null ? event.getOption("image").getAsString() : null;
		String thumbnail = event.getOption("thumbnail") != null ? event.getOption("thumbnail").getAsString() : null;
		String color = event.getOption("color") != null ? event.getOption("color").getAsString() : "#a29bfe";
		String edit = event.getOption("edit") != null ? event.getOption("edit").getAsString() : null;
		//IMentionable mention = event.getOption("mention") != null ? event.getOption("mention").getAsMentionable() : null;
		
		EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor(author, null, "https://minotar.net/helm/"+author);
			builder.setTitle(title);
			builder.setDescription(text);
			builder.setThumbnail(thumbnail);
			builder.setColor(Color.decode(color));
			builder.setImage(image);
		if (edit == null) {
			channel.sendMessageEmbeds(builder.build()).queue();
		} else {
			channel.retrieveMessageById(edit).complete().editMessageEmbeds(builder.build()).queue();
		}
    	event.reply("Published!").setEphemeral(true).queue();
    	
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
	    event.reply(Synergy.translateString("synergy-prune-messages-confirmation").replace("%AMOUNT%", String.valueOf(amount)))
	        .addActionRow(
	            Button.secondary(userId + ":delete", "No"),
	            Button.danger(userId + ":prune:" + amount, "Yes"))
	        .queue();
	}

    @EventHandler
    public void getMessage(SynergyPluginMessage e) {
        if (!e.getIdentifier().equals("discord")) {
            return;
        }
        String player = e.getArgs()[0];
        String message = e.getArgs()[1];
		JDA jda = spigot.getJda();
		String globalchat = Synergy.getConfig().getString("discord.channels.global-chat-channel");
		String adminchat = Synergy.getConfig().getString("discord.channels.admin-chat-channel");
		String logchat = Synergy.getConfig().getString("discord.channels.log-channel");
		
		if (jda != null) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor(player, null, "https://minotar.net/helm/" + player);
			builder.setTitle(new ChatManager(spigot).removeChatTypeSymbol(message), null);
			
			if (logchat.length() == 19) {
				jda.getTextChannelById(logchat).sendMessage("```["+new ChatManager(spigot).getChatTypeFromMessage(message)+"] "+player+": "+message+"```").queue();
			}
			if (globalchat.length() == 19 && new ChatManager(spigot).getChatTypeFromMessage(message).equals("global")) {
				builder.setColor(Color.decode("#f1c40f"));
				jda.getTextChannelById(globalchat).sendMessageEmbeds(builder.build()).queue();
			}
			if (adminchat.length() == 19 && new ChatManager(spigot).getChatTypeFromMessage(message).equals("admin") && Bukkit.getPlayer(player).hasPermission("synergy.adminchat")) {
				builder.setColor(Color.decode("#e74c3c"));
				jda.getTextChannelById(adminchat).sendMessageEmbeds(builder.build()).queue();
			}
		}
    }
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		Member memder = event.getMember();
		
		if (!event.getAuthor().isBot()) {
			
			String channelId = event.getChannel().getId();
			
			if (channelId.equals(Synergy.getConfig().getString("discord.channels.global-chat-channel"))) {
				SynergyPluginMessage spm = new SynergyPluginMessage("chat");
		    	spm.setArguments(new String[] {memder.getEffectiveName(), "@"+message.getContentDisplay()}).send(spigot);
			}
			
			if (channelId.equals(Synergy.getConfig().getString("discord.channels.admin-chat-channel"))) {
				SynergyPluginMessage spm = new SynergyPluginMessage("chat");
		    	spm.setArguments(new String[] {memder.getEffectiveName(), "$"+message.getContentDisplay()}).send(spigot);
			}
			
			if (Synergy.getConfig().getBoolean("discord.gpt-bot.enabled")) {
				if (event.getMessage().getMentions().isMentioned(event.getJDA().getSelfUser(), Message.MentionType.USER) || (event.getMessage().getReferencedMessage() != null && event.getMessage().getReferencedMessage().getAuthor().equals(event.getJDA().getSelfUser()))) {
					message.getChannel().sendTyping().queue();
	    			OpenAi gpt = new OpenAi();
	    			String question = spigot.getConfig().getString("discord.gpt-bot.personality").replace("%MESSAGE%", event.getMessage().getContentRaw().replace("<@1213481423055486976>", ""));
	    			List<CompletionChoice> text = gpt.newPrompt((event.getMessage().getReferencedMessage() != null ? event.getMessage().getReferencedMessage().getContentRaw() : "") + question);
	    			text.forEach(t -> spigot.log(t.getText(), false));
	    			String answer = text.get(0).getText();
	    			event.getMessage().reply(answer.replace("\"", " ")).queue();
				}
			}
			
			
			
			return;
		}
		
	}
	
}