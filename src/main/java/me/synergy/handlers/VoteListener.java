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
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigotInstance());
        Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
    }

    @EventHandler
    public void onSynergyPluginMessage(SynergyEvent event) {
        if (!event.getIdentifier().equals("votifier")) {
            return;
        }
        
        UUID player = event.getUniqueId();
        String service = event.getArgument();
        BreadMaker bread = event.getBread();
        
        if (Bukkit.getPlayer(player) != null) {
            Bukkit.getPlayer(player).sendMessage(Synergy.translateString(Synergy.getConfig().getString("votifier.message")).replace("%SERVICE%", service));
        }
        
        for (String command: Synergy.getConfig().getStringList("votifier.rewards")) {
            Bukkit.dispatchCommand((CommandSender) Bukkit.getServer().getConsoleSender(), command.replace("%PLAYER%", bread.getName()));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        UUID uuid = UUID.nameUUIDFromBytes(vote.getUsername().getBytes());
        Synergy.createSynergyEvent("votifier").setUniqueId(uuid).setWaitForPlayerIfOffline(true).setArgument(vote.getServiceName()).send();
    }
}