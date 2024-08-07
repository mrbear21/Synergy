package me.synergy.handlers;

import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;

import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;
import me.synergy.events.SynergyVelocityEvent;
import me.synergy.modules.OpenAi;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.Translation;
import me.synergy.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInput.Builder;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class DiscordListener extends ListenerAdapter implements Listener {

	public void initialize() {
        if (!Synergy.getConfig().getBoolean("discord.enabled")) {
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());
	}

	@Subscribe(order = PostOrder.EARLY)
	public void onEvent(SynergyVelocityEvent e) {
        if (!e.getIdentifier().equals("chat")) {
            return;
        }
	}

    @EventHandler
    public void getMessage(SynergyEvent event) {

    	if (event.getIdentifier().equals("make-discord-link")) {
    		UUID uuid = event.getPlayerUniqueId();
    		String discordTag = event.getOption("tag").getAsString();
    		Synergy.getDiscord().makeDiscordLink(uuid, discordTag);
    	}

    	if (event.getIdentifier().equals("confirm-discord-link")) {
    		UUID uuid = event.getPlayerUniqueId();
    		Synergy.getDiscord().confirmDiscordLink(uuid);
    	}

        if (event.getIdentifier().equals("create-discord-link")) {
        	UUID uuid = event.getPlayerUniqueId();
            String discordId = event.getOption("id").getAsString();
            Synergy.getDiscord().createDiscordLink(uuid, discordId);
        }

    	if (event.getIdentifier().equals("remove-discord-link")) {
    		UUID uuid = event.getPlayerUniqueId();
    		Synergy.getDiscord().removeDiscordLink(uuid);
    	}

    	if (event.getIdentifier().equals("discord-log")) {
	        String logchat = Synergy.getConfig().getString("discord.channels.log-channel");
            if (logchat.length() == 19) {
            	Synergy.getDiscord().getJda().getTextChannelById(logchat).sendMessage(event.getOption("message").getAsString()).queue();
            }
    	}

        if (event.getIdentifier().equals("discord-announcement")) {
        	 String announcementschat = Synergy.getConfig().getString("discord.channels.announcements-channel");
        	 if (announcementschat.length() == 19) {
        		 MessageEmbed announcement = info(Synergy.translate(event.getOption("message").getAsString(), Translation.getDefaultLanguage()).getStripped().replace("%ARGUMENT%", event.getOption("argument").getAsString()));
        		 Synergy.getDiscord().getJda().getTextChannelById(announcementschat).sendMessageEmbeds(announcement).queue();
        	 }
        }

        if (event.getIdentifier().equals("discord")) {

	        String displayname = event.getOption("player").getAsString();
	        String message = event.getOption("message").getAsString();
	        message = Utils.translateSmiles(message);
	        message = Utils.censorBlockedWords(message);
	        message =  Utils.removeRepetitiveCharacters(message);
	        String chat = event.getOption("chat").getAsString();

	        String globalchat = Synergy.getConfig().getString("discord.channels.global-chat-channel");
	        String adminchat = Synergy.getConfig().getString("discord.channels.admin-chat-channel");

            String[] messageParts = Utils.splitMessage(message);
            for (String part: messageParts) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setAuthor(displayname, null, Synergy.getDiscord().getBotName().equals(displayname) ? Synergy.getDiscord().getJda().getSelfUser().getAvatarUrl() : ("https://minotar.net/helm/" + displayname));
                builder.setTitle(Synergy.getChatManager().removeChatTypeSymbol(part), null);
                if (globalchat.length() == 19 && chat.equals("global")) {
                    builder.setColor(Color.decode("#f1c40f"));
                    Synergy.getDiscord().getJda().getTextChannelById(globalchat).sendMessageEmbeds(builder.build()).queue();
                }
                if (adminchat.length() == 19 && chat.equals("admin") && Bukkit.getPlayer(event.getPlayerUniqueId()).hasPermission("synergy.adminchat")) {
                    builder.setColor(Color.decode("#e74c3c"));
                    Synergy.getDiscord().getJda().getTextChannelById(adminchat).sendMessageEmbeds(builder.build()).queue();
                }
                Synergy.getLogger().discord("```[" + chat + "] " + displayname + ": " + part + "```");
            }
        }

        if (event.getIdentifier().equals("sync-roles-from-mc-to-discord")) {
        	Synergy.getDiscord().syncRolesFromMcToDiscord(event.getPlayerUniqueId(), event.getOption("group").getAsString());
        }

        if (event.getIdentifier().equals("sync-roles-from-discord-to-mc")) {
        	Synergy.getDiscord().syncRolesFromDiscordToMc(event.getPlayerUniqueId());
        }

        if (event.getIdentifier().equals("remove-player-group")) {
            if (Synergy.isDependencyAvailable("Vault") && Synergy.getConfig().getBoolean("discord.synchronization.use-vault")) {
            	Synergy.getSpigot().getPermissions().playerRemoveGroup(Bukkit.getPlayer(event.getPlayerUniqueId()), event.getOption("group").getAsString());
            } else {
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Synergy.getConfig().getString("discord.synchronization.custom-command-remove").replace("%PLAYER%", event.getBread().getName()).replace("%GROUP%", event.getOption("group").getAsString()));
            }
        }

        if (event.getIdentifier().equals("set-player-group")) {
            if (Synergy.isDependencyAvailable("Vault") && Synergy.getConfig().getBoolean("discord.synchronization.use-vault")) {
                Synergy.getSpigot().getPermissions().playerAddGroup(Bukkit.getPlayer(event.getPlayerUniqueId()), event.getOption("group").getAsString());
            } else {
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Synergy.getConfig().getString("discord.synchronization.custom-command-add").replace("%PLAYER%", event.getBread().getName()).replace("%GROUP%", event.getOption("group").getAsString()));
            }
        }
    }

    @Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (Synergy.getConfig().getBoolean("discord.synchronization.sync-roles-from-discord-to-mc")) {
        	Synergy.getDiscord().syncRolesFromDiscordToMc(event.getMember());
        }
    }

    @Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        if (Synergy.getConfig().getBoolean("discord.synchronization.sync-roles-from-discord-to-mc")) {
        	Synergy.getDiscord().syncRolesFromDiscordToMc(event.getMember());
        }
    }

    @Override
	public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        User user = event.getAuthor();
        String channelId = event.getChannel().getId();

        UUID uuid = Synergy.getDiscord().getUniqueIdByDiscordId(user.getId());

        if (!event.getAuthor().isBot()) {

            if (channelId.equals(Synergy.getConfig().getString("discord.channels.global-chat-channel"))) {
                if (uuid == null) {
                	event.getChannel().sendMessageEmbeds(warning(Synergy.translate("<lang>synergy-you-have-to-link-account</lang>", Translation.getDefaultLanguage()).getStripped())).queue();
                    return;
                }
                if (Synergy.getBread(uuid).isMuted()) {
                	event.getMessage().replyEmbeds(warning(Synergy.translate("<lang>synergy-you-are-muted</lang>", Translation.getDefaultLanguage()).getStripped())).queue();
                	return;
                }

	            Synergy.createSynergyEvent("chat").setPlayerUniqueId(uuid).setOption("player", event.getAuthor().getEffectiveName())
	            	.setOption("chat", "discord").setOption("message", message.getContentDisplay()).send();

            }

            if (channelId.equals(Synergy.getConfig().getString("discord.channels.admin-chat-channel"))) {
                if (uuid == null) {
                	event.getChannel().sendMessageEmbeds(warning(Synergy.translate("<lang>synergy-you-have-to-link-account</lang>", Translation.getDefaultLanguage()).getStripped())).queue();
                    return;
                }
                Synergy.createSynergyEvent("chat").setPlayerUniqueId(uuid).setOption("player", event.getAuthor().getEffectiveName())
	            	.setOption("chat", "discord_admin").setOption("message", message.getContentDisplay()).send();
            }

            if (Synergy.getConfig().getBoolean("discord.gpt-bot.enabled")) {

                try {
                    boolean startsWithBotName = message.getContentDisplay().toLowerCase().startsWith(Synergy.getDiscord().getBotName().toLowerCase());
                    boolean isGlobalChatChannel = channelId.equals(Synergy.getConfig().getString("discord.channels.global-chat-channel"));
                    boolean mentionedBot = message.getMentions().isMentioned((IMentionable) event.getJDA().getSelfUser(), Message.MentionType.USER);
                    boolean isReplyToBot = message.getReferencedMessage() != null && message.getReferencedMessage().getAuthor().equals(event.getJDA().getSelfUser());

                    if ((startsWithBotName && !isGlobalChatChannel) || mentionedBot || isReplyToBot) {
                        message.getChannel().sendTyping().queue();
                        String question = Synergy.getConfig().getString("discord.gpt-bot.personality")
                            .replace("%MESSAGE%", Utils.removeIgnoringCase(Synergy.getDiscord().getBotName(), event.getMessage().getContentRaw())
                                .replace(event.getJDA().getSelfUser().getAsMention(), ""));
                        //String.valueOf((event.getMessage().getReferencedMessage() != null) ? event.getMessage().getReferencedMessage().getContentRaw() : "");
                        String answer = new OpenAi().newPrompt(question).get(0).getText().replace("\"", "");
                        message.reply(answer).queue();
                    }

                    if (startsWithBotName && isGlobalChatChannel) {
                        String question = Synergy.getConfig().getString("discord.gpt-bot.personality").replace("%MESSAGE%", Utils.removeIgnoringCase(Synergy.getDiscord().getBotName(), message.getContentDisplay()));
                        String answer = (new OpenAi().newPrompt(question).get(0)).getText().replace("\"", "").trim();
                        Synergy.createSynergyEvent("chat").setOption("player", Synergy.getDiscord().getBotName())
                    		.setOption("chat", "discord").setOption("message", answer).send();
                        Synergy.createSynergyEvent("discord").setOption("player", Synergy.getDiscord().getBotName())
	                        .setOption("chat", "global").setOption("message", answer).send();
                    }

                } catch (Exception c) {
                    Synergy.getLogger().error(c.getMessage());
                    event.getMessage().replyEmbeds(warning(Synergy.translate("<lang>synergy-service-unavailable</lang>", Translation.getDefaultLanguage()).getStripped())).queue();
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
            case "confirm":
            	if (Synergy.getDiscord().getUniqueIdByDiscordId(event.getUser().getId()) == null) {
            		UUID uuid = UUID.fromString(id[2]);
            		Synergy.getDiscord().createDiscordLink(uuid, authorId);
            		BreadMaker bread = Synergy.getBread(uuid);
	                event.getUser().openPrivateChannel().complete().sendMessage(Synergy.translate("<lang>synergy-discord-link-success</lang>", Translation.getDefaultLanguage()).getStripped().replace("%ACCOUNT%", bread.getName())).queue();
            	}
                break;
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (event.getGuild() == null) {
				return;
			}
            switch (event.getName()) {
                case "post":
                	post(event);
                    break;
                case "list":
                	list(event);
                    break;
                case "link":
                	link(event);
                    break;
                case "balance":
                	balance(event);
                    break;
                case "embed":
                	embed(event);
                    break;
                case "vote":
                	vote(event);
                    break;
                default:
                    event.replyEmbeds(warning(Synergy.translate("<lang>synergy-service-unavailable</lang>", Translation.getDefaultLanguage()).getStripped())).setEphemeral(true).queue();
            }
        } catch (Exception c) {
            event.replyEmbeds(warning(Synergy.translate("<lang>synergy-service-unavailable</lang>", Translation.getDefaultLanguage()) + " (*" + c.getMessage() + "*)")).setEphemeral(true).queue();
        }
    }

    @Override
	public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (event.getModalId().equals("minecraftlink")) {
            String username = event.getValue("username").getAsString();
            UUID uuid = Synergy.getUniqueIdFromName(username);
            BreadMaker bread = new BreadMaker(uuid);

            if (uuid == null) {
                event.replyEmbeds(warning(Synergy.translate("<lang>synergy-player-doesnt-exist</lang>", Translation.getDefaultLanguage()).getStripped().replace("%ACCOUNT%", Synergy.getDiscord().getJda().getUserById(Synergy.getDiscord().getDiscordIdByUniqueId(uuid)).getEffectiveName()))).setEphemeral(true).queue();
                return;
            }
            if (Synergy.getDiscord().getDiscordIdByUniqueId(uuid) != null) {
                event.replyEmbeds(warning(Synergy.translate("<lang>synergy-link-minecraft-already-linked</lang>", Translation.getDefaultLanguage()).getStripped().replace("%ACCOUNT%", Synergy.getDiscord().getJda().getUserById(Synergy.getDiscord().getDiscordIdByUniqueId(uuid)).getEffectiveName()))).setEphemeral(true).queue();
                return;
            }
            if (Synergy.getDiscord().getUniqueIdByDiscordId(event.getUser().getId()) != null) {
            	UUID account = Synergy.getDiscord().getUniqueIdByDiscordId(event.getUser().getId());
                event.replyEmbeds(warning(Synergy.translate("<lang>synergy-link-discord-already-linked</lang>", Translation.getDefaultLanguage()).getStripped().replace("%ACCOUNT%", Synergy.getBread(account).getName()))).setEphemeral(true).queue();
                return;
            }

            Synergy.getDataManager().setData("players."+uuid+".confirm-discord", event.getUser().getId());
            event.replyEmbeds(info(Synergy.translate("<lang>synergy-link-minecraft-confirmation</lang>", Translation.getDefaultLanguage()).getStripped())).setEphemeral(true).queue();
            bread.sendMessage(Translation.translate("<lang>synergy-link-discord-confirmation</lang>", bread.getLanguage()).replace("%ACCOUNT%", event.getUser().getEffectiveName()));
        }

        if (event.getModalId().equals("embed")) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(event.getValue("author") == null ? null : event.getValue("author").getAsString(), null, "https://minotar.net/helm/" + event.getValue("author").getAsString());
            builder.setTitle(event.getValue("title") == null ? null : event.getValue("title").getAsString());
            new Utils();
			builder.setDescription(event.getValue("text") == null ? null : Synergy.translate(event.getValue("text").getAsString(), Translation.getDefaultLanguage()).getStripped());
            builder.setColor(event.getValue("color") == null ? null : Color.decode(event.getValue("color").getAsString()));
            builder.setImage(event.getValue("image") == null ? null : event.getValue("image").getAsString());
            if (message != null) {
                channel.retrieveMessageById(message).complete().editMessageEmbeds(builder.build()).queue();
                message = null;
            } else {
                channel.sendMessageEmbeds(builder.build()).queue();
            }
            event.reply("Published!").setEphemeral(true).queue();
        }
    }

    public void list(SlashCommandInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(Synergy.translate("<lang>synergy-online-players-list</lang>", Translation.getDefaultLanguage()).getStripped());
    	if (Synergy.getDiscord().getUniqueIdByDiscordId(event.getUser().getId()) != null) {
    		BreadMaker bread = Synergy.getBread(Synergy.getDiscord().getUniqueIdByDiscordId(event.getUser().getId()));
            builder.setTitle(Synergy.translate("<lang>synergy-online-players-list</lang>", bread.getLanguage()).getStripped());
    	}
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
            List < String > players = new ArrayList < > ();
            Bukkit.getOnlinePlayers().forEach(player -> players.add(player.getName()));
            list = list + String.join(", ", players);
        }
        builder.setDescription(list);
        builder.setColor(Color.decode("#a29bfe"));
        event.replyEmbeds(builder.build()).queue();
    }

    public void post(SlashCommandInteractionEvent event) {
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
		builder.setDescription(Synergy.translate(text, Translation.getDefaultLanguage()).getStripped());
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

    public void balance(SlashCommandInteractionEvent event) {
    	if (Synergy.getDiscord().getUniqueIdByDiscordId(event.getUser().getId()) != null) {
        	BreadMaker bread = Synergy.getBread(Synergy.getDiscord().getUniqueIdByDiscordId(event.getUser().getId()));
			OfflinePlayer player = Bukkit.getOfflinePlayer(Synergy.getDiscord().getUniqueIdByDiscordId(event.getUser().getId()));
	    	double balance = Synergy.getSpigot().getEconomy().getBalance(player);
	    	EmbedBuilder embed = new EmbedBuilder();
	    	embed.addField(Synergy.translate("<lang>synergy-vault-balance-title</lang>", bread.getLanguage()).getStripped(), Synergy.translate("<lang>synergy-vault-balance-field</lang>", bread.getLanguage()).getStripped().replace("%AMOUNT%", String.valueOf((int) balance)), true);
	    	embed.setThumbnail("https://minotar.net/helm/"+Synergy.getBread(Synergy.getDiscord().getUniqueIdByDiscordId(event.getUser().getId())).getName());
	    	embed.setColor(Color.decode("#f1c40f"));
	    	embed.setFooter(Synergy.translate("<lang>synergy-vault-balance-footer</lang>", bread.getLanguage()).getStripped());
	    	event.replyEmbeds(embed.build()).queue();
    	} else {
    		event.replyEmbeds(warning(Synergy.translate("<lang>synergy-you-have-to-link-account</lang>", Translation.getDefaultLanguage()).getStripped())).queue();
    	}
	}

    public void vote(SlashCommandInteractionEvent event) {
    	BreadMaker bread = Synergy.getDiscord().getUniqueIdByDiscordId(event.getUser().getId()) != null ? Synergy.getBread(Synergy.getDiscord().getUniqueIdByDiscordId(event.getUser().getId())) : null;
    	EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(Synergy.translate("<lang>synergy-vote-monitorings</lang>", bread.getLanguage()).getStripped());
        StringBuilder links = new StringBuilder();
        for (String link : Synergy.getConfig().getStringList("votifier.monitorings")) {
        	try {
            	String domain = new URI(link).getHost();
        		String format = Synergy.translate("<lang>synergy-vote-monitorings-format-stripped</lang>", bread.getLanguage()).getStripped().replace("%MONITORING%", domain).replace("%URL%", link) + "\n";
				links.append(format);
			} catch (URISyntaxException e) {e.printStackTrace();}
        }
        embed.setDescription(links);
        embed.setColor(Color.decode("#f1c40f"));
        event.replyEmbeds(embed.build()).queue();
    }


    public void link(SlashCommandInteractionEvent event) {
        TextInput subject = TextInput.create("username", Synergy.translate("<lang>synergy-link-minecraft-your-username</lang>", Translation.getDefaultLanguage()).getStripped(), TextInputStyle.SHORT).setPlaceholder("Steve").setMinLength(3).setMaxLength(28).build();
        Modal modal = Modal.create("minecraftlink", Synergy.translate("<lang>synergy-link-minecraft-title</lang>", Translation.getDefaultLanguage()).getStripped()).addComponents(new LayoutComponent[] {
            ActionRow.of(new ItemComponent[] {
                subject
            })
        }).build();
        event.replyModal(modal).queue();
    }

    private static TextChannel channel = null;
    private static String message = null;

    public void embed(SlashCommandInteractionEvent event) {
        if (event.getOption("channel") != null) {
            channel = event.getOption("channel").getAsChannel().asTextChannel();
        }

        Builder title = TextInput.create("title", "Title", TextInputStyle.SHORT).setPlaceholder("Title").setMinLength(0).setMaxLength(256);
        Builder text = TextInput.create("text", "Text", TextInputStyle.PARAGRAPH).setPlaceholder("Text").setMinLength(0).setMaxLength(1000);
        Builder author = TextInput.create("author", "Author", TextInputStyle.SHORT).setPlaceholder("Author").setRequired(false).setMinLength(0).setMaxLength(256);
        Builder image = TextInput.create("image", "Image URL", TextInputStyle.SHORT).setPlaceholder("URL").setRequired(false).setMinLength(0).setMaxLength(256);
        Builder color = TextInput.create("color", "#color", TextInputStyle.SHORT).setPlaceholder("#B48EAD").setMinLength(0).setMaxLength(256);

        if (event.getOption("message") != null) {
            message = event.getOption("message").getAsString();
            MessageEmbed embed = channel.retrieveMessageById(event.getOption("message").getAsString()).complete().getEmbeds().get(0);
            if (embed.getTitle() != null) {
				title.setValue(embed.getTitle());
			}
            if (embed.getDescription() != null) {
				text.setValue(embed.getDescription());
			}
            if (embed.getAuthor() != null) {
				author.setValue(embed.getAuthor().getName());
			}
            if (embed.getImage() != null) {
				image.setValue(embed.getImage().getUrl());
			}
            if (embed.getColor() != null) {
				color.setValue("#" + Integer.toHexString(embed.getColor().getRGB()).substring(2));
			}
        }

        Modal modal = Modal.create("embed", event.getOption("message") != null ? Synergy.translate("<lang>synergy-discord-embed-edit</lang>", Translation.getDefaultLanguage()).getStripped() : Synergy.translate("<lang>synergy-discord-embed-new</lang>", Translation.getDefaultLanguage()).getStripped()).addComponents(
            ActionRow.of(title.build()),
            ActionRow.of(text.build()),
            ActionRow.of(author.build()),
            ActionRow.of(image.build()),
            ActionRow.of(color.build())
        ).build();
        event.replyModal(modal).queue();
    }


    public MessageEmbed info(String message) {
    	EmbedBuilder embed = new EmbedBuilder();
    	embed.setColor(Color.decode("#3498db"));
    	embed.setDescription(message);
    	return embed.build();
    }

    public MessageEmbed warning(String message) {
    	EmbedBuilder embed = new EmbedBuilder();
    	embed.setColor(Color.decode("#f39c12"));
    	embed.setDescription(message);
    	return embed.build();
    }

	public void updateCommands() {
        CommandListUpdateAction commands = Synergy.getDiscord().getJda().updateCommands();
        commands.addCommands(new CommandData[] {
            Commands.slash("post", Synergy.translate("<lang>synergy-create-post</lang>", Translation.getDefaultLanguage()).getStripped())
                .addOptions(new OptionData[] {
                    (new OptionData(OptionType.STRING, "title", "Title")).setRequired(true)
                })
                .addOptions(new OptionData[] {
                    (new OptionData(OptionType.STRING, "text", "Text")).setRequired(true)
                })
                .addOptions(new OptionData[] {
                    new OptionData(OptionType.STRING, "author", "Author")
                })
                .addOptions(new OptionData[] {
                    new OptionData(OptionType.CHANNEL, "channel", "Channel")
                })
                .addOptions(new OptionData[] {
                    new OptionData(OptionType.STRING, "image", "Image url")
                })
                .addOptions(new OptionData[] {
                    new OptionData(OptionType.STRING, "color", "#Color")
                })
                .addOptions(new OptionData[] {
                    new OptionData(OptionType.STRING, "thumbnail", "Image url")
                })
                .addOptions(new OptionData[] {
                    new OptionData(OptionType.MENTIONABLE, "mention", "Mention")
                })
                .addOptions(new OptionData[] {
                    new OptionData(OptionType.STRING, "edit", "Message ID (edit a message that has already been sent)")
                })
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(new Permission[] {
                    Permission.MESSAGE_MANAGE
                }))
        });

        if (Synergy.isDependencyAvailable("Vault")) {
	        commands.addCommands(new CommandData[] {
	            Commands.slash("balance", Synergy.translate("<lang>synergy-check-vault-balance</lang>", Translation.getDefaultLanguage()).getStripped())
	                .setGuildOnly(true)
	        });
        }

        commands.addCommands(new CommandData[] {
            Commands.slash("list", Synergy.translate("<lang>synergy-online-players-list</lang>", Translation.getDefaultLanguage()).getStripped())
                .setGuildOnly(true)
        });

        commands.addCommands(new CommandData[] {
            Commands.slash("vote", Synergy.translate("<lang>synergy-vote-for-server</lang>", Translation.getDefaultLanguage()).getStripped())
                .setGuildOnly(true)
        });

        commands.addCommands(new CommandData[] {
            Commands.slash("link", Synergy.translate("<lang>synergy-link-minecraft-title</lang>", Translation.getDefaultLanguage()).getStripped())
                .setGuildOnly(true)
        });
        commands.addCommands(new CommandData[] {
            Commands.slash("embed", Synergy.translate("<lang>synergy-discord-embed-new</lang>", Translation.getDefaultLanguage()).getStripped())
                .addOptions(new OptionData[] {
                    (new OptionData(OptionType.CHANNEL, "channel", "Channel ID")).setRequired(true)
                })
                .addOptions(new OptionData[] {
                    (new OptionData(OptionType.STRING, "message", "Message ID (edit a message that has already been sent)"))
                })
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(new Permission[] {
                    Permission.MESSAGE_MANAGE
                }))
        });
        commands.queue();
	}


}
