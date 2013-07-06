package net.robbytu.banjoserver.framework.api;

import net.robbytu.banjoserver.framework.Main;
import net.robbytu.banjoserver.framework.interfaces.Server;
import net.robbytu.banjoserver.framework.interfaces.Switch;
import org.bukkit.Material;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SwitchAPI {
    public static Switch[] getSwitches() {
        // Init some vars
        Connection conn = Main.conn;
        ArrayList<Switch> switches = new ArrayList<Switch>();

        try {
            // Create a new select statement
            Statement select = conn.createStatement();
            ResultSet result = select.executeQuery("SELECT type, server, material, description FROM bs_switcher WHERE active = 1");

            // For each switch ...
            while(result.next()) {
                // Create a new switch
                Switch sw = new Switch();

                // Check type
                if(result.getInt(1) == 1) {
                    // Check wether the server is up
                    Server server = ServerAPI.getServer(result.getInt(2));
                    if(server.serverStatus == 1) {
                        sw.type = 1;
                        sw.server = server;
                        sw.serverName = server.serverName;
                        sw.switchDescription = result.getString(4);
                        sw.switchMaterial = Material.getMaterial(result.getInt(3));

                        switches.add(sw);
                    }
                }
                else if(result.getInt(1) == 2) {
                    sw.type = 2;

                    switches.add(sw);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Return the array of switches
        return switches.toArray(new Switch[switches.size()]);
    }
}
