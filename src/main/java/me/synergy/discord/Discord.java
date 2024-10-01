package me.synergy.discord;

import java.awt.Color;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import me.synergy.brains.Synergy;
import me.synergy.integrations.PlaceholdersAPI;
import me.synergy.objects.DataObject;
import me.synergy.utils.Translation;
import me.synergy.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Discord {

    private static JDA JDA;
    ScheduledExecutorService REPEATING_TASK = Executors.newSingleThreadScheduledExecutor();
    public static Set<String> USERS_TAGS_CACHE = new CopyOnWriteArraySet<>();

    private static final GatewayIntent[] INTENTS = new GatewayIntent[] {
        GatewayIntent.SCHEDULED_EVENTS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES
    };

    public void initialize() {
        try {
            if (!Synergy.getConfig().getBoolean("discord.enabled")) {
                return;
            }

            JDABuilder bot = botBuilder();
            
            bot.addEventListeners(new ListDiscordCommand());
            bot.addEventListeners(new LinkDiscordCommand());
            bot.addEventListeners(new VoteDiscordCommand());
            bot.addEventListeners(new RolesDiscordListener());
            bot.addEventListeners(new ChatDiscordListener());
      
            JDA = bot.build();
            
            updateCommands();

            activityStatus();

            Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
        } catch (Exception exception) {
            Synergy.getLogger().error(String.valueOf(getClass().getSimpleName()) + " module failed to initialize: " + exception.getMessage());
        	exception.printStackTrace();
        }
    }
    
    private void updateCommands() {
    	
        CommandListUpdateAction commands = Synergy.getDiscord().updateCommands();
        
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
        
        commands.queue();
    }
    
    private JDABuilder botBuilder() {
       return JDABuilder.create(Synergy.getConfig().getString("discord.bot-token"), Arrays.asList(INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, new CacheFlag[] {
                    CacheFlag.VOICE_STATE
                })
                .disableCache(CacheFlag.ACTIVITY, new CacheFlag[] {
                    CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.ONLINE_STATUS, CacheFlag.STICKER
                })
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.customStatus(Synergy.getConfig().getStringList("discord.activities").get(0)))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setBulkDeleteSplittingEnabled(true);
    }
    
    public void shutdown() {
        if (getJda() != null) {
            System.out.println("Registered listeners before shutdown: " + getJda().getRegisteredListeners().size());
            for (Object listener : getJda().getRegisteredListeners()) {
                getJda().removeEventListener(listener);
            }
            System.out.println("Registered listeners after removal: " + getJda().getRegisteredListeners().size());
            
            getJda().shutdownNow();
        }
    }

    
    private void activityStatus() {
        if (Synergy.getConfig().getStringList("discord.activities").size() > 1) {
        	REPEATING_TASK.scheduleAtFixedRate(() -> {
                long currentTimeSeconds = System.currentTimeMillis() / 1000;
                int index = (int)(currentTimeSeconds % Synergy.getConfig().getStringList("discord.activities").size());
                String customStatusText = Synergy.getConfig().getStringList("discord.activities").get(index);
                customStatusText = PlaceholdersAPI.processPlaceholders(null, customStatusText);
                customStatusText = customStatusText.replace("%online%", String.valueOf(Utils.getPlayers().size()));
                Discord.JDA.getPresence().setActivity(Activity.customStatus(customStatusText));
            }, 0, 60, TimeUnit.SECONDS);
        }
    }
    
    private static long LAST_CACHE_UPDATE = 0;
    private static long CACHE_UPDATE_INTERVAL = 60000;
    

    public static Set<String> getUsersTagsCache() {
        if (System.currentTimeMillis() - LAST_CACHE_UPDATE >= CACHE_UPDATE_INTERVAL) {
        	LAST_CACHE_UPDATE = System.currentTimeMillis();
        	Synergy.createSynergyEvent("retrieve-users-tags").send();
        }
        return USERS_TAGS_CACHE;
    }

    public static String getDiscordIdByUniqueId(UUID uuid) {
    	Set<String> links = Synergy.getDataManager().getConfigurationSection("discord.links").keySet();
    	if (links != null) {
	        for (String link: links) {
	            if (new DataObject(Synergy.getDataManager().get("discord.links." + link)).getAsString().equals(uuid.toString())) {
	                return link;
	            }
	        }
    	}
        return null;
    }
    
    public static UUID getUniqueIdByDiscordId(String id) {
    	if (Synergy.getDataManager().isSet("discord.links." + id)) {
    		return new DataObject(Synergy.getDataManager().get("discord.links." + id)).getAsUUID();
    	}
        return null;
    }
    
    public static String getBotName() {
        return Synergy.getConfig().getString("discord.gpt-bot.name");
    }

    public static MessageEmbed info(String message) {
    	EmbedBuilder embed = new EmbedBuilder();
    	embed.setColor(Color.decode("#3498db"));
    	embed.setTitle(message);
    	return embed.build();
    }

    public static MessageEmbed warning(String message) {
    	EmbedBuilder embed = new EmbedBuilder();
    	embed.setColor(Color.decode("#f39c12"));
    	embed.setTitle(message);
    	return embed.build();
    }
    
    public JDA getJda() {
        return JDA;
    }

}