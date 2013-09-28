package net.robbytu.banjoserver.framework.utils;

import net.robbytu.banjoserver.framework.Main;
import net.robbytu.banjoserver.framework.auth.AuthProvider;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PluginMessengerListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, Player s, byte[] message) {
        if(!channel.equals("BSBungee")) return;

        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        try {
            String cmd = in.readUTF();
            if(cmd.equals("teleport")) {
                Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Player sender = Main.plugin.getServer().getPlayer(in.readUTF());
                            Player target = Main.plugin.getServer().getPlayer(in.readUTF());

                            if(sender == null || target == null) {
                                Main.plugin.getLogger().warning("Could not teleport! Got request from PluginMessenger.");
                                return;
                            }

                            sender.teleport(target);
                        }
                        catch(Exception ignored) {}
                    }
                }, 30L);
            }
            else if(cmd.equals("userCommand")) {
                Player target = Main.plugin.getServer().getPlayer(in.readUTF());
                if(target != null) target.performCommand(in.readUTF());
            }
            else if(cmd.equals("PlayerAuthInfo")) {
                // Authenticated users don't *have* to be on this particular server, so we don't want to get the target user as a Player object.
                String player = in.readUTF();
                String action = in.readUTF();

                Main.plugin.getLogger().info("Received player authentication information update for PLAYER=" + player + ", ACTION=" + action);

                if(action.equals("authenticated")) AuthProvider.addAuthenticatedUser(player);
                else if(action.equals("unauthorized")) AuthProvider.removeAuthenticatedUser(player);
            }
            else {
                Main.plugin.getLogger().warning("Received incompatible bs-bungee plugin command: " + cmd);
            }
        } catch (IOException ignored) {}
    }
}
