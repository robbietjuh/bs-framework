package net.robbytu.banjoserver.framework;

import net.robbytu.banjoserver.framework.auth.AuthListener;
import net.robbytu.banjoserver.framework.auth.AuthProvider;
import net.robbytu.banjoserver.framework.listeners.PlayerJoinListener;
import net.robbytu.banjoserver.framework.utils.PluginMessengerListener;
import net.robbytu.banjoserver.framework.utils.ServerUpdater;

import net.robbytu.banjoserver.framework.utils.TaskWorker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;

public class Main extends JavaPlugin {
	public static Connection conn;
	public static Main plugin;
	
	private ServerUpdater serverUpdater;
	
	@Override
	public void onEnable() {
		plugin = this;

		if(!getConfig().contains("db.host") || !getConfig().contains("db.port") || !getConfig().contains("db.username") || !getConfig().contains("db.password") || !getConfig().contains("db.database")) {
			getConfig().set("db.host", "127.0.0.1");
			getConfig().set("db.port", 3306);
			getConfig().set("db.username", "minecraft");
			getConfig().set("db.password", "");
			getConfig().set("db.database", "minecraft");

			this.saveConfig();

			getLogger().warning("Framework has not been enabled. Please check your configuration - a default one has been written.");
			return;
		}

		String host = getConfig().getString("db.host");
		int port = getConfig().getInt("db.port");
		String user = getConfig().getString("db.username");
		String pass = getConfig().getString("db.password");
		String database = getConfig().getString("db.database");

		try{
			conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true", user, pass);
            getLogger().info("Connected to the database.");
		}
		catch (Exception e) {
			getLogger().warning("Framework has not been enabled. Please check your configuration.");
			e.printStackTrace();
		}

        getLogger().info("Updating server info...");
		this.serverUpdater = new ServerUpdater();
		this.serverUpdater.setOnline(1);

        getLogger().info("Registring for events...");
        getServer().getPluginManager().registerEvents(this.serverUpdater, this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        if(getServer().getServerName().equalsIgnoreCase("hub")) {
            getLogger().info("Detected a hub server - enabling authentication gateway...");

            getServer().getPluginManager().registerEvents(new AuthListener(), this);
            AuthProvider.enabled = true;
        }

        getLogger().info("Setting up task worker...");
        new TaskWorker();

        getLogger().info("Setting up bs-bungee communication...");
        PluginMessageListenerRegistration result = Bukkit.getMessenger().registerIncomingPluginChannel(this, "BSBungee", new PluginMessengerListener());
        if(result.isValid()) getLogger().info("Registered for plugin messages.");

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BSFramework");

		getLogger().info("Framework has been enabled.");
	}
	
	@Override
	public void onDisable() {
		// Update our status to offline
		this.serverUpdater.setOnline(0);
		
		getLogger().info("Framework has been disabled.");

        this.saveConfig();
	}

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Console may not interact with bs-framework!");
            return true;
        }

        if(label.equalsIgnoreCase("bs-framework") && args.length > 0) {
            if(!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "Rainbows " + ChatColor.YELLOW + "have " + ChatColor.AQUA + "a " + ChatColor.LIGHT_PURPLE + "lot " + ChatColor.GREEN + "of " + ChatColor.GOLD + "colours.");
                return true;
            }

            if(args[0].equalsIgnoreCase("setspawn")) {
                int x = ((Player)sender).getLocation().getBlockX();
                int y = ((Player)sender).getLocation().getBlockY();
                int z = ((Player)sender).getLocation().getBlockZ();

                float pitch = ((Player)sender).getLocation().getPitch();
                float yaw = ((Player)sender).getLocation().getYaw();

                String world = ((Player)sender).getLocation().getWorld().getName();

                if(args.length == 2 && args[1].equalsIgnoreCase("authorized")) {
                    getConfig().set("spawn.authenticated.is_set", true);

                    getConfig().set("spawn.authenticated.x", x);
                    getConfig().set("spawn.authenticated.y", y);
                    getConfig().set("spawn.authenticated.z", z);

                    getConfig().set("spawn.authenticated.pitch", pitch);
                    getConfig().set("spawn.authenticated.yaw", yaw);

                    getConfig().set("spawn.authenticated.world", world);

                    sender.sendMessage(ChatColor.GREEN + "Updated spawn coords for authorized players.");
                    return true;
                }
                else if(args.length == 2 && args[1].equalsIgnoreCase("unauthorized")) {
                    getConfig().set("spawn.unauthenticated.is_set", true);

                    getConfig().set("spawn.unauthenticated.x", x);
                    getConfig().set("spawn.unauthenticated.y", y);
                    getConfig().set("spawn.unauthenticated.z", z);

                    getConfig().set("spawn.unauthenticated.pitch", pitch);
                    getConfig().set("spawn.unauthenticated.yaw", yaw);

                    getConfig().set("spawn.unauthenticated.world", world);

                    sender.sendMessage(ChatColor.GREEN + "Updated spawn coords for unauthorized players.");
                    return true;
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Usage: /bs-framework setspawn [authorized/unauthorized]");
                    return true;
                }
            }
            else {
                sender.sendMessage("");
                sender.sendMessage(ChatColor.YELLOW + "Currently available methods: ");
                sender.sendMessage(ChatColor.GRAY + " * setspawn [authorized/unauthorized]");
                sender.sendMessage("");
            }
        }
        else if(label.equalsIgnoreCase("spawn")) {
            int spawn_to_use = 0;

            if(Main.plugin.getConfig().getBoolean("spawn.unauthenticated.is_set", false)) spawn_to_use = 1;
            if(Main.plugin.getConfig().getBoolean("spawn.authenticated.is_set", false)) spawn_to_use = 2;

            if(spawn_to_use > 0) {
                int x = Main.plugin.getConfig().getInt("spawn." + ((spawn_to_use == 1) ? "un" : "") + "authenticated.x");
                int y = Main.plugin.getConfig().getInt("spawn." + ((spawn_to_use == 1) ? "un" : "") + "authenticated.y");
                int z = Main.plugin.getConfig().getInt("spawn." + ((spawn_to_use == 1) ? "un" : "") + "authenticated.z");

                float pitch = Main.plugin.getConfig().getInt("spawn." + ((spawn_to_use == 1) ? "un" : "") + "authenticated.pitch");
                float yaw = Main.plugin.getConfig().getInt("spawn." + ((spawn_to_use == 1) ? "un" : "") + "authenticated.yaw");

                String world = Main.plugin.getConfig().getString("spawn." + ((spawn_to_use == 1) ? "un" : "") + "authenticated.world");

                final Location location = new Location(Main.plugin.getServer().getWorld(world), x, y, z, yaw, pitch);
                final Location old_location = ((Player) sender).getLocation();

                if(getServer().getServerName().equalsIgnoreCase("hub")) {
                    ((Player) sender).teleport(location);
                    return true;
                }

                getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    @Override
                    public void run() {
                        if(((Player) sender).getLocation().equals(old_location)) ((Player) sender).teleport(location);
                        else sender.sendMessage(ChatColor.RED + "Teleport geannuleerd: je bewoog.");
                    }
                }, 20*3);

                sender.sendMessage(ChatColor.YELLOW + "Blijf stilstaan; je wordt in 3 seconden geteleporteerd.");
            }
            else {
                sender.sendMessage(ChatColor.RED + "No spawn has been set. Please contact an admin.");
            }
        }

        return true;
    }
}
