package me.synergy.modules;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bukkit.configuration.ConfigurationSection;

import me.clip.placeholderapi.PlaceholderAPI;
import me.synergy.brains.Synergy;
import me.synergy.commands.DiscordCommand;
import me.synergy.handlers.DiscordListener;
import me.synergy.objects.BreadMaker;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Discord {

    private static JDA JDA;
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private Set<String> userTagsCache = new CopyOnWriteArraySet<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_UPDATE_INTERVAL = 60000;
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
                .enableCache(CacheFlag.MEMBER_OVERRIDES, new CacheFlag[] {
                    CacheFlag.VOICE_STATE
                })
                .disableCache(CacheFlag.ACTIVITY, new CacheFlag[] {
                    CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.ONLINE_STATUS, CacheFlag.STICKER
                })
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.customStatus(Synergy.getConfig().getStringList("discord.activities").get(0)))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(new DiscordListener())
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

            new DiscordListener().updateCommands();
            if (Synergy.isSpigot()) {
	            new DiscordListener().initialize();
	            new DiscordCommand().initialize();
            }

            Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
        } catch (Exception exception) {
            Synergy.getLogger().error(String.valueOf(getClass().getSimpleName()) + " module failed to initialize: " + exception.getMessage());
        	exception.printStackTrace();
        }
    }

    public JDA getJda() {
        return JDA;
    }
    
    public void shutdown() {
        if (getJda() != null) {
        	getJda().removeEventListener(new DiscordListener());
            getJda().shutdownNow();
        }
    }

    public List<String> getAllUserTags() {
        if (System.currentTimeMillis() - lastCacheUpdate >= CACHE_UPDATE_INTERVAL) {
            userTagsCache.clear();
            getJda().getGuilds().forEach(guild ->
                guild.getMembers().stream()
                    .filter(member -> !member.getUser().isBot())
                    .forEach(member -> userTagsCache.add(member.getUser().getEffectiveName()))
            );
            lastCacheUpdate = System.currentTimeMillis();
        }
        return List.copyOf(userTagsCache);
    }

    public String getDiscordIdByUUID(UUID player) {
        for (String l: Synergy.getDataManager().getConfigurationSection("discord.links").getKeys(false)) {
            if (Synergy.getDataManager().getData("discord.links." + l).getAsString().equals(player.toString())) {
                return l;
            }
        }
        return null;
    }

    public UUID getUUIDByDiscordId(String id) {
    	UUID uuid = UUID.nameUUIDFromBytes(Synergy.getDataManager().getData("discord.links." + id).getAsString().getBytes());
        return uuid;
    }
    
    public void syncRolesFromMcToDiscord(UUID player, String group) {
        String roleId = getRoleIdByGroup(group);
        String discordId = getDiscordIdByUUID(player);
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

    public void syncRolesFromDiscordToMc(UUID player) {
        try {
            String memberId = getDiscordIdByUUID(player);
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
        try {
	    	if (getUUIDByDiscordId(member.getId()) != null) {
		        UUID player = getUUIDByDiscordId(member.getId());
		        Synergy.createSynergyEvent("clear-player-group").setUniqueId(player).setWaitForPlayerIfOffline(true).send();
		        for (Role r: member.getRoles()) {
		            String group = getGroupByRoleId(r.getId());
		            if (group != null && player != null) {
		                Synergy.createSynergyEvent("set-player-group").setUniqueId(player).setArgument(group).setWaitForPlayerIfOffline(true).send();
		            }
		        }
	    	}
        } catch (Exception c) {
            Synergy.getLogger().error(c.getMessage());
        }
    }

    public String getBotName() {
        return Synergy.getConfig().getString("discord.gpt-bot.name");
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
    
    public void removeDiscordLink(UUID player) {
    	BreadMaker bread = new BreadMaker(player);
        for (String l: Synergy.getDataManager().getConfigurationSection("discord.links").getKeys(false)) {
            if (Synergy.getDataManager().getData("discord.links." + l).getAsString().equals(player)) {
                Synergy.getDataManager().setData("discord.links." + l, null);
                bread.sendMessage(Synergy.translateString("synergy-link-minecraft-unlinked", player));
                return;
            }
        }
        bread.sendMessage("synergy-you-have-no-linked-accounts");
    }

    public void createDiscordLink(UUID player, String discordId) {
    	BreadMaker bread = new BreadMaker(player);
    	Synergy.getDataManager().setData("discord.links." + discordId, player.toString());
		String account = Synergy.getDiscord().getJda().getUserById(Synergy.getDiscord().getDiscordIdByUUID(player)).getEffectiveName();
    	bread.sendMessage(Synergy.translateString("synergy-discord-link-success").replace("%ACCOUNT%", account));
    	addVerifiedRole(discordId);
    }
    
    public void confirmDiscordLink(UUID player) {
    	BreadMaker bread = new BreadMaker(player);
		if (Synergy.getDataManager().getConfig().isSet("players."+player+".confirm-discord")) {
	    	String discordid = Synergy.getDataManager().getData("players."+player+".confirm-discord").getAsString();
	    	createDiscordLink(player, discordid);
	    	Synergy.getDataManager().setData("players."+player+".confirm-discord", null);
		} else {
			bread.sendMessage("synergy-confirmation-nothing-to-confirm");
		}
    }
    
    public void makeDiscordLink(UUID player, String discordTag) {
    	BreadMaker bread = new BreadMaker(player);
		
       	if (Synergy.getDiscord().getDiscordIdByUUID(player) != null) {
    		String account = Synergy.getDiscord().getJda().getUserById(Synergy.getDiscord().getDiscordIdByUUID(player)).getEffectiveName();
    		bread.sendMessage(Synergy.translateString("synergy-link-discord-already-linked").replace("%ACCOUNT%", account));
    		return;
    	}

        try {
        	for (Guild guild : Synergy.getDiscord().getJda().getGuilds()) {
        	    for (Member member : guild.getMembers()) {
        	        if (member.getEffectiveName().equalsIgnoreCase(discordTag)) {
        	            User user = member.getUser();

    	            	String account = UUID.fromString(Synergy.getDiscord().getUUIDByDiscordId(user.getId()).toString()).toString();
    	            	
        	            if (Synergy.getDiscord().getUUIDByDiscordId(user.getId()) != null) {
        	            	bread.sendMessage(Synergy.translateString("synergy-link-minecraft-already-linked").replace("%ACCOUNT%", account));
        	                return;
        	            }
        	            
        	            PrivateChannel privateChannel = user.openPrivateChannel().complete();
        	            String message = Synergy.translateStringColorStripped("synergy-discord-confirm-link").replace("%ACCOUNT%", account);
        	            
        	            MessageHistory history = privateChannel.getHistory();
        	            Message lastMessage = history.retrievePast(1).complete().size() == 0 ? null : history.retrievePast(1).complete().get(0);
        	            
        	            System.out.println(lastMessage.getContentRaw() + " => " + message);
        	            
        	            if (lastMessage == null || !lastMessage.getContentRaw().equals(message)) {
        	                if (privateChannel.canTalk()) {
        	                    privateChannel.sendMessage(message)
        	                            .addActionRow(
        	                                    Button.success(user.getId() + ":confirm:" + player, Synergy.translateStringColorStripped("synergy-confirm-action")))
        	                            .queue();
        	                    bread.sendMessage(Synergy.translateString("synergy-discord-link-check-pm", player).replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
        	                } else {
        	                	bread.sendMessage(Synergy.translateString("synergy-discord-use-link-cmd", player).replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
        	                }
        	            } else {
        	            	bread.sendMessage(Synergy.translateString("synergy-discord-use-link-cmd", player).replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
        	            }
        	            return;
        	        }
        	    }
        	}
        } catch (Exception c) {
        	c.printStackTrace();
        	bread.sendMessage(Synergy.translateStringColorStripped("synergy-discord-use-link-cmd").replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
        }
    }
    
    

}