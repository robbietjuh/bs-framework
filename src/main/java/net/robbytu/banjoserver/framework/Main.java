package net.robbytu.banjoserver.framework;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	@Override
	public void onEnable() {
		// Fetch db configuration
		String host = getConfig().getString("db.host");
		String user = getConfig().getString("db.username");
		String pass = getConfig().getString("db.password");
		String database = getConfig().getString("db.database");
		
		if(host.isEmpty() || user.isEmpty() || pass.isEmpty() || database.isEmpty()) {
			getLogger().warning("Framework has not been enabled. Please check your configuration.");
			return;
		}
		
		
		
		getLogger().info("Framework has been enabled.");
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Framework has been disabled.");
	}
}
