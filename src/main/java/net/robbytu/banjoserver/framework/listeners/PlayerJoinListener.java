package net.robbytu.banjoserver.framework.listeners;

import net.robbytu.banjoserver.framework.Main;
import net.robbytu.banjoserver.framework.auth.AuthProvider;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PlayerJoinListener implements Listener {

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled=true)
    public void playerJoin(PlayerJoinEvent event) {
        boolean isVanished = false;
        for(final MetadataValue value : event.getPlayer().getMetadata("vanished")) {
            if(value.getOwningPlugin().getName().equals("VanishNoPacket") && value.asBoolean()) {
                isVanished = true;
            }
        }

        event.setJoinMessage((Main.plugin.getServer().getOnlinePlayers().length < 30 && !Main.plugin.getServer().getServerName().equalsIgnoreCase("hub") && !isVanished) ? ChatColor.YELLOW + "Welkom in " + Main.plugin.getServer().getServerName() + ", " + event.getPlayer().getName() : null);

        if(Main.plugin.getServer().getServerName().equalsIgnoreCase("hub")) {
            //event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3));
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        }

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
            }
        }
        catch(Exception ignored) {}
    }

}
