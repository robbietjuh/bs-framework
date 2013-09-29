package net.robbytu.banjoserver.framework.listeners;

import net.robbytu.banjoserver.framework.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;

public class PlayerQuitListener implements Listener {
    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled=true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        boolean isVanished = false;
        for(final MetadataValue value : event.getPlayer().getMetadata("vanished")) {
            if(value.getOwningPlugin().getName().equals("VanishNoPacket") && value.asBoolean()) {
                isVanished = true;
            }
        }

        event.setQuitMessage((Main.plugin.getServer().getOnlinePlayers().length < 30 && !Main.plugin.getServer().getServerName().equalsIgnoreCase("hub") && !isVanished) ? event.getPlayer().getName() + " verliet " + Main.plugin.getServer().getServerName() : null);
    }
}
