package com.biokey.client.constants;

import java.io.Serializable;

/**
 * General constants for the client.
 */
public class AppConstants implements Serializable {

    private static final long serialVersionUID = 700;

    public static final String SERVER_TOKEN_HEADER = "authorization";
    public static final int KEYSTROKE_WINDOW_SIZE_PER_REQUEST = 1000;
    public static final String LOCAL_STATE_PATH = "/Users/brandonkucera/tmp/ClientState.ser";

}
