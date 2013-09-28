package net.robbytu.banjoserver.framework.listeners;

import net.robbytu.banjoserver.framework.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled=true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage((Main.plugin.getServer().getOnlinePlayers().length < 30 && !Main.plugin.getServer().getServerName().equalsIgnoreCase("hub")) ? event.getPlayer().getName() + " verliet " + Main.plugin.getServer().getServerName() : null);
    }
}
