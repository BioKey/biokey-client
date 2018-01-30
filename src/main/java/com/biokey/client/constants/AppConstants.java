package com.biokey.client.constants;

import java.nio.file.Paths;

/**
 * General constants for the client.
 */
public class AppConstants {

    public static final String SERVER_TOKEN_HEADER = "authorization";
    public static final int KEYSTROKE_WINDOW_SIZE_PER_REQUEST = 1000;
    public static final String LOCAL_STATE_PATH = Paths.get(System.getProperty("user.dir"), "target", "biokey-state.ser").toString();

}
