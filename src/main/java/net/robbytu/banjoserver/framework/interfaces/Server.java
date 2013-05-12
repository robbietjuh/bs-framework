package net.robbytu.banjoserver.framework.interfaces;

/**
 * Server class
 * 
 * @author Robert
 *
 */
public class Server {
	
	/**
	 * Name of the server
	 */
	public String serverName;
	
	/**
	 * Status of this server:
	 *   0 = offline
	 *   1 = online
	 */
	public int serverStatus;
	
	/**
	 * Total amount of players online
	 */
	public int serverPlayers;
	
}
