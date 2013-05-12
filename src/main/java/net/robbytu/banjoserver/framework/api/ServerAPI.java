package net.robbytu.banjoserver.framework.api;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.robbytu.banjoserver.framework.Main;
import net.robbytu.banjoserver.framework.interfaces.Server;

public class ServerAPI {
	
	/**
	 * Fetch all servers, including their status and online players
	 * @return An array of servers
	 */
	public static Server[] getServers() {
		// Init some vars
		Connection conn = Main.conn;
		Server[] servers = {};
		
		try {
			// Create a new select statement
			Statement select = conn.createStatement();
			ResultSet result = select.executeQuery("SELECT servername, online, players FROM bs_servers");
			
			// For each server ...
			while(result.next()) {
				// Create a new Server instance
				Server server = new Server();
				
				// Fill in properties
				server.serverName = result.getString(0);
				server.serverStatus = result.getInt(1);
				server.serverPlayers = result.getInt(2);
				
				// Add server to return array
				servers[servers.length] = server;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Return the array of servers
		return servers;
	}
	
}
