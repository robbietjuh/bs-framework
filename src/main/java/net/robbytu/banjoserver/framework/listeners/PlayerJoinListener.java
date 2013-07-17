package net.robbytu.banjoserver.framework.listeners;

import net.robbytu.banjoserver.framework.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        try {
            Connection conn = Main.conn;

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM bs_firstjoin WHERE username = ?");
            statement.setString(1, event.getPlayer().getName());
            ResultSet result = statement.executeQuery();

            boolean firstJoin = true;
            while(result.next()) firstJoin = false;

            if(firstJoin) {
                statement = conn.prepareStatement("SELECT material, book_title, book_content, amount FROM bs_defaultinvs WHERE server = ?");
                statement.setString(1, Main.plugin.getServer().getServerName());
                result = statement.executeQuery();

                while(result.next()) {
                    Material material = Material.getMaterial(result.getInt(1));
                    ItemStack stack = new ItemStack(material, result.getInt(4));
                    event.getPlayer().getInventory().addItem(stack);
                }

                event.getPlayer().sendMessage(ChatColor.YELLOW + "Welkom in de " + Main.plugin.getServer().getServerName() + " server van de Banjoserver, " + event.getPlayer().getName() + "!");
            }
        }
        catch(Exception ignored) {}
    }

}
