package net.robbytu.banjoserver.framework.utils;

import net.robbytu.banjoserver.framework.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

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
            ResultSet result = select.executeQuery("SELECT id, tasktype, task, as_user FROM bs_tasks WHERE server = '" + Bukkit.getServer().getServerName() + "' AND completed = 0");

            ArrayList<String> completedTasks = new ArrayList<String>();

            // For each task ...
            while(result.next()) {
                switch(result.getInt(2)) {
                    case 1:
                        // Execute a command
                        Bukkit.getLogger().info("Executing task type 1: " + result.getString(3));
                        Player player = Bukkit.getPlayer(result.getString(4));
                        player.performCommand(result.getString(3));
                        break;
                    default:
                        // Unknown command type. Log it.
                        Bukkit.getLogger().warning("Got a task but didn't know what to do with it. Type seems to be " + result.getInt(2));
                        break;
                }

                // Add ID to the complete-array
                completedTasks.add("" + result.getInt(1));
            }

            // Update all completed items
            String updateStatement = "UPDATE bs_tasks SET completed = 1 WHERE ";
            for(String taskId : completedTasks.toArray(new String[completedTasks.size()]))
                updateStatement += "id = " + taskId + " OR ";

            // Execute update query
            conn.createStatement().executeUpdate(updateStatement.substring(-4));
        }
        catch (Exception ex) {
            Bukkit.getLogger().warning("Could not check for tasks due to an unexpected error.");
            ex.printStackTrace();
        }
    }
}
