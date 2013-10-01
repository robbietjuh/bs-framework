package net.robbytu.banjoserver.framework.auth;

import net.robbytu.banjoserver.framework.Main;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

public class AuthListener implements Listener {
    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if(AuthProvider.enabled && !event.isCancelled() && !AuthProvider.isAuthenticated(event.getPlayer().getName())) event.setTo(event.getFrom());
    }

    @EventHandler
    public static void playerInteractEvent(PlayerInteractEvent event) {
        if(AuthProvider.enabled && ( (!event.isCancelled() && !AuthProvider.isAuthenticated(event.getPlayer().getName())) || (event.getAction() == Action.LEFT_CLICK_BLOCK && !event.getPlayer().isOp() && Main.plugin.getServer().getName().equalsIgnoreCase("hub")) )) event.setCancelled(true);
    }

    @EventHandler
    public static void playerDropItemEvent(PlayerDropItemEvent event) {
        if(AuthProvider.enabled && !event.isCancelled() && !AuthProvider.isAuthenticated(event.getPlayer().getName())) event.setCancelled(true);
    }

    @EventHandler
    public static void playerPickupItemEvent(PlayerPickupItemEvent event) {
        if(AuthProvider.enabled && !event.isCancelled() && !AuthProvider.isAuthenticated(event.getPlayer().getName())) event.setCancelled(true);
    }

    @EventHandler
    public static void playerRespawnEvent(PlayerRespawnEvent event) {
        if(!AuthProvider.enabled) return;

        int spawn_to_use = 0;

        if(Main.plugin.getConfig().getBoolean("spawn.unauthenticated.is_set", false) == true && !AuthProvider.isAuthenticated(event.getPlayer().getName())) {
            spawn_to_use = 1; //unauthed
        }
        else if(Main.plugin.getConfig().getBoolean("spawn.authenticated.is_set", false) == true && AuthProvider.isAuthenticated(event.getPlayer().getName())) {
            spawn_to_use = 2; //authed
        }
        else if(Main.plugin.getConfig().getBoolean("spawn.unauthenticated.is_set", false) == true && AuthProvider.isAuthenticated(event.getPlayer().getName())) {
            spawn_to_use = 1; //unauthed
        }

        if(spawn_to_use == 0) return;

        int x = Main.plugin.getConfig().getInt("spawn." + ((spawn_to_use == 1) ? "un" : "") + "authenticated.x");
        int y = Main.plugin.getConfig().getInt("spawn." + ((spawn_to_use == 1) ? "un" : "") + "authenticated.y");
        int z = Main.plugin.getConfig().getInt("spawn." + ((spawn_to_use == 1) ? "un" : "") + "authenticated.z");

        float pitch = Main.plugin.getConfig().getInt("spawn." + ((spawn_to_use == 1) ? "un" : "") + "authenticated.pitch");
        float yaw = Main.plugin.getConfig().getInt("spawn." + ((spawn_to_use == 1) ? "un" : "") + "authenticated.yaw");

        String world = Main.plugin.getConfig().getString("spawn." + ((spawn_to_use == 1) ? "un" : "") + "authenticated.world");

        Location location = new Location(Main.plugin.getServer().getWorld(world), x, y, z, yaw, pitch);

        event.setRespawnLocation(location);
    }

    @EventHandler
    public void playerDamageEvent(EntityDamageEvent event) {
        if(!AuthProvider.enabled || event.getEntityType() != EntityType.PLAYER) return;

        ((Player)event.getEntity()).setFoodLevel(20);
        event.setCancelled(true);
    }
}
