package me.synergy.discord;

import java.awt.Color;

import javax.annotation.Nonnull;

import me.synergy.brains.Synergy;
import me.synergy.utils.Translation;
import me.synergy.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInput.Builder;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class EmbedDiscordCommand extends ListenerAdapter {

	public EmbedDiscordCommand() {
		
        if (!Synergy.getConfig().getBoolean("discord.enabled")) {
            return;
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
               /* case "balance":
                	balance(event);
                    break;*/
                case "embed":
                	embed(event);
                    break;
                default:
                    event.replyEmbeds(Discord.warning(Synergy.translate("<lang>synergy-service-unavailable</lang>", Translation.getDefaultLanguage()).getStripped())).setEphemeral(true).queue();
            }
        } catch (Exception c) {
            event.replyEmbeds(Discord.warning(Synergy.translate("<lang>synergy-service-unavailable</lang>", Translation.getDefaultLanguage()) + " (*" + c.getMessage() + "*)")).setEphemeral(true).queue();
        }
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
/*
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
	}*/


    private static TextChannel channel = null;
    private static String message = null;

    

    @Override
	public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
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

	public void updateCommands() {
        CommandListUpdateAction commands = Synergy.getDiscord().updateCommands();
        commands.addCommands(new CommandData[] {
            Commands.slash("post", "Create a post") //Synergy.translate("<lang>synergy-create-post</lang>", Translation.getDefaultLanguage()).getStripped())
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
/*
        if (Synergy.isDependencyAvailable("Vault")) {
	        commands.addCommands(new CommandData[] {
	            Commands.slash("balance", Synergy.translate("<lang>synergy-check-vault-balance</lang>", Translation.getDefaultLanguage()).getStripped())
	                .setGuildOnly(true)
	        });
        }
*/





     /*   commands.addCommands(new CommandData[] {
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
        });*/
        commands.queue();
	}


}
