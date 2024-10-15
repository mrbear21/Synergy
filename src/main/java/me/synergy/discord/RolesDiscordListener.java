package me.synergy.discord;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import me.synergy.anotations.SynergyHandler;
import me.synergy.anotations.SynergyListener;
import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RolesDiscordListener extends ListenerAdapter implements SynergyListener {

	public RolesDiscordListener() {
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
	
    @Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (Synergy.getConfig().getBoolean("discord.synchronization.sync-roles-from-discord-to-mc")) {
        	syncRolesFromDiscordToMc(event.getMember());
        }
    }

    @Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        if (Synergy.getConfig().getBoolean("discord.synchronization.sync-roles-from-discord-to-mc")) {
        	syncRolesFromDiscordToMc(event.getMember());
        }
    }
    
    @SynergyHandler
    public void onSynergyEvent(SynergyEvent event) {
    	
        if (event.getIdentifier().equals("sync-roles-from-mc-to-discord")) {
        	syncRolesFromMcToDiscord(event.getPlayerUniqueId(), event.getOption("group").getAsString());
        }

        if (event.getIdentifier().equals("sync-roles-from-discord-to-mc")) {
        	syncRolesFromDiscordToMc(event.getPlayerUniqueId());
        }

        if (event.getIdentifier().equals("add-verified-role")) {
            if (Synergy.getConfig().getString("discord.synchronization.verified-role").length() == 19) {
            	try {
    	        	Role verified = Synergy.getDiscord().getRoleById(Synergy.getConfig().getString("discord.synchronization.verified-role"));
    	        	Guild guild = verified.getGuild();
    	        	Member member = guild.getMemberById(event.getOption("id").getAsString());
    	        	if (!member.getRoles().contains(verified)) {
    	        		guild.addRoleToMember(member, verified).queue();
    	        	}
            	} catch (Exception c) {
            		Synergy.getLogger().error(c.getMessage());
            	}
            }
        }
    }
    
    public static void addVerifiedRole(String discordId) {
    	Synergy.createSynergyEvent("add-verified-role").setOption("id", discordId).send();
    }
    
    public static void syncRolesFromDiscordToMc(UUID uuid) {
        try {
            String memberId = Discord.getDiscordIdByUniqueId(uuid);
            for (String r: Synergy.getConfig().getConfigurationSection("discord.synchronization.roles").keySet()) {
                if (Synergy.getConfig().getString("discord.synchronization.roles." + r).length() == 19) {
                    Role role = Synergy.getDiscord().getRoleById(Synergy.getConfig().getString("discord.synchronization.roles." + r));
                    Guild guild = role.getGuild();
                    syncRolesFromDiscordToMc(guild.getMemberById(memberId));
                    return;
                }
            }
        } catch (Exception c) {
            Synergy.getLogger().error(c.getMessage());
        }
    }

    public static void syncRolesFromDiscordToMc(Member member) {
        try {
	    	if (Discord.getUniqueIdByDiscordId(member.getId()) != null) {
		        UUID uuid = Discord.getUniqueIdByDiscordId(member.getId());
		        if (uuid != null) {
			        for (String group : getGroups()) {
				        Synergy.createSynergyEvent("remove-player-group").setPlayerUniqueId(uuid).setOption("group", group).send();
		            }
			        for (Role r: member.getRoles()) {
			            String group = getGroupByRoleId(r.getId());
			            if (group != null) {
			                Synergy.createSynergyEvent("set-player-group").setPlayerUniqueId(uuid).setOption("group", group).send();
			            }
			        }
		        }
	    	}
        } catch (Exception c) {
            Synergy.getLogger().error(c.getMessage());
        }
    }

    public static void syncRolesFromMcToDiscord(UUID uuid, String group) {
        String roleId = getRoleIdByGroup(group);
        String discordId = Discord.getDiscordIdByUniqueId(uuid);
        if (roleId != null && discordId != null) {
            try {
                Role role = Synergy.getDiscord().getRoleById(roleId);
                Guild guild = role.getGuild();
                Member member = guild.getMemberById(discordId);
                for (String r: Synergy.getConfig().getConfigurationSection("discord.synchronization.roles").keySet()) {
                    if (Synergy.getConfig().getString("discord.synchronization.roles." + r).length() == 19) {
                        try {
                            if (role != Synergy.getDiscord().getRoleById(Synergy.getConfig().getString("discord.synchronization.roles." + r))) {
                                guild.removeRoleFromMember(member, Synergy.getDiscord().getRoleById(Synergy.getConfig().getString("discord.synchronization.roles." + r))).queue();
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

    public static String getRoleIdByGroup(String group) {
        return Synergy.getConfig().getString("discord.synchronization.roles." + group);
    }

    public static String getGroupByRoleId(String id) {
        Set<String> roles = Synergy.getConfig().getConfigurationSection("discord.synchronization.roles").keySet();
        for (String r: roles) {
            if (Synergy.getConfig().getString("discord.synchronization.roles." + r).equals(id)) {
                return r;
            }
        }
        return null;
    }

    public static List<String> getGroups() {
        Set<String> roles = Synergy.getConfig().getConfigurationSection("discord.synchronization.roles").keySet();
        List<String> groups = new ArrayList<>();
        for (String r : roles) {
            if (Synergy.getConfig().getString("discord.synchronization.roles." + r).length() == 19) {
            	groups.add(r);
            }
        }
        return groups;
    }
    
}
