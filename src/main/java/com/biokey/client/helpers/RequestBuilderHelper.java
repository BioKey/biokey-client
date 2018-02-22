package com.biokey.client.helpers;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.models.pojo.KeyStrokesPojo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Queue;

public class RequestBuilderHelper {

    /**
     * Create header map with the current status' access token added.
     *
     * @param token the custom token
     * @return header map with the current status' access token as the only element
     */
    public static HttpHeaders headerMapWithToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AppConstants.SERVER_TOKEN_HEADER, token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Create empty header map specifying content type application/json.
     *
     * @return header map specifying application/json
     */
    public static HttpHeaders emptyHeaderMap(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Create request body for POST keystrokes.
     *
     * @param keysToSend the keys to serialize
     * @param typingProfileId the typing profile id to attach to each serialized key
     * @return string representing the request body for POST keystrokes
     * @throws JsonProcessingException if serialization of keystrokes to JSON has failed
     */
    public static String requestBodyToPostKeystrokes(KeyStrokesPojo keysToSend, String typingProfileId) throws JsonProcessingException {
        ObjectWriter writer = (new ObjectMapper()).writerFor(Queue.class)
                .withAttribute("typingProfile", typingProfileId);
        return "{\"keystrokes\": " + writer.writeValueAsString(keysToSend.getKeyStrokes()) + "}";
    }

    /**
     * Create request body for POST login.
     *
     * @param email    the email of the user to be logged in
     * @param password the password of the user to be logged in
     * @return string representing the request body for POST keystrokes
     */
    public static String requestBodyToPostLogin(@NonNull String email, @NonNull String password) {
        return "{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}";
    }
}

