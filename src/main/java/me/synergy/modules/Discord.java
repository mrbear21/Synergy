package me.synergy.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.clip.placeholderapi.PlaceholderAPI;
import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;
import me.synergy.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
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
import net.dv8tion.jda.api.interactions.components.text.TextInput.Builder;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.md_5.bungee.api.ChatColor;

public class Discord extends ListenerAdapter implements Listener, CommandExecutor, TabCompleter {

    private static JDA JDA;
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private static final GatewayIntent[] INTENTS = new GatewayIntent[] {
        GatewayIntent.SCHEDULED_EVENTS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES
    };

    public void initialize() {
        try {

            if (!Synergy.getConfig().getBoolean("discord.enabled")) {
                return;
            }

            if (Synergy.isSpigot()) {
                Bukkit.getPluginManager().registerEvents(new Discord(), Synergy.getSpigotInstance());
                Synergy.getSpigotInstance().getCommand("discord").setExecutor(this);
                Synergy.getSpigotInstance().getCommand("discord").setTabCompleter(this);
            }

            String token = Synergy.getConfig().getString("discord.bot-token");

            Discord.JDA = JDABuilder.create(token, Arrays.asList(INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, new CacheFlag[] {
                    CacheFlag.VOICE_STATE
                })
                .disableCache(CacheFlag.ACTIVITY, new CacheFlag[] {
                    CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.ONLINE_STATUS, CacheFlag.STICKER
                })
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.customStatus(Synergy.getConfig().getStringList("discord.activities").get(0)))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(this)
                .setBulkDeleteSplittingEnabled(true).build();

            if (Synergy.getConfig().getStringList("discord.activities").size() > 1) {
                executorService.scheduleAtFixedRate(() -> {
                    long currentTimeSeconds = System.currentTimeMillis() / 1000;
                    int index = (int)(currentTimeSeconds % Synergy.getConfig().getStringList("discord.activities").size());
                    String customStatusText = Synergy.getConfig().getStringList("discord.activities").get(index);
                    if (Synergy.isDependencyAvailable("PlaceholderAPI")) {
                        customStatusText = PlaceholderAPI.setPlaceholders(null, customStatusText);
                    }
                    Discord.JDA.getPresence().setActivity(Activity.customStatus(customStatusText));
                }, 0, 10, TimeUnit.SECONDS);
            }

            CommandListUpdateAction commands = Discord.JDA.updateCommands();
            commands.addCommands(new CommandData[] {
                Commands.slash("post", Synergy.translateStringColorStripped("synergy-create-post"))
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

            commands.addCommands(new CommandData[] {
                Commands.slash("balance", Synergy.translateStringColorStripped("synergy-check-vault-balance"))
                    .setGuildOnly(true)
            });
            
            commands.addCommands(new CommandData[] {
                Commands.slash("list", Synergy.translateStringColorStripped("synergy-online-players-list"))
                    .setGuildOnly(true)
            });

            commands.addCommands(new CommandData[] {
                Commands.slash("vote", Synergy.translateStringColorStripped("synergy-vote-for-server"))
                    .setGuildOnly(true)
            });

            commands.addCommands(new CommandData[] {
                Commands.slash("link", Synergy.translateStringColorStripped("synergy-link-minecraft-title"))
                    .setGuildOnly(true)
            });
            commands.addCommands(new CommandData[] {
                Commands.slash("embed", Synergy.translateStringColorStripped("synergy-discord-embed-new"))
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
            commands.addCommands(new CommandData[] {
                Commands.slash("prune", Synergy.translateStringColorStripped("synergy-prune-messages"))
                    .addOption(OptionType.INTEGER, "amount", "Amount")
                    .setGuildOnly(true)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(new Permission[] {
                        Permission.MESSAGE_MANAGE
                    }))
            });
            commands.queue();

            Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
        } catch (Exception exception) {
            Synergy.getLogger().error(String.valueOf(getClass().getSimpleName()) + " module failed to initialize: " + exception.getMessage());;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("link", "unlink", "confirm");
        } else if (args[0].equals("link") && args.length == 2) {
        	List<String> usertags = new ArrayList<>();
        	getJda().getGuilds().forEach(g -> 
        	    g.getMembers().forEach(member -> {
        	    	if (!member.getUser().isBot()) {
        	    		usertags.add(member.getUser().getEffectiveName());
        	    	}
        	    }));
        	return usertags;
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Synergy.translateStringColorStripped("synergy-discord-invite").replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
            return true;
        }

        switch (args[0]) {
            case "link":
            	if (getDiscordIdByPlayername(sender.getName()) != null) {
            		sender.sendMessage("synergy-link-discord-already-linked");
            		return true;
            	}
            	if (args.length < 2) {
            		sender.sendMessage("synergy-discord-link-cmd-usage");
            		return true;
            	}
                try {
                	for (Guild guild : getJda().getGuilds()) {
                	    for (Member member : guild.getMembers()) {
                	        if (member.getEffectiveName().equalsIgnoreCase(args[1])) {
                	            User user = member.getUser();
                	            
                	            if (getPlayernameByDiscordId(user.getId()) != null) {
                	                sender.sendMessage("synergy-link-minecraft-already-linked");
                	                return true;
                	            }
                	            
                	            PrivateChannel privateChannel = user.openPrivateChannel().complete();
                	            String message = Synergy.translateStringColorStripped("synergy-discord-confirm-link").replace("%PLAYER%", sender.getName());
                	            
                	            MessageHistory history = privateChannel.getHistory();
                	            Message lastMessage = history.retrievePast(1).complete().size() == 0 ? null : history.retrievePast(1).complete().get(0);
                	            
                	            System.out.println(lastMessage.getContentRaw() + " => " + message);
                	            
                	            if (lastMessage == null || !lastMessage.getContentRaw().equals(message)) {
                	                if (privateChannel.canTalk()) {
                	                    privateChannel.sendMessage(message)
                	                            .addActionRow(
                	                                    Button.success(user.getId() + ":confirm:" + sender.getName(), Synergy.translateStringColorStripped("synergy-confirm-action")))
                	                            .queue();
                	                    sender.sendMessage(Synergy.translateString("synergy-discord-link-check-pm", sender.getName()).replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
                	                } else {
                	                    sender.sendMessage(Synergy.translateString("synergy-discord-use-link-cmd", sender.getName()).replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
                	                }
                	            } else {
                	                sender.sendMessage(Synergy.translateString("synergy-discord-use-link-cmd", sender.getName()).replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
                	            }
                	            return true;
                	        }
                	    }
                	}
                } catch (Exception c) {
                	c.printStackTrace();
                    sender.sendMessage(Synergy.translateStringColorStripped("synergy-discord-use-link-cmd").replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
                }
                break;
            case "confirm":
            	if (Synergy.getDataManager().getConfig().isSet("players."+sender.getName()+".confirm-discord")) {
	            	String discordid = Synergy.getDataManager().getData("players."+sender.getName()+".confirm-discord").getAsString();
	            	Synergy.createSynergyEvent("discord-link").setWaitForPlayerIfOffline(true).setPlayer(sender.getName()).setArgument(discordid).send();
	            	Synergy.getDataManager().setData("players."+sender.getName()+".confirm-discord", null);
            	} else {
            		sender.sendMessage("synergy-confirmation-nothing-to-confirm");
            	}
                break;
            case "unlink":
                for (String l: Synergy.getDataManager().getConfigurationSection("discord.links").getKeys(false)) {
                    if (Synergy.getDataManager().getData("discord.links." + l).getAsString().equals(sender.getName())) {
                        Synergy.getDataManager().setData("discord.links." + l, null);
                        sender.sendMessage(Synergy.translateString("synergy-link-minecraft-unlinked", sender.getName()));
                        return true;
                    }
                }
                sender.sendMessage("synergy-you-have-no-linked-accounts");
                break;
        }

        return true;
    }


    public void shutdown() {
        if (getJda() != null) {
        	getJda().removeEventListener(this);
            getJda().shutdownNow();
        }
    }

    public JDA getJda() {
        return JDA;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
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
                    event.reply(Synergy.translateStringColorStripped("synergy-service-unavailable")).setEphemeral(true).queue();
            }
        } catch (Exception c) {
            event.reply(Synergy.translateStringColorStripped("synergy-service-unavailable") + " (*" + c.getMessage() + "*)").setEphemeral(true).queue();
        }
    }

    private void balance(SlashCommandInteractionEvent event) {
    	if (getPlayernameByDiscordId(event.getUser().getId()) != null) {
        	@SuppressWarnings("deprecation")
			OfflinePlayer player = Bukkit.getOfflinePlayer(getPlayernameByDiscordId(event.getUser().getId()));
	    	double balance = Synergy.getSpigotInstance().getEconomy().getBalance(player);
	    	EmbedBuilder embed = new EmbedBuilder();
	    	embed.addField(Synergy.translateStringColorStripped("synergy-vault-balance-title"), Synergy.translateStringColorStripped("synergy-vault-balance-field").replace("%AMOUNT%", String.valueOf((int) balance)), true);
	    	embed.setThumbnail("https://minotar.net/helm/"+getPlayernameByDiscordId(event.getUser().getId()));
	    	embed.setColor(Color.decode("#f1c40f"));
	    	embed.setFooter(Synergy.translateStringColorStripped("synergy-vault-balance-footer"));
	    	event.replyEmbeds(embed.build()).queue();
    	} else {
    		event.reply(Synergy.translateStringColorStripped("synergy-you-have-to-link-account")).queue();
    	}
	}


	private void vote(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(ChatColor.stripColor(new Utils().processColors(Synergy.translateStringColorStripped("synergy-vote-monitorings"))));
        embed.setDescription(Synergy.translateStringColorStripped(String.join("\n", Synergy.getConfig().getStringList("votifier.monitorings"))));
        embed.setColor(Color.decode("#f1c40f"));
        event.replyEmbeds(embed.build()).queue();
    }


    private void link(SlashCommandInteractionEvent event) {
        TextInput subject = TextInput.create("username", Synergy.translateStringColorStripped("synergy-link-minecraft-your-username"), TextInputStyle.SHORT).setPlaceholder("Steve").setMinLength(3).setMaxLength(28).build();
        Modal modal = Modal.create("minecraftlink", Synergy.translateStringColorStripped("synergy-link-minecraft-title")).addComponents(new LayoutComponent[] {
            (LayoutComponent) ActionRow.of(new ItemComponent[] {
                (ItemComponent) subject
            })
        }).build();
        event.replyModal(modal).queue();
    }

    private static TextChannel channel = null;
    private static String message = null;

    private void embed(SlashCommandInteractionEvent event) {
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
            if (embed.getTitle() != null)
                title.setValue(embed.getTitle());
            if (embed.getDescription() != null)
                text.setValue(embed.getDescription());
            if (embed.getAuthor() != null)
                author.setValue(embed.getAuthor().getName());
            if (embed.getImage() != null)
                image.setValue(embed.getImage().getUrl());
            if (embed.getColor() != null)
                color.setValue("#" + Integer.toHexString(embed.getColor().getRGB()).substring(2));
        }

        Modal modal = Modal.create("embed", event.getOption("message") != null ? Synergy.translateStringColorStripped("synergy-discord-embed-edit") : Synergy.translateStringColorStripped("synergy-discord-embed-new")).addComponents(
            ActionRow.of(title.build()),
            ActionRow.of(text.build()),
            ActionRow.of(author.build()),
            ActionRow.of(image.build()),
            ActionRow.of(color.build())
        ).build();
        event.replyModal(modal).queue();
    }

    public String getDiscordIdByPlayername(String player) {
        for (String l: Synergy.getDataManager().getConfigurationSection("discord.links").getKeys(false)) {
            if (Synergy.getDataManager().getData("discord.links." + l).getAsString().equals(player)) {
                return l;
            }
        }
        return null;
    }

    public String getPlayernameByDiscordId(String id) {
        return Synergy.getDataManager().getData("discord.links." + id).getAsString();
    }

    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (event.getModalId().equals("minecraftlink")) {
            String username = event.getValue("username").getAsString();
            if (getDiscordIdByPlayername(username) != null) {
                event.reply(Synergy.translateStringColorStripped("synergy-link-minecraft-already-linked")).setEphemeral(true).queue();
                return;
            }
            Synergy.getDataManager().setData("players."+username+".confirm-discord", event.getUser().getId());
            event.reply(Synergy.translateStringColorStripped("synergy-link-minecraft-confirmation")).setEphemeral(true).queue();
            if (Bukkit.getPlayer(username) != null) {
            	Bukkit.getPlayer(username).sendMessage(Synergy.translateString("synergy-link-discord-confirmation", username).replace("%ACCOUNT%", event.getUser().getEffectiveName()));
            }
        }
        if (event.getModalId().equals("embed")) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(event.getValue("author") == null ? null : event.getValue("author").getAsString(), null, "https://minotar.net/helm/" + event.getValue("author").getAsString());
            builder.setTitle(event.getValue("title") == null ? null : event.getValue("title").getAsString());
            builder.setDescription(event.getValue("text") == null ? null : ChatColor.stripColor(new Utils().processColors(Synergy.translateStringColorStripped(event.getValue("text").getAsString()))));
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

    private void list(SlashCommandInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(Synergy.translateStringColorStripped("synergy-online-players-list"));
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
        builder.setDescription(ChatColor.stripColor(new Utils().processColors(Synergy.translateStringColorStripped(text))));
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
            case "confirm":
            	if (getPlayernameByDiscordId(event.getUser().getId()) == null) {
	                event.getUser().openPrivateChannel().complete().sendMessage(Synergy.translateStringColorStripped("synergy-confirmation-success").replace("%PLAYER%", id[2])).queue();
	                Synergy.createSynergyEvent("discord-link").setPlayer(id[2]).setArgument(id[0]).send();
            	}
                break;
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
        event.reply(Synergy.translateStringColorStripped("synergy-prune-messages-confirmation").replace("%AMOUNT%", String.valueOf(amount)))
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
            for (String part: messageParts) {
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
        List < String > parts = new ArrayList < > ();
        String[] words = message.split("\\s+");
        StringBuilder currentPart = new StringBuilder();

        for (String word: words) {
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

    public String getRoleIdByGroup(String group) {
        return Synergy.getConfig().getString("discord.synchronization.roles." + group);
    }

    public String getGroupByRoleId(String id) {
        ConfigurationSection roles = Synergy.getConfig().getConfigurationSection("discord.synchronization.roles");
        for (String r: roles.getKeys(false)) {
            if (Synergy.getConfig().getString("discord.synchronization.roles." + r).equals(id)) {
                return r;
            }
        }
        return null;
    }
    
    public void addVerifiedRole(String discordId) {
        if (Synergy.getConfig().getString("discord.synchronization.verified-role").length() == 19) {
        	try {
	        	Role verified = getJda().getRoleById(Synergy.getConfig().getString("discord.synchronization.verified-role"));
	        	Guild guild = verified.getGuild();
	        	Member member = guild.getMemberById(discordId);
	        	if (!member.getRoles().contains(verified)) {
	        		guild.addRoleToMember(member, verified).queue();
	        	}
        	} catch (Exception c) {
        		Synergy.getLogger().error(c.getMessage());
        	}
        }
    }

    @EventHandler
    public void onSynergyEvent(SynergyEvent event) {
    	
        if (event.getIdentifier().equals("discord-link")) {
            String player = event.getPlayer();
            String discordId = event.getArgument();
            if (Bukkit.getPlayer(player) != null)
                Bukkit.getPlayer(player).sendMessage("synergy-confirmation-success");
            Synergy.getDataManager().setData("discord.links." + discordId, player);
            addVerifiedRole(discordId);
        }

        if (event.getIdentifier().equals("sync-roles-from-mc-to-discord")) {
            syncRolesFromMcToDiscord(event.getPlayer(), event.getArgument());
        }

        if (event.getIdentifier().equals("sync-roles-from-discord-to-mc")) {
            syncRolesFromDiscordToMc(event.getPlayer());
        }

        if (event.getIdentifier().equals("clear-player-group")) {
            if (Synergy.getConfig().getBoolean("discord.synchronization.use-vault")) {
                for (String g: Synergy.getSpigotInstance().getPermissions().getPlayerGroups(Bukkit.getPlayer(event.getPlayer()))) {
                    Synergy.getSpigotInstance().getPermissions().playerRemoveGroup(Bukkit.getPlayer(event.getPlayer()), g);
                }
            } else {
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Synergy.getConfig().getString("discord.synchronization.custom-command-remove").replace("%PLAYER%", event.getPlayer()));
            }
        }

        if (event.getIdentifier().equals("set-player-group")) {
            if (Synergy.getConfig().getBoolean("discord.synchronization.use-vault")) {
                Synergy.getSpigotInstance().getPermissions().playerAddGroup(Bukkit.getPlayer(event.getPlayer()), event.getArgument());
            } else {
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Synergy.getConfig().getString("discord.synchronization.custom-command-add").replace("%PLAYER%", event.getPlayer()).replace("%GROUP%", event.getArgument()));
            }
        }
    }

    public void syncRolesFromMcToDiscord(String player, String group) {
        String roleId = getRoleIdByGroup(group);
        String discordId = getDiscordIdByPlayername(player);
        if (roleId != null && discordId != null) {
            try {
                Role role = getJda().getRoleById(roleId);
                Guild guild = role.getGuild();
                Member member = guild.getMemberById(discordId);
                for (String r: Synergy.getConfig().getConfigurationSection("discord.synchronization.roles").getKeys(false)) {
                    if (Synergy.getConfig().getString("discord.synchronization.roles." + r).length() == 19) {
                        try {
                            if (role != getJda().getRoleById(Synergy.getConfig().getString("discord.synchronization.roles." + r))) {
                                guild.removeRoleFromMember(member, getJda().getRoleById(Synergy.getConfig().getString("discord.synchronization.roles." + r))).queue();
                            }
                        } catch (Exception c) {
                            Synergy.getLogger().error(c.getMessage());
                        }
                    }
                }
                guild.addRoleToMember(member, role).queue();
            } catch (Exception c) {
                Synergy.getLogger().error(c.getMessage());
            }
        }
    }

    public void syncRolesFromDiscordToMc(String player) {
        try {
            String memberId = getDiscordIdByPlayername(player);
            for (String r: Synergy.getConfig().getConfigurationSection("discord.synchronization.roles").getKeys(false)) {
                if (Synergy.getConfig().getString("discord.synchronization.roles." + r).length() == 19) {
                    Role role = getJda().getRoleById(Synergy.getConfig().getString("discord.synchronization.roles." + r));
                    Guild guild = role.getGuild();
                    syncRolesFromDiscordToMc(guild.getMemberById(memberId));
                    return;
                }
            }
        } catch (Exception c) {
            Synergy.getLogger().error(c.getMessage());
        }
    }

    public void syncRolesFromDiscordToMc(Member member) {
        String player = getPlayernameByDiscordId(member.getId());
        Synergy.createSynergyEvent("clear-player-group").setPlayer(player).setWaitForPlayerIfOffline(true).send();
        for (Role r: member.getRoles()) {
            String group = getGroupByRoleId(r.getId());
            if (group != null && player != null) {
                Synergy.createSynergyEvent("set-player-group").setPlayer(player).setArgument(group).setWaitForPlayerIfOffline(true).send();
            }
        }
    }

    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (Synergy.getConfig().getBoolean("discord.synchronization.sync-roles-from-discord-to-mc")) {
            syncRolesFromDiscordToMc(event.getMember());
        }
    }

    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        if (Synergy.getConfig().getBoolean("discord.synchronization.sync-roles-from-discord-to-mc")) {
            syncRolesFromDiscordToMc(event.getMember());
        }
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        Member memder = event.getMember();
        String channelId = event.getChannel().getId();

        if (!event.getAuthor().isBot()) {

            if (channelId.equals(Synergy.getConfig().getString("discord.channels.global-chat-channel"))) {
                String sender = Synergy.getDataManager().getData("discord.links." + memder.getId()).getAsString();
                if (sender == null) {
                    event.getChannel().sendMessage(Synergy.translateStringColorStripped("synergy-you-have-to-link-account")).queue();
                    return;
                } else {
                    Synergy.createSynergyEvent("chat").setPlayer(sender).setArguments(new String[] {
                        "@" + message.getContentDisplay()
                    }).send();
                }
            }

            if (channelId.equals(Synergy.getConfig().getString("discord.channels.admin-chat-channel"))) {
                OfflinePlayer sender = Synergy.getVault().getPlayersWithPermission("synergy.discord." + memder.getId()).get(0);
                Synergy.createSynergyEvent("chat").setPlayer(sender.getName()).setArguments(new String[] {
                    "$" + message.getContentDisplay()
                }).send();
            }

            if (Synergy.getConfig().getBoolean("discord.gpt-bot.enabled")) {

                try {
                    boolean startsWithBotName = message.getContentDisplay().toLowerCase().startsWith(getBotName().toLowerCase());
                    boolean isGlobalChatChannel = channelId.equals(Synergy.getConfig().getString("discord.channels.global-chat-channel"));
                    boolean mentionedBot = message.getMentions().isMentioned((IMentionable) event.getJDA().getSelfUser(), Message.MentionType.USER);
                    boolean isReplyToBot = message.getReferencedMessage() != null && message.getReferencedMessage().getAuthor().equals(event.getJDA().getSelfUser());

                    if ((startsWithBotName && !isGlobalChatChannel) || mentionedBot || isReplyToBot) {
                        message.getChannel().sendTyping().queue();
                        String question = Synergy.getConfig().getString("discord.gpt-bot.personality")
                            .replace("%MESSAGE%", Synergy.getUtils().removeIgnoringCase(getBotName(), event.getMessage().getContentRaw())
                                .replace(event.getJDA().getSelfUser().getAsMention(), ""));
                        Synergy.debug(question);
                        //String.valueOf((event.getMessage().getReferencedMessage() != null) ? event.getMessage().getReferencedMessage().getContentRaw() : "");
                        String answer = new OpenAi().newPrompt(question).get(0).getText().replace("\"", "");
                        message.reply(answer).queue();
                    }

                    if (startsWithBotName && isGlobalChatChannel) {
                        String question = Synergy.getConfig().getString("discord.gpt-bot.personality").replace("%MESSAGE%", Synergy.getUtils().removeIgnoringCase(getBotName(), message.getContentDisplay()));
                        String answer = (new OpenAi().newPrompt(question).get(0)).getText().replace("\"", "").trim();
                        Synergy.createSynergyEvent("chat").setPlayer(getBotName().replace(" ", "_")).setArguments(new String[] {
                            "@" + answer
                        }).send();
                        Synergy.createSynergyEvent("discord").setPlayer(getBotName().replace(" ", "_")).setArguments(new String[] {
                            "!" + answer
                        }).send();
                    }

                } catch (Exception c) {
                    Synergy.getLogger().error(c.getMessage());
                    event.getMessage().reply(Synergy.translateStringColorStripped("synergy-service-unavailable")).queue();
                }
            }

            if (Synergy.getConfig().getBoolean("discord.hightlights.enabled") &&
                Synergy.getConfig().getStringList("discord.hightlights.channels").contains(channelId) &&
                message.getAttachments().size() > 0) {
                message.addReaction((Emoji) Emoji.fromUnicode(Synergy.getConfig().getString("discord.hightlights.reaction-emoji"))).complete();
                message.createThreadChannel(Synergy.translateStringColorStripped("synergy-hightlights-comments")).queue();
            }
            return;
        }
    }

    public String getBotName() {
        return Synergy.getConfig().getString("discord.gpt-bot.name");
    }

}