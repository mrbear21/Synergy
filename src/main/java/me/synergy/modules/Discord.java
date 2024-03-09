package me.synergy.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Discord extends ListenerAdapter implements Listener {

    private static JDA JDA;

    public Discord() {}

    private static final GatewayIntent[] INTENTS = new GatewayIntent[] {
        GatewayIntent.SCHEDULED_EVENTS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES
    };

    public void initialize() {
        try {
        	
            if (!Synergy.getConfig().getBoolean("discord.enabled")) {
                return;
            }
            
            String token = Synergy.getConfig().getString("discord.bot-token");
            
            Discord.JDA = JDABuilder.create(token, Arrays.asList(INTENTS))
        		.enableCache(CacheFlag.MEMBER_OVERRIDES, new CacheFlag[] {CacheFlag.VOICE_STATE})
        		.disableCache(CacheFlag.ACTIVITY, new CacheFlag[] {CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.ONLINE_STATUS, CacheFlag.STICKER})
        		.setStatus(OnlineStatus.ONLINE)
        		.setActivity(Activity.customStatus(Synergy.getConfig().getString("discord.activity")))
        		.setMemberCachePolicy(MemberCachePolicy.ALL)
        		.addEventListeners(this)
        		.setBulkDeleteSplittingEnabled(true).build();

            if (Synergy.isRunningSpigot()) {
                Bukkit.getPluginManager().registerEvents(new Discord(), Synergy.getSpigotInstance());
            }

            CommandListUpdateAction commands = Discord.JDA.updateCommands();
            commands.addCommands(new CommandData[] {
                Commands.slash("post", Synergy.translateString("synergy-create-post"))
                .addOptions(new OptionData[] {(new OptionData(OptionType.STRING, "title", "Title")).setRequired(true)})
                .addOptions(new OptionData[] {(new OptionData(OptionType.STRING, "text", "Text")).setRequired(true)})
                .addOptions(new OptionData[] {new OptionData(OptionType.STRING, "author", "Author")})
                .addOptions(new OptionData[] {new OptionData(OptionType.CHANNEL, "channel", "Channel")})
                .addOptions(new OptionData[] {new OptionData(OptionType.STRING, "image", "Image url")})
                .addOptions(new OptionData[] {new OptionData(OptionType.STRING, "color", "#Color")})
                .addOptions(new OptionData[] {new OptionData(OptionType.STRING, "thumbnail", "Image url")})
                .addOptions(new OptionData[] {new OptionData(OptionType.MENTIONABLE, "mention", "Mention")})
                .addOptions(new OptionData[] {new OptionData(OptionType.STRING, "edit", "Message ID (edit a message that has already been sent)")})
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(new Permission[] {Permission.MESSAGE_MANAGE}))
            });
            commands.addCommands(new CommandData[] {
                Commands.slash("list", Synergy.translateString("synergy-online-players-list"))
                .setGuildOnly(true)
            });
            commands.addCommands(new CommandData[] {
                Commands.slash("link", Synergy.translateString("synergy-link-minecraft"))
                .setGuildOnly(true)
            });
            commands.addCommands(new CommandData[] {
                Commands.slash("prune", Synergy.translateString("synergy-prune-messages"))
                .addOption(OptionType.INTEGER, "amount", "Amount")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(new Permission[] {Permission.MESSAGE_MANAGE}))
            });
            commands.queue();
            
            Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
        } catch (Exception exception) {
        	Synergy.getLogger().error(String.valueOf(getClass().getSimpleName()) + " module failed to initialize: "+  exception.getMessage());;
        }
    }

    public void shutdown() {
    	if (getJda() != null) {
    		getJda().shutdownNow();
    	}
    }
    
    public JDA getJda() {
    	return JDA;
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null)
            return;
        switch (event.getName()) {
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
        TextInput subject = TextInput.create("username", Synergy.translateString("synergy-link-minecraft-your-username"), TextInputStyle.SHORT).setPlaceholder("Steve").setMinLength(3).setMaxLength(28).build();
        Modal modal = Modal.create("minecraftlink", Synergy.translateString("synergy-link-minecraft")).addComponents(new LayoutComponent[] {
            (LayoutComponent) ActionRow.of(new ItemComponent[] {
                (ItemComponent) subject
            })
        }).build();
        event.replyModal(modal).queue();
    }

    @EventHandler
    public void onDiscordLink(SynergyEvent event) {
        if (!event.getIdentifier().equals("discord-link"))
            return;
        String player = event.getPlayer();
        String discord = event.getArgument();
        if (Bukkit.getPlayer(player) != null)
            Bukkit.getPlayer(player).sendMessage("Your discord => " + discord);
    }

    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (event.getModalId().equals("minecraftlink")) {
            String username = event.getValue("username").getAsString();
            if (Bukkit.getPlayer(username) != null)
                Synergy.createSynergyEvent("discord-link").setPlayer(username).setArgument(event.getMember().getId()).send();
            event.reply(Synergy.translateString("synergy-link-minecraft-confirmation")).setEphemeral(true).queue();
        }
    }

	private void list(SlashCommandInteractionEvent event) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(Synergy.translateString("synergy-online-players-list"));
		String list = "";
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
        String author = (event.getOption("author") != null) ? event.getOption("author").getAsString() : null;
        TextChannel channel = (event.getOption("channel") != null) ? event.getOption("channel").getAsChannel().asTextChannel() : event.getChannel().asTextChannel();
        String image = (event.getOption("image") != null) ? event.getOption("image").getAsString() : null;
        String thumbnail = (event.getOption("thumbnail") != null) ? event.getOption("thumbnail").getAsString() : null;
        String color = (event.getOption("color") != null) ? event.getOption("color").getAsString() : "#a29bfe";
        String edit = (event.getOption("edit") != null) ? event.getOption("edit").getAsString() : null;
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(author, null, "https://minotar.net/helm/" + author);
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
	    if (!authorId.equals(event.getUser().getId())) {
	        return;
	    }
	    event.deferEdit().queue();
	    switch (type) {
	        case "prune":
	            int amount = Integer.parseInt(id[2]);
	            event.getChannel().getIterableHistory()
	                .skipTo(event.getMessageIdLong())
	                .takeAsync(amount)
	                .thenAccept(event.getChannel()::purgeMessages);
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
    public void getMessage(SynergyEvent e) {
        if (!e.getIdentifier().equals("discord")) {
            return;
        }
        
        String player = e.getPlayer();
        String message = e.getArgument();
        String globalchat = Synergy.getConfig().getString("discord.channels.global-chat-channel");
        String adminchat = Synergy.getConfig().getString("discord.channels.admin-chat-channel");
        String logchat = Synergy.getConfig().getString("discord.channels.log-channel");
        
        if (getJda() != null) {
            String[] messageParts = splitMessage(message);
            for (String part : messageParts) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setAuthor(player, null, getBotName().equals(player) ? getJda().getSelfUser().getAvatarUrl() : ("https://minotar.net/helm/" + player));
                builder.setTitle(Synergy.getChatManager().removeChatTypeSymbol(part), null);
                if (logchat.length() == 19)
                	getJda().getTextChannelById(logchat).sendMessage("```[" + Synergy.getChatManager().getChatTypeFromMessage(message) + "] " + player + ": " + part + "```").queue();
                if (globalchat.length() == 19 && Synergy.getChatManager().getChatTypeFromMessage(message).equals("global")) {
                    builder.setColor(Color.decode("#f1c40f"));
                    getJda().getTextChannelById(globalchat).sendMessageEmbeds(builder.build()).queue();
                }
                if (adminchat.length() == 19 && Synergy.getChatManager().getChatTypeFromMessage(message).equals("admin") && Bukkit.getPlayer(player).hasPermission("synergy.adminchat")) {
                    builder.setColor(Color.decode("#e74c3c"));
                    getJda().getTextChannelById(adminchat).sendMessageEmbeds(builder.build()).queue();
                }
            }
        }
    }
    
    private String[] splitMessage(String message) {
        List<String> parts = new ArrayList<>();
        String[] words = message.split("\\s+");
        StringBuilder currentPart = new StringBuilder();
        
        for (String word : words) {
            if (currentPart.length() + word.length() + 1 <= 256) {
                currentPart.append(currentPart.length() > 0 ? " " : "").append(word);
            } else {
                parts.add(currentPart.toString());
                currentPart = new StringBuilder(word);
            }
        }
        
        parts.add(currentPart.toString());
        
        return parts.toArray(new String[0]);
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        Member memder = event.getMember();
        if (!event.getAuthor().isBot()) {
            String channelId = event.getChannel().getId();
            if (channelId.equals(Synergy.getConfig().getString("discord.channels.global-chat-channel")))
                Synergy.createSynergyEvent("chat").setPlayer(memder.getEffectiveName()).setArguments(new String[] {
                    "@" + message.getContentDisplay()
                }).send();
            if (channelId.equals(Synergy.getConfig().getString("discord.channels.admin-chat-channel")))
                Synergy.createSynergyEvent("chat").setPlayer(memder.getEffectiveName()).setArguments(new String[] {
                    "$" + message.getContentDisplay()
                }).send();
            if (Synergy.getConfig().getBoolean("discord.gpt-bot.enabled"))
                try {
                    if ( event.getMessage().getContentDisplay().toLowerCase().startsWith(getBotName().toLowerCase())
                    		|| event.getMessage().getMentions().isMentioned((IMentionable) event.getJDA().getSelfUser(), new Message.MentionType[] {Message.MentionType.USER}) 
                    		|| (event.getMessage().getReferencedMessage() != null
                    		&& event.getMessage().getReferencedMessage().getAuthor().equals(event.getJDA().getSelfUser()))) {
                    	
                        message.getChannel().sendTyping().queue();
                        
                        String question =  Synergy.getConfig().getString("discord.gpt-bot.personality")
                        		.replace("%MESSAGE%", Synergy.getUtils().removeIgnoringCase(getBotName(), event.getMessage().getContentRaw())
                        		.replace(event.getJDA().getSelfUser().getAsMention(), ""));
                        Synergy.debug(question);
                        //String.valueOf((event.getMessage().getReferencedMessage() != null) ? event.getMessage().getReferencedMessage().getContentRaw() : "");
                        String answer = new OpenAi().newPrompt(question).get(0).getText().replace("\"", "");
                        
                        event.getMessage().reply(answer).queue();
                    }
                    if (event.getMessage().getContentDisplay().toLowerCase().startsWith(getBotName().toLowerCase()) && channelId.equals(Synergy.getConfig().getString("discord.channels.global-chat-channel"))) {
                        String question = Synergy.getConfig().getString("discord.gpt-bot.personality").replace("%MESSAGE%", Synergy.getUtils().removeIgnoringCase(getBotName(), event.getMessage().getContentDisplay()));
                        String answer = (new OpenAi().newPrompt(question).get(0)).getText().replace("\"", "").trim();
                        Synergy.createSynergyEvent("chat").setPlayer(getBotName().replace(" ", "_")).setArguments(new String[] {"@" + answer}).send();
                        Synergy.createSynergyEvent("discord").setPlayer(getBotName().replace(" ", "_")).setArguments(new String[] {"!" + answer}).send();
                    }
                } catch (Exception c) {
                    Synergy.getLogger().error(c.getMessage());
                    event.getMessage().reply(Synergy.translateString("synergy-service-unavailable")).queue();
                }
            if (Synergy.getConfig().getBoolean("discord.hightlights.enabled") &&
                Synergy.getConfig().getStringList("discord.hightlights.channels").contains(channelId) &&
                event.getMessage().getAttachments().size() > 0)
                event.getMessage().addReaction((Emoji) Emoji.fromUnicode(Synergy.getConfig().getString("discord.hightlights.reaction-emoji"))).complete();
            return;
        }
    }

    public String getBotName() {
        return Synergy.getConfig().getString("discord.gpt-bot.name");
    }
}