package me.synergy.handlers;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import me.synergy.brains.Spigot;
import me.synergy.brains.Synergy;
import me.synergy.events.SynergyPluginEvent;
    
public class VoteListener implements Listener {
    
	private Spigot spigot;
	  
	public VoteListener(Spigot spigot) {
		this.spigot = spigot;
	}
	
	public void initialize() {
		if (!spigot.getConfig().getBoolean("votifier.enabled")) {
			return;
		}
		if (!spigot.isDependencyAvailable("Votifier")) {
			spigot.getLogger().warning("Votifier is required to initialize "+this.getClass().getSimpleName()+" module!");
			return;
		}
		Bukkit.getPluginManager().registerEvents(new VoteListener(spigot), spigot);
		spigot.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
	}
		
    @EventHandler
    public void onSynergyPluginMessage(SynergyPluginEvent event) {
        if (!event.getIdentifier().equals("votifier")) {
            return;
        }

        String player = event.getPlayer();
        //String serviceName = event.getArgs()[1];
        
        if (Bukkit.getPlayer(player) != null) {
        	Bukkit.getPlayer(player).sendMessage(spigot.getConfig().getString("votifier.message"));
        }
		for (String command : spigot.getConfig().getStringList("votifier.rewards")) {
			Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replace("%PLAYER%", player));
		}
    }
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onVotifierEvent(VotifierEvent event) {
		Vote vote = event.getVote();
		Synergy.createSynergyEvent("votifier").setPlayer(vote.getUsername()).setArguments(new String[] {vote.getServiceName()}).send(spigot);
	}
}