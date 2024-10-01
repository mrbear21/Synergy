package me.synergy.handlers;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import me.synergy.anotations.SynergyHandler;
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
        
        Synergy.getEventManager().registerEvents(this);
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigot());
        Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
    }

    @SynergyHandler
    public void onSynergyPluginMessage(SynergyEvent event) {
        if (!event.getIdentifier().equals("votifier")) {
            return;
        }

        String service = event.getOption("service").getAsString();
        String username = event.getOption("username").getAsString();
        BreadMaker bread = Synergy.getBread(Synergy.getSpigot().getUniqueIdFromName(username));

        if (bread.isOnline()) {
        	bread.sendMessage(Translation.processLangTags("<lang>synergy-voted-successfully</lang>", bread.getLanguage()).replace("%SERVICE%", service));
        }

    	Synergy.createSynergyEvent("broadcast").setOption("message", "<lang>synergy-player-voted<arg>"+bread.getName()+"</arg></lang>").send();
    	Synergy.createSynergyEvent("discord-broadcast").setOption("message", "<lang>synergy-player-voted<arg>"+bread.getName()+"</arg></lang>").send();

        for (String command : Synergy.getConfig().getStringList("votifier.rewards")) {
        	Synergy.executeConsoleCommand(command.replace("%PLAYER%", bread.getName()));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        Synergy.createSynergyEvent("votifier").setOption("service", vote.getServiceName()).setOption("username", vote.getUsername()).send();
    }
}