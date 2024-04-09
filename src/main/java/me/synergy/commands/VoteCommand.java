package me.synergy.commands;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.synergy.brains.Synergy;
import me.synergy.objects.BreadMaker;
import me.synergy.utils.LangTagProcessor;
import me.synergy.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class VoteCommand implements CommandExecutor {

    public VoteCommand() {}

    public void initialize() {
        if (!Synergy.getConfig().getBoolean("votifier.enabled")) {
        	return;
        }
        Synergy.getSpigot().getCommand("vote").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("synergy.vote")) {
        	BreadMaker bread = Synergy.getBread(((Player) sender).getUniqueId());
            List<String> monitorings = Synergy.getConfig().getStringList("votifier.monitorings");
            TextComponent message = new TextComponent(LangTagProcessor.processLangTags("<lang>synergy-vote-monitorings</lang>", bread.getLanguage()));
            for (String m : monitorings) {
                try {
                	String domain = new URI(m).getHost();
                	TextComponent t = new TextComponent(LangTagProcessor.processLangTags("<lang>synergy-vote-monitorings-format<arg>"+domain.replace("www.", "")+"</arg></lang>", bread.getLanguage()));
                	t.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, m));
                    t.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(LangTagProcessor.processLangTags("<lang>synergy-click-to-open<arg>"+domain+"</arg></lang>", bread.getLanguage()))));
                    message.addExtra("\n");
                    message.addExtra(t);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            //((Player) sender).spigot().sendMessage(message);
            Utils.sendFakeBook((Player) sender, "Monitorings", new TextComponent[] {message});
        } else {
            sender.sendMessage("<lang>synergy-no-permission</lang>");
        }
        return true;
    }
}