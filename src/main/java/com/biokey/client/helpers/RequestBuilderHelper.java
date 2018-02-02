package com.biokey.client.helpers;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.KeyStrokesPojo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Queue;

public class RequestBuilderHelper {

    @Autowired
    private ClientStateModel state;

    /**
     * Create header map with the current status' access token added.
     *
     * @return header map with the current status' access token as the only element
     */
    public HttpHeaders headerMapWithToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AppConstants.SERVER_TOKEN_HEADER, state.getCurrentStatus().getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Create header map with the custom token
     *
     * @param token the custom token
     * @return header map with the custom access token as the only element
     */
    public HttpHeaders headerMapWithCustomToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AppConstants.SERVER_TOKEN_HEADER, token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }


    /**
     * Create header map specifying application/json
     *
     * @return header map specifying application/json
     */
    public HttpHeaders headerMapNoToken (){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Create request body for POST keystrokes.
     *
     * @return string representing the request body for POST keystrokes
     * @throws JsonProcessingException if serialization of keystrokes to JSON has failed
     */
    public String requestBodyToPostKeystrokes(KeyStrokesPojo keysToSend) throws JsonProcessingException {
        ObjectWriter writer = (new ObjectMapper()).writerFor(Queue.class)
                .withAttribute("typingProfile", state.getCurrentStatus().getProfile().getId());
        return "{\"keystrokes\": " + writer.writeValueAsString(keysToSend.getKeyStrokes()) + "}";
    }

    /**
     * Create request body for POST login.
     *
     * @param email    the email of the user to be logged in
     * @param password the password of the user to be logged in
     * @return string representing the request body for POST keystrokes
     * @throws JsonProcessingException if serialization of login body to JSON has failed
     */
    public String requestBodyToPostLogin(@NonNull String email, @NonNull String password) throws JsonProcessingException {
        return "{\"email\":" + "\"" + email + "\"" + ",\"password\":" + "\"" + password + "\"}";
    }
}

