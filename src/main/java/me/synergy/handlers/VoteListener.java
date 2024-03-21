package me.synergy.handlers;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import me.synergy.brains.Synergy;
import me.synergy.events.SynergyEvent;
import me.synergy.objects.BreadMaker;

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
        
        UUID uuid = event.getPlayerUniqueId();
        String service = event.getOption("service");
        BreadMaker bread = event.getBread();
        
        if (Bukkit.getPlayer(uuid) != null) {
            Bukkit.getPlayer(uuid).sendMessage(Synergy.translateString(Synergy.getConfig().getString("votifier.message")).replace("%SERVICE%", service));
        }
        
        for (String command: Synergy.getConfig().getStringList("votifier.rewards")) {
            Bukkit.dispatchCommand((CommandSender) Bukkit.getServer().getConsoleSender(), command.replace("%PLAYER%", bread.getName()));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        UUID uuid = Synergy.getUniqueIdFromName(vote.getUsername());
        Synergy.createSynergyEvent("votifier").setPlayerUniqueId(uuid).setWaitForPlayerIfOffline(true).setOption("service", vote.getServiceName()).send();
    }
}