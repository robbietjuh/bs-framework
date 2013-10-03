package net.robbytu.banjoserver.framework;

import net.robbytu.banjoserver.framework.auth.AuthListener;
import net.robbytu.banjoserver.framework.auth.AuthProvider;
import net.robbytu.banjoserver.framework.listeners.PlayerJoinListener;
import net.robbytu.banjoserver.framework.listeners.PlayerQuitListener;
import net.robbytu.banjoserver.framework.utils.EssentialsTimeConverter;
import net.robbytu.banjoserver.framework.utils.PluginMessengerListener;
import net.robbytu.banjoserver.framework.utils.ServerUpdater;

import net.robbytu.banjoserver.framework.utils.TaskWorker;
import net.robbytu.banjoserver.framework.votes.Vote;
import net.robbytu.banjoserver.framework.votes.Votes;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;

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
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);

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
        else if(label.equalsIgnoreCase("ci") || label.equalsIgnoreCase("clearinventory")) {
            ((Player)sender).getInventory().clear();
            sender.sendMessage(ChatColor.GRAY + "Inventory geleegd.");
            getLogger().info(sender.getName() + " cleared its inventory.");
        }
        else if(label.equalsIgnoreCase("time")) {
            if(!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "Je hebt geen toegang tot dit commando.");
                return true;
            }

            if(args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Er moet 1 argument opgegeven worden: tijd");
                return true;
            }

            long ticks = EssentialsTimeConverter.parse(args[0]);
            ((Player) sender).getWorld().setTime(ticks);
        }
        else if(label.equalsIgnoreCase("me")) {
            if(args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Geef een handeling op.");
                return true;
            }

            String message = "";
            for (int i = 0; i < args.length; i++) message += ((i == 0) ? "" : " ") + args[i];

            for(Player player : getServer().getOnlinePlayers()) player.sendMessage(ChatColor.YELLOW + " * " + sender.getName() + " " + message);
        }
        else if(label.equalsIgnoreCase("gm")) {
            if(!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "Je hebt geen toegang tot dit commando.");
                return true;
            }

            if(args.length == 0) {
                ((Player) sender).setGameMode((((Player) sender).getGameMode() == GameMode.SURVIVAL) ? GameMode.CREATIVE : GameMode.SURVIVAL);
            }
            else {
                sender.sendMessage(ChatColor.RED + "Het wijzigen van de gamemode van een speler is enkel toegestaan vanuit de console om cheaten tegen te gaan. Deze actie is vastgelegd in de logbestanden. Neem contact op met een Owner.");
                getLogger().warning(sender.getName() + " tried to change " + args[1] + "'s gamemode!");
            }
        }
        else if(label.equalsIgnoreCase("vote")) {
            Vote vote = Votes.getVoteForUser(sender.getName());
            if(vote != null) {
                int random = (int)(Math.random() * getConfig().getList("vote.rewards").size()) -1;
                int itemId = getConfig().getInt("vote.rewards.item" + random + ".item");
                int itemAmount = getConfig().getInt("vote.rewards.item" + random + ".amount");

                ItemStack item = new ItemStack(Material.getMaterial(itemId), itemAmount);
                sender.sendMessage(ChatColor.GREEN + "Bedankt voor het stemmen! Je hebt " + Material.getMaterial(itemId).name() + " verdiend!");

                Votes.redeemVote(vote);
            }
            else {
                sender.sendMessage(ChatColor.GRAY + "Je hebt nog niet gestemt of je hebt de afgelopen 24 uur al een stem ingewisseld. Ga naar onze website om te stemmen! Bedankt.");
            }
        }

        return true;
    }
}
