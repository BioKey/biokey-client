package com.biokey.client.constants;

/**
 * Constants for the URLs of server endpoints.
 */
public class UrlConstants {

    public static final String SERVER_NAME = "http://18.219.182.1:3000";
    public static final String KEYSTROKE_POST_API_ENDPOINT = "/api/keystrokes";
    public static final String USERS_GET_API_ENDPOINT = "/api/users/me";
    public static final String LOGIN_POST_API_ENDPOINT = "/api/auth/login";
    public static final String TYPING_PROFILE_POST_API_ENDPOINT = "/api/typingProfiles/machine/{id}";
    public static final String HEARTBEAT_POST_API_ENDPOINT = "/api/typingProfiles/{id}/heartbeat";
    public static final String CLIENT_STATUS_PUT_API_ENDPOINT = "/api/typingProfiles/{id}";
    public static final String ANALYSIS_RESULT_POST_API_ENDPOINT = "/api/analysisResults";
}
