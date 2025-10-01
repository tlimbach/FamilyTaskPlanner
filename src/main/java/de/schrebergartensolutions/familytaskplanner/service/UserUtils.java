package de.schrebergartensolutions.familytaskplanner.service;

import de.schrebergartensolutions.familytaskplanner.SessionUtils;

public class UserUtils {
    public static boolean isLoggedIn() {
        String u = SessionUtils.get(SessionUtils.Type.user, String.class);
        return u != null && !u.isBlank();
    }

}
