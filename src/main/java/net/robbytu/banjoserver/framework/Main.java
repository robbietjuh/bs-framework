package net.robbytu.banjoserver.framework;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main extends JavaPlugin {
	public Connection conn = null;
	
	@Override
	public void onEnable() {
		// Fetch db configuration
		String host = getConfig().getString("db.host");
		int port = getConfig().getInt("db.port");
		String user = getConfig().getString("db.username");
		String pass = getConfig().getString("db.password");
		String database = getConfig().getString("db.database");
		
		// Check db configuration
		if(host.isEmpty() || user.isEmpty() || pass.isEmpty() || database.isEmpty()) {
			getLogger().warning("Framework has not been enabled. Please check your configuration.");
			return;
		}
		
		// Set up a connection
		try{
			conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, pass);
		}
		catch (Exception e) {
			getLogger().warning("Framework has not been enabled. Please check your configuration.");
			getLogger().warning(e.toString());
		}
		
		// Everything went OK
		getLogger().info("Framework has been enabled.");
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Framework has been disabled.");
	}
}
