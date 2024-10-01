package me.synergy.utils;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.synergy.brains.Synergy;
import me.synergy.objects.BreadMaker;
import net.md_5.bungee.chat.ComponentSerializer;

public class BookMessage {
	
    public static void sendFakeBook(Player player, String title, String content) {
    	
        BreadMaker bread = Synergy.getBread(player.getUniqueId());
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle(title);

        content = Translation.processLangTags(content, bread.getLanguage());

        String[] pages = content.split("%np%");

        for (String page : pages) {
        	page = Synergy.translate(page, bread.getLanguage()).setExecuteInteractive(bread).getColored(bread.getTheme());
            meta.spigot().addPage(ComponentSerializer.parse(page));
        }

        meta.setAuthor("synergy");
        book.setItemMeta(meta);
        player.openBook(book);
        player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
        
    }
    
}
