package com.biokey.client.helpers;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.KeyStrokesPojo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
     * Create request body for POST keystrokes.
     *
     * @return map representing the request body for POST keystrokes
     * @throws JsonProcessingException if serialization of keystrokes to JSON has failed
     */
    public String requestBodyToPostKeystrokes(KeyStrokesPojo keysToSend) throws JsonProcessingException {
        ObjectWriter writer = (new ObjectMapper()).writerFor(Queue.class)
                .withAttribute("typingProfile", state.getCurrentStatus().getProfile().getId());
        return "{\"keystrokes\": " + writer.writeValueAsString(keysToSend.getKeyStrokes()) + "}";
    }
}
