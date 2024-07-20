package me.synergy.handlers;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.Translation;

public class VoteListener implements Listener {

    public VoteListener() {

    }

    public void initialize() {
        if (!Synergy.getConfig().getBoolean("votifier.enabled")) {
            return;
        }
        if (!Synergy.isDependencyAvailable("Votifier")) {
            Synergy.getLogger().warning("NuVotifier is required to initialize " + getClass().getSimpleName() + " module!");
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());
        Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
    }

    @EventHandler
    public void onSynergyPluginMessage(SynergyEvent event) {
        if (!event.getIdentifier().equals("votifier")) {
            return;
        }
        
        String service = event.getOption("service").getAsString();
        BreadMaker bread = event.getBread();
        
        if (bread.isOnline()) {
        	bread.sendMessage(Translation.processLangTags("<lang>synergy-voted-successfully</lang>", bread.getLanguage()).replace("%SERVICE%", service));
        }
        
    	Synergy.createSynergyEvent("announcement").setOption("message", "<lang>synergy-player-voted</lang>").setOption("argument", bread.getName()).send();
    	Synergy.createSynergyEvent("discord-announcement").setOption("message", "<lang>synergy-player-voted</lang>").setOption("argument", bread.getName()).send();
          
        for (String command : Synergy.getConfig().getStringList("votifier.rewards")) {
        	Synergy.executeConsoleCommand(command.replace("%PLAYER%", bread.getName()));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        UUID uuid = Synergy.getUniqueIdFromName(vote.getUsername());
        Synergy.createSynergyEvent("votifier").setPlayerUniqueId(uuid).setOption("service", vote.getServiceName()).send();
    }
}