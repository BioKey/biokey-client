package com.biokey.client.controllers;

import com.biokey.client.constants.SyncStatusConstants;
import com.biokey.client.helpers.RequestBuilderHelper;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.KeyStrokesPojo;
import com.biokey.client.models.response.LoginResponse;
import com.biokey.client.models.response.TypingProfileContainerResponse;
import com.biokey.client.models.response.TypingProfileResponse;
import com.biokey.client.providers.AppProvider;
import com.biokey.client.services.ClientInitService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;

import static com.biokey.client.constants.AppConstants.KEYSTROKE_WINDOW_SIZE_PER_REQUEST;
import static com.biokey.client.constants.UrlConstants.*;

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
    public boolean sendKeyStrokes() throws JsonProcessingException {
        // First, make sure to get the lock.
        state.obtainAccessToModel();

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
                    String.class,
                    (ResponseEntity<String> response) -> {
                        // First, make sure to get the lock.
                        state.obtainAccessToModel();

                        try {
                            // Check if the response was good.
                            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                                log.debug("KeyStrokes failed to sync with server and received response: " + response);
                                keysToSend.setSyncedWithServer(SyncStatusConstants.UNSYNCED);
                                return;
                            }

                            // If it was good then update the model.
                            state.dequeueSyncedKeyStrokes();
                            log.debug("KeyStrokes successfully synced with server and received response: " + response);
                        } finally {
                            state.releaseAccessToModel();
                        }
                    });

            return true;
        } catch (JsonProcessingException e) {
            log.error("Exception when trying to serialize keystrokes to JSON", e);
            throw e;
        } finally {
            state.releaseAccessToModel();
        }
    }

    /**
     * Modify the model to include the new keystroke.
     *
     * @param keyStroke
     */
    public void enqueueKeyStroke(@NonNull KeyStrokePojo keyStroke) {
        // First, make sure to get the lock.
        state.obtainAccessToKeyStrokes();

        try {
            // Enqueue key stroke and if the oldest window of keystrokes is too long then create a new window.
            state.enqueueKeyStroke(keyStroke);
            if (state.getOldestKeyStrokes().getKeyStrokes().size() >= KEYSTROKE_WINDOW_SIZE_PER_REQUEST) {
                state.divideKeyStrokes();
            }
        } finally {
            state.releaseAccessToKeyStrokes();
        }
    }

    /**
     * Send a login request to the server.
     *
     * @param email the email of the user to be logged in
     * @param password the password of the user to be logged in
     * @throws JsonProcessingException if the cast to json fails
     */
    public void sendLoginRequest(@NonNull String email, @NonNull String password,ServerRequestExecutorHelper.ServerResponseHandler<LoginResponse> handler) throws JsonProcessingException {
        //        // First, make sure to get the lock.
        state.obtainAccessToStatus();

        try {
            // Make the request
            serverRequestExecutorHelper.submitPostRequest(
                    SERVER_NAME + LOGIN_POST_API_ENDPOINT,
                    requestBuilderHelper.headerMapNoToken(),
                    requestBuilderHelper.requestBodyToPostLogin(email, password),
                    LoginResponse.class,handler);

        } catch (JsonProcessingException e) {
            log.error("Exception when trying to serialize keystrokes to JSON", e);
            throw e;
        } finally {
            state.releaseAccessToStatus();
        }
    }


    /**
     * Send a get request to retreive a user's typing profile
     *
     * @param mac the mac address of the computer
     * @throws JsonProcessingException if the cast to json fails
     */
    public void retrieveTypingProfileGivenAuthandMAC(@NonNull String mac,String token,ServerRequestExecutorHelper.ServerResponseHandler<TypingProfileContainerResponse> handler) throws JsonProcessingException {
        //        // First, make sure to get the lock.
        state.obtainAccessToStatus();

        try {
            // Make the request
            serverRequestExecutorHelper.submitGetRequest(
                    SERVER_NAME + GET_TYPING_PROFILE_ENDPOINT + mac,
                    requestBuilderHelper.headerMapWithCustomToken(token), //take a specific token not from model
                    TypingProfileContainerResponse.class,
                    handler);

        }  finally {
            state.releaseAccessToStatus();
        }
    }

    /**
     * Sends the server a request with the access token to confirm the client is still authenticated.
     */
    public void confirmAccessToken(ServerRequestExecutorHelper.ServerResponseHandler<String> handler) {
        // First, make sure to get the lock.
        state.obtainAccessToStatus();
        try {
            // Make the request
            serverRequestExecutorHelper.submitGetRequest(
                    SERVER_NAME + AUTH_GET_API_ENDPOINT,
                    requestBuilderHelper.headerMapWithToken(),
                    String.class, handler
                    );
        } finally {
            state.releaseAccessToStatus();
        }
    }

    /**
     * Passes the client state read from memory to the model.
     *
     * @param fromMemory client status loaded from memory, to be passed to the model
     */
    public void passStateToModel(@NonNull ClientStateModel fromMemory) {
        state.loadStateFromMemory(fromMemory);
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
