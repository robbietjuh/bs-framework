package net.robbytu.banjoserver.framework.interfaces;

import org.bukkit.Material;

/**
 * Switch class
 *
 * @author Robert
 *
 */
public class Switch {

    /**
     * Switch type
     */
    public int type;

    /**
     * Server entry
     */
    public Server server;

    /**
     * Server name
     */
    public String serverName;

    /**
     * Material to indicate for this server
     */
    public Material switchMaterial;

    /**
     * Description
     */
    public String switchDescription;
}
