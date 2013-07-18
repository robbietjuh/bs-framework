package net.robbytu.banjoserver.framework.auth;

import java.util.ArrayList;
import java.util.List;

public class AuthProvider {
    private static List<String> authenticatedUsers = new ArrayList<String>();

    public static boolean isAuthenticated(String player) {
        return (authenticatedUsers.contains(player.toUpperCase()));
    }

    public static void addAuthenticatedUser(String player) {
        authenticatedUsers.add(player);
    }

    public static void removeAuthenticatedUser(String player) {
        authenticatedUsers.remove(player);
    }
}
