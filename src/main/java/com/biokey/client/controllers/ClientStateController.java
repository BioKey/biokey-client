package com.biokey.client.controllers;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SyncStatusConstants;
import com.biokey.client.helpers.RequestBuilderHelper;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.KeyStrokesPojo;
import com.biokey.client.models.response.KeyStrokesPostResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;

import static com.biokey.client.constants.UrlConstants.KEYSTROKE_POST_API_ENDPOINT;
import static com.biokey.client.constants.UrlConstants.SERVER_NAME;

/**
 * Handles requests by services to make changes to the client data.
 */
public class ClientStateController implements
        ClientStateModel.IClientStatusListener,
        ClientStateModel.IClientKeyListener,
        ClientStateModel.IClientAnalysisListener {

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
     * Modifies the sync status for all keystrokes sent.
     *
     * @return true if the oldest keys were unsynced and an attempt to sync them was made
     */
    private boolean sendKeyStrokes() throws JsonProcessingException {
        // First, make sure to get the lock.
        state.obtainAccessToKeyStrokes();

        try {
            // Change the state of keystrokes to SYNCING.
            KeyStrokesPojo keysToSend = state.getOldestKeyStrokes();
            if (!keysToSend.getSyncedWithServer().equals(SyncStatusConstants.UNSYNCED)) return false;
            keysToSend.setSyncedWithServer(SyncStatusConstants.SYNCING);

            // Make the request
            serverRequestExecutorHelper.submitPostRequest(
                    SERVER_NAME + KEYSTROKE_POST_API_ENDPOINT,
                    requestBuilderHelper.headerMapWithToken(),
                    requestBuilderHelper.requestBodyToPostKeystrokes(keysToSend),
                    KeyStrokesPostResponse.class,
                    (ResponseEntity<KeyStrokesPostResponse> response) -> {
                        // First, make sure to get the lock.
                        state.obtainAccessToKeyStrokes();

                        try {
                            // Check if the response was good.
                            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                                keysToSend.setSyncedWithServer(SyncStatusConstants.UNSYNCED);
                                return;
                            }

                            // If it was good then update the model.
                            state.dequeueSyncedKeyStrokes();
                        } finally {
                            state.releaseAccessToKeyStrokes();
                        }
                    });

            return true;
        } catch (JsonProcessingException e) {
            log.error("Exception when trying to serialize keystrokes to JSON", e);
            throw e;
        } finally {
            state.releaseAccessToKeyStrokes();
        }
    }

    /**
     * Allows passes the client state read from memory to the model
     *
     * @param fromMemory client status loaded from memory, to be passed to the model
     */
    public void passStateToModel(@NonNull ClientStateModel fromMemory) {
        state.loadStatusFromMemory(fromMemory);
    }

    /**
     *
     * @param keyStroke
     */
    public void enqueueKeyStroke(@NonNull KeyStrokePojo keyStroke) {

    }

    /**
     * Implementation of listener to the ClientStateModel's status.
     * The controller will send server requests periodically.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {
        //TODO: Implement keystrokeQueueChanged()
        return;
    }

    /**
     * Implementation of listener to the ClientStateModel's keystroke queues.
     * The controller will send server requests periodically.
     */
    public void keystrokeQueueChanged(KeyStrokePojo newKey) {
        //TODO: Implement keystrokeQueueChanged()
        return;
    }

    /**
     * Implementation of listener to the ClientStateModel's analysis results queue.
     * The controller will send server requests periodically.
     */
    public void analysisResultQueueChanged(AnalysisResultPojo newResult) {
        //TODO: Implement analysisResultQueueChanged()
        return;
    }

}
