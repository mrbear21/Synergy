package me.synergy.integrations;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

public class PlotSquaredAPI {

    public static Plot getCurrentPlot(Player player) {
        PlotPlayer<Player> plotPlayer = PlotPlayer.from(player);
        return plotPlayer.getCurrentPlot();
    }

    public static boolean isPlotChatEnabled(Player player) {
        @SuppressWarnings("unused")
		PlotPlayer<Player> plotPlayer = PlotPlayer.from(player);
        return false;
    }

    public static List<Player> getPlayersOnPlot(Plot plot) {
    	List<Player> players = new ArrayList<>();
    	for (Player p : Bukkit.getOnlinePlayers()) {
    		if (getCurrentPlot(p) == plot) {
    			players.add(p);
    		}
    	}
    	return players;
    }

}
