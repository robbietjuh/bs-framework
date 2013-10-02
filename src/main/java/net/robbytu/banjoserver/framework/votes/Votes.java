package net.robbytu.banjoserver.framework.votes;

import net.robbytu.banjoserver.framework.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Votes {
    public static Vote getVoteForUser(String username) {
        Connection conn = Main.conn;

        try {
            PreparedStatement statement = conn.prepareStatement("SELECT username, serviceName, address, timestamp FROM bs_votes WHERE timestamp < ? AND redeemed = ? AND username = ?");

            statement.setInt(1, (int) ((int) System.currentTimeMillis() / 1000L));
            statement.setInt(2, 0);
            statement.setString(3, username);

            ResultSet result = statement.executeQuery();

            if(result.next()) {
                Vote vote = new Vote();

                vote.username = result.getString(1);
                vote.serviceName = result.getString(2);
                vote.address = result.getString(3);
                vote.timestamp = result.getInt(4);

                return vote;
            }
            else {
                return null;
            }
        }
        catch(Exception ignored) {
            return null;
        }
    }
}
