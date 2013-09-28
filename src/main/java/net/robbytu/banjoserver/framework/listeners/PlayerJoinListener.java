package net.robbytu.banjoserver.framework.listeners;

import net.robbytu.banjoserver.framework.Main;
import net.robbytu.banjoserver.framework.auth.AuthProvider;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        event.setJoinMessage("");

        if(AuthProvider.enabled) return;

        try {
            Connection conn = Main.conn;

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM bs_firstjoin WHERE username LIKE ?");
            statement.setString(1, event.getPlayer().getName());
            ResultSet result = statement.executeQuery();

            boolean firstJoin = true;
            while(result.next()) firstJoin = false;

            if(firstJoin) {
                statement = conn.prepareStatement("SELECT material, book_title, book_content, amount FROM bs_defaultinvs WHERE server LIKE ?");
                statement.setString(1, Main.plugin.getServer().getServerName());
                result = statement.executeQuery();

                while(result.next()) {
                    Material material = Material.getMaterial(result.getInt(1));
                    ItemStack stack = new ItemStack(material, result.getInt(4));

                    if(material == Material.WRITTEN_BOOK) {
                        BookMeta meta = (BookMeta) stack.getItemMeta();

                        meta.setAuthor("Banjoserver");
                        meta.setTitle(result.getString(2));
                        meta.setPages(result.getString(3));

                        stack.setItemMeta(meta);
                    }

                    event.getPlayer().getInventory().addItem(stack);
                }

                event.getPlayer().sendMessage(ChatColor.YELLOW + "Welkom in de " + Main.plugin.getServer().getServerName() + " server van de Banjoserver, " + event.getPlayer().getName() + "!");
            }
        }
        catch(Exception ignored) {}
    }

}
