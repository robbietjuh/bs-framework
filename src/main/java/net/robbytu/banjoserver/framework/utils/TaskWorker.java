package net.robbytu.banjoserver.framework.utils;

import net.robbytu.banjoserver.framework.Main;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TaskWorker {
    /**
     * Initializes a new TaskWorker
     */
    public TaskWorker() {
        // Register a scheduler to check for new tasks
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
            public void run() {
                checkTasks();
            }
        }, 20, 200);

        Bukkit.getLogger().info("Started TaskWorker scheduled task checker");
    }

    /**
     * Checks for new tasks
     */
    private void checkTasks() {
        try {
            // Fetch database connection from the Main class
            Connection conn = Main.conn;

            // Create a new select statement
            Statement select = conn.createStatement();
            ResultSet result = select.executeQuery("SELECT tasktype, task, as_user FROM bs_tasks WHERE server = '" + Bukkit.getServer().getServerName() + "' AND completed = 0");

            // For each task ...
            while(result.next()) {
                // Check what kind of task this is
                switch(result.getInt(1)) {
                    case 1:
                        Bukkit.getLogger().info("Executing task type 1: " + result.getString(2));
                        // Todo: implement task executer
                        break;
                    default:
                        Bukkit.getLogger().warning("Got a task but didn't know what to do with it. Type seems to be " + result.getInt(1));
                        break;
                }
            }
        }
        catch (Exception ex) {
            Bukkit.getLogger().warning("Could not check for tasks due to an unexpected error.");
            ex.printStackTrace();
        }
    }
}
