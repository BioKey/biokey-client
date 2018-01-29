package com.biokey.client.helpers;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.models.ClientStateModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class RequestBuilderHelper {

    @Autowired
    private ClientStateModel state;

    /**
     * Create header map with the current status' access token added.
     *
     * @return header map with the current status' access token as the only element
     */
    public MultiValueMap<String, String> headerMapWithToken() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(AppConstants.SERVER_TOKEN_HEADER, state.getCurrentStatus().getAccessToken());
        return map;
    }

    /**
     * Create request body for POST keystrokes.
     *
     * @return map representing the request body for POST keystrokes
     * @throws JsonProcessingException if serialization of keystrokes to JSON has failed
     */
    public Map<String, String> requestBodyToPostKeystrokes() throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        ObjectWriter writer = (new ObjectMapper()).writerFor(Queue.class)
                .withAttribute("typingProfile", state.getCurrentStatus().getProfile().getId());
        map.put("keystrokes", writer.writeValueAsString(state.getOldestKeyStrokes().getKeyStrokes()));
        return map;
    }
}
