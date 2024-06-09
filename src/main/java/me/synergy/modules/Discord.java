package me.synergy.modules;

import java.util.ArrayList;
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
                }, 0, 5, TimeUnit.MINUTES);
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
                    .forEach(member -> userTagsCache.add(member.getUser().getName()))
            );
            lastCacheUpdate = System.currentTimeMillis();
        }
        return List.copyOf(userTagsCache);
    }

    
    
    public String getDiscordIdByUniqueId(UUID uuid) {
    	ConfigurationSection links = Synergy.getDataManager().getConfigurationSection("discord.links");
    	if (links != null) {
	        for (String l: links.getKeys(false)) {
	            if (Synergy.getDataManager().getData("discord.links." + l).getAsString().equals(uuid.toString())) {
	                return l;
	            }
	        }
    	}
        return null;
    }

    public UUID getUniqueIdByDiscordId(String id) {
    	if (Synergy.getDataManager().isSet("discord.links." + id)) {
    		return Synergy.getDataManager().getData("discord.links." + id).getAsUUID();
    	}
        return null;
    }
    
    public void syncRolesFromMcToDiscord(UUID uuid, String group) {
        String roleId = getRoleIdByGroup(group);
        String discordId = getDiscordIdByUniqueId(uuid);
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

    public void syncRolesFromDiscordToMc(UUID uuid) {
        try {
            String memberId = getDiscordIdByUniqueId(uuid);
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
	    	if (getUniqueIdByDiscordId(member.getId()) != null) {
		        UUID uuid = getUniqueIdByDiscordId(member.getId());
		        if (uuid != null) {
			        for (String group : getGroups()) {
				        Synergy.createSynergyEvent("remove-player-group").setPlayerUniqueId(uuid).setOption("group", group).setWaitForPlayerIfOffline(true).send();
		            }
			        for (Role r: member.getRoles()) {
			            String group = getGroupByRoleId(r.getId());
			            if (group != null) {
			                Synergy.createSynergyEvent("set-player-group").setPlayerUniqueId(uuid).setOption("group", group).setWaitForPlayerIfOffline(true).send();
			            }
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
    
    public List<String> getGroups() {
        ConfigurationSection roles = Synergy.getConfig().getConfigurationSection("discord.synchronization.roles");
        List<String> groups = new ArrayList<String>();
        for (String r: roles.getKeys(false)) {
            if (Synergy.getConfig().getString("discord.synchronization.roles." + r).length() == 19) {
            	groups.add(r);
            }
        }
        return groups;
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
    
    public void removeDiscordLink(UUID uuid) {
    	BreadMaker bread = new BreadMaker(uuid);
        for (String l: Synergy.getDataManager().getConfigurationSection("discord.links").getKeys(false)) {
            if (Synergy.getDataManager().getData("discord.links." + l).getAsUUID().equals(uuid)) {
                Synergy.getDataManager().setData("discord.links." + l, null);
                bread.sendMessage("<lang>synergy-link-minecraft-unlinked</lang>");
                return;
            }
        }
        bread.sendMessage("<lang>synergy-you-have-no-linked-accounts</lang>");
    }

    public void createDiscordLink(UUID uuid, String discordId) {
    	BreadMaker bread = new BreadMaker(uuid);
    	Synergy.getDataManager().setData("discord.links." + discordId, uuid.toString());
		String account = Synergy.getDiscord().getJda().getUserById(Synergy.getDiscord().getDiscordIdByUniqueId(uuid)).getEffectiveName();
    	bread.sendMessage(bread.translateString("synergy-discord-link-success").replace("%ACCOUNT%", account));
    	addVerifiedRole(discordId);
    }
    
    public void confirmDiscordLink(UUID uuid) {
    	BreadMaker bread = new BreadMaker(uuid);
		if (bread.getData("confirm-discord").isSet()) {
	    	String discordid = bread.getData("confirm-discord").getAsString();
	    	createDiscordLink(uuid, discordid);
	    	bread.setData("confirm-discord", null);
		} else {
			bread.sendMessage("<lang>synergy-confirmation-nothing-to-confirm</lang>");
		}
    }
    
    public void makeDiscordLink(UUID uuid, String discordTag) {
    	BreadMaker bread = new BreadMaker(uuid);
		
       	if (Synergy.getDiscord().getDiscordIdByUniqueId(uuid) != null) {
    		String account = Synergy.getDiscord().getJda().getUserById(Synergy.getDiscord().getDiscordIdByUniqueId(uuid)).getEffectiveName();
    		bread.sendMessage(bread.translateString("synergy-link-discord-already-linked").replace("%ACCOUNT%", account));
    		return;
    	}

        try {
        	for (Guild guild : Synergy.getDiscord().getJda().getGuilds()) {
        	    for (Member member : guild.getMembers()) {
        	        if (member.getUser().getName().equalsIgnoreCase(discordTag)) {
        	            User user = member.getUser();

        	            if (Synergy.getDiscord().getUniqueIdByDiscordId(user.getId()) != null) {
        	            	bread.sendMessage(bread.translateString("synergy-link-minecraft-already-linked").replace("%ACCOUNT%", bread.getName()));
        	                return;
        	            }
        	            
        	            PrivateChannel privateChannel = user.openPrivateChannel().complete();
        	            String message = bread.translateStringColorStripped("<lang>synergy-discord-confirm-link</lang>").replace("%ACCOUNT%", bread.getName());
        	            
        	            MessageHistory history = privateChannel.getHistory();
        	            Message lastMessage = history.retrievePast(1).complete().size() == 0 ? null : history.retrievePast(1).complete().get(0);
        	            
        	            if (lastMessage == null || !lastMessage.getContentRaw().equals(message)) {
        	                if (privateChannel.canTalk()) {
        	                    privateChannel.sendMessage(message)
        	                            .addActionRow(
        	                                    Button.success(user.getId() + ":confirm:" + uuid, bread.translateStringColorStripped("<lang>synergy-confirm-action</lang>")))
        	                            .queue();
        	                    bread.sendMessage(bread.translateString("synergy-discord-link-check-pm").replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
        	                } else {
        	                	bread.sendMessage(bread.translateString("synergy-discord-use-link-cmd").replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
        	                }
        	            } else {
        	            	bread.sendMessage(bread.translateString("synergy-discord-use-link-cmd").replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
        	            }
        	            return;
        	        }
        	    }
        	}
        } catch (Exception c) {
        	c.printStackTrace();
        	bread.sendMessage(bread.translateStringColorStripped("<lang>synergy-discord-use-link-cmd</lang>").replace("%INVITE%", Synergy.getConfig().getString("discord.invite-link")));
        }
    }
    
    

}