package com.biokey.client.constants;

/**
 * General constants for the client.
 */
public class AppConstants {
    public static final String APP_NAME = "BioKey";
    public static final String SERVER_TOKEN_HEADER = "authorization";
    public static final int KEYSTROKE_WINDOW_SIZE_PER_REQUEST = 100;
    public static final int KEYSTROKE_TIME_INTERVAL_PER_WINDOW = 300000; // 5 minutes of inactivity
    public static final String CLIENT_STATE_PREFERENCES_ID = "client_state";
    public static final int KEYSTROKE_WINDOW_SIZE_PER_SAVE = 1000;
    public static final int ANALYSIS_RESULT_WINDOW_SIZE_PER_SAVE = 1000;
    public static final int TIME_BETWEEN_HEARTBEATS = 1000;
    public static final int TIME_BETWEEN_SERVER_SYNCS = 1000;
    public static final int MAX_CHALLENGE_ATTEMPTS = 3;
    public static final int SQS_LISTENER_PERIOD = 3000;
    public static final float DEFAULT_THRESHOLD = 0.1f;
    public static final boolean SEND_KEYSTROKES_TO_SERVER = true;
    public static final boolean SEND_ANALYSIS_TO_SERVER = true;
}
