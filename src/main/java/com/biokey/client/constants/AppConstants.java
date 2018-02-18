package com.biokey.client.constants;

import java.nio.file.Paths;

/**
 * General constants for the client.
 */
public class AppConstants {

    public static final String SERVER_TOKEN_HEADER = "authorization";
    public static final int KEYSTROKE_WINDOW_SIZE_PER_REQUEST = 1000;
    public static final int KEYSTROKE_TIME_INTERVAL_PER_WINDOW = 300000; // 5 minutes of inactivity
    public static final String CLIENT_STATE_PREFERENCES_ID = "client_state";
    public static final int KEYSTROKE_WINDOW_SIZE_PER_SAVE = 1000;
    public static final int TIME_BETWEEN_HEARTBEATS = 5000;
}
