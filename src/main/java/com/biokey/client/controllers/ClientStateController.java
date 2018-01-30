package com.biokey.client.controllers;

import com.biokey.client.helpers.RequestBuilderHelper;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.response.KeyStrokesPostResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static com.biokey.client.constants.UrlConstants.KEYSTROKE_POST_API_ENDPOINT;
import static com.biokey.client.constants.UrlConstants.SERVER_NAME;

/**
 * Handles requests by services to make changes to the client data.
 */
public class ClientStateController {

    private static Logger log = Logger.getLogger(ClientStateController.class);

    @Autowired
    ClientStateModel state;
    @Autowired
    RequestBuilderHelper requestBuilderHelper;
    @Autowired
    ServerRequestExecutorHelper serverRequestExecutorHelper;

    /**
     * Sends the server a message at fixed time intervals to let it know when the client is alive.
     */
    public void sendHeartbeat() {
        return; //TODO: Implement heartbeat().
    }

    /**
     * Sends the server a request with all of the client keystrokes not yet known to server.
     * Modifies client data after request is successfully sent.
     */
    private void sendKeyStrokes() throws JsonProcessingException {
        state.obtainAccessToKeyStrokes();
        try {
            serverRequestExecutorHelper.submitPostRequest(
                    SERVER_NAME + KEYSTROKE_POST_API_ENDPOINT,
                    requestBuilderHelper.headerMapWithToken(),
                    requestBuilderHelper.requestBodyToPostKeystrokes(),
                    KeyStrokesPostResponse.class,
                    (ResponseEntity<KeyStrokesPostResponse> response) -> {
                        return;
                    });
        } catch (JsonProcessingException e) {
            log.error("Exception when trying to serialize keystrokes to JSON", e);
            throw e;
        } finally {
            state.releaseAccessToKeyStrokes();
        }
    }

    /**
     *
     * @param keyStroke
     */
    public void enqueueKeyStroke(@NonNull KeyStrokePojo keyStroke) {

    }

}
