package me.synergy.events;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.ArrayList;
import java.util.List;
import me.synergy.brains.Synergy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class SynergyEvent extends Event implements Listener {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private String identifier;

    private String[] args;

    private String player;

    private boolean waitForPlayer = false;

    public SynergyEvent(String identifier, String[] args) {
        this.identifier = identifier;
        this.args = args;
    }

    public SynergyEvent(String identifier) {
        this.identifier = identifier;
    }

    public SynergyEvent(String identifier, String player, String[] args) {
        this.identifier = identifier;
        this.args = args;
        this.player = player;
    }

    public SynergyEvent() {}

    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Synergy.getSpigotInstance());
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String[] getArgs() {
        (new String[1])[0] = "N/A";
        return (this.args.length > 0) ? this.args : new String[1];
    }

    public String getArgument() {
        return getArgs()[0];
    }

    public String getPlayer() {
        return this.player;
    }

    public boolean getWaitForPlayerIfOffline() {
        return this.waitForPlayer;
    }

    public SynergyEvent setArguments(String[] args) {
        this.args = args;
        return this;
    }

    public SynergyEvent setArgument(String args) {
        this.args = new String[] {
            args
        };
        return this;
    }

    public SynergyEvent setPlayer(String player) {
        this.player = player;
        return this;
    }

    public SynergyEvent setWaitForPlayerIfOffline(boolean waitPlayer) {
        this.waitForPlayer = waitPlayer;
        return this;
    }

    public void send() {
        if (Synergy.getConfig().getBoolean("synergy-plugin-messaging.enabled")) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(identifier);
            out.writeUTF(player);
            out.writeUTF(waitForPlayer+"");
            for (String arg: args) {
                out.writeUTF(arg);
            }
            Bukkit.getServer().sendPluginMessage(Synergy.getSpigotInstance(), "net:synergy", out.toByteArray());
        } else if (getWaitForPlayerIfOffline() && Bukkit.getPlayer(this.player) == null) {
            ConfigurationSection objs = Synergy.getSpigotInstance().getConfig().getConfigurationSection("tempdata." + this.player + "." + this.identifier);
            int obj = (objs != null) ? (objs.getKeys(false).size() + 1) : 1;
            Synergy.getConfig().set("tempdata." + this.player + "." + this.identifier + "." + obj, this.args);
        } else {
            trigger(this.identifier, this.player, this.args);
        }
    }

    private void trigger(final String identifier, final String player, final String[] args) {
        Bukkit.getScheduler().runTask((Plugin) Synergy.getSpigotInstance(), new Runnable() {
            public void run() {
                Bukkit.getServer().getPluginManager().callEvent(new SynergyEvent(identifier, player, args));
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String player = event.getPlayer().getName();
        if (Synergy.getConfig().get("tempdata." + player) != null) {
            ConfigurationSection identifiers = Synergy.getSpigotInstance().getConfig().getConfigurationSection("tempdata." + player);
            if (identifiers != null)
                for (String identifier: identifiers.getKeys(false)) {
                    ConfigurationSection objs = Synergy.getSpigotInstance().getConfig().getConfigurationSection("tempdata." + player + "." + identifier);
                    if (objs != null)
                        for (String obj: objs.getKeys(false)) {
                            List < String > args = new ArrayList < > ();
                            if (Synergy.getConfig().isSet("tempdata." + player + "." + identifier + "." + obj))
                                args = Synergy.getSpigotInstance().getConfig().getStringList("tempdata." + player + "." + identifier + "." + obj);
                            trigger(identifier, player, args. < String > toArray(new String[0]));
                        }
                }
        }
        Synergy.getConfig().set("tempdata." + player, null);
    }
}