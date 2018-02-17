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
import com.biokey.client.models.response.LoginResponse;
import com.biokey.client.models.response.TypingProfileContainerResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriTemplate;

import java.util.Deque;

import static com.biokey.client.constants.AppConstants.KEYSTROKE_TIME_INTERVAL_PER_WINDOW;

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

    private ClientStateModel state;
    private RequestBuilderHelper requestBuilderHelper;
    private ServerRequestExecutorHelper serverRequestExecutorHelper;

    @Autowired
    public ClientStateController(ClientStateModel state, RequestBuilderHelper requestBuilderHelper,
                                 ServerRequestExecutorHelper serverRequestExecutorHelper) {
        this.state = state;
        this.requestBuilderHelper = requestBuilderHelper;
        this.serverRequestExecutorHelper = serverRequestExecutorHelper;
    }

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
            if (state.getCurrentStatus() == null || keysToSend == null || !(keysToSend.getSyncedWithServer() == SyncStatusConstants.UNSYNCED)) return false;
            keysToSend.setSyncedWithServer(SyncStatusConstants.SYNCING);

            // Make the request.
            serverRequestExecutorHelper.submitPostRequest(
                    SERVER_NAME + KEYSTROKE_POST_API_ENDPOINT,
                    requestBuilderHelper.headerMapWithToken(state.getCurrentStatus().getAccessToken()),
                    requestBuilderHelper.requestBodyToPostKeystrokes(keysToSend, state.getCurrentStatus().getProfile().getId()),
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
     * Send a login request to the server.
     *
     * @param email the email of the user to be logged in
     * @param password the password of the user to be logged in
     * @param handler the code to call when the server returns a response
     * @throws JsonProcessingException if the cast to json fails
     */
    public void sendLoginRequest(@NonNull String email, @NonNull String password,
                                 @NonNull ServerRequestExecutorHelper.ServerResponseHandler<LoginResponse> handler) {
        // First, make sure to get the lock.
        state.obtainAccessToStatus();
        try {
            // Make the request
            serverRequestExecutorHelper.submitPostRequest(
                    SERVER_NAME + LOGIN_POST_API_ENDPOINT,
                    requestBuilderHelper.emptyHeaderMap(),
                    requestBuilderHelper.requestBodyToPostLogin(email, password),
                    LoginResponse.class,
                    handler);
        } finally {
            state.releaseAccessToStatus();
        }
    }

    /**
     * Send a GET request to retrieve a user's typing profile.
     *
     * @param mac the mac address of the computer
     * @param accessToken a new access token after login that has not been reflected in the state
     * @param handler the code to call when the server returns a response
     */
    public void retrieveStatusFromServer(@NonNull String mac, @NonNull String accessToken,
                                         @NonNull ServerRequestExecutorHelper.ServerResponseHandler<TypingProfileContainerResponse> handler) {
        // First, make sure to get the lock.
        state.obtainAccessToStatus();
        try {
            // Make the request.
            serverRequestExecutorHelper.submitPostRequest(
                    new UriTemplate(SERVER_NAME + POST_TYPING_PROFILE_ENDPOINT).expand(mac).toString(),
                    requestBuilderHelper.headerMapWithToken(accessToken),
                    "{}", // TODO: make helper function for better form
                    TypingProfileContainerResponse.class,
                    handler);

        }  finally {
            state.releaseAccessToStatus();
        }
    }

    /**
     * Sends the server a request with the access token to confirm the client is still authenticated.
     * @param handler the code to call when the server returns a response
     */
    public void confirmAccessToken(@NonNull ServerRequestExecutorHelper.ServerResponseHandler<String> handler) {
        // First, make sure to get the lock.
        state.obtainAccessToStatus();
        try {
            // Check if there is a current status.
            if (state.getCurrentStatus() == null) {
                log.error("Confirm access token called but no model was found.");
                handler.handleResponse(null);
                return;
            }

            // Make the request
            serverRequestExecutorHelper.submitGetRequest(
                    SERVER_NAME + USERS_GET_API_ENDPOINT,
                    requestBuilderHelper.headerMapWithToken(state.getCurrentStatus().getAccessToken()),
                    String.class,
                    handler);
        } finally {
            state.releaseAccessToStatus();
        }
    }

    /**
     * Modify the model to include the new key stroke.
     *
     * @param keyStroke the key stroke to enqueue
     */
    public void enqueueKeyStroke(@NonNull KeyStrokePojo keyStroke) {
        // First, make sure to get the lock.
        state.obtainAccessToKeyStrokes();
        try {
            // If the oldest window of keystrokes is too long (by keys or by time) then create a new window.
            if (state.getNewestKeyStrokes() != null) {
                Deque<KeyStrokePojo> newestKeyStrokes = state.getNewestKeyStrokes().getKeyStrokes();
                if (newestKeyStrokes.size() >= KEYSTROKE_WINDOW_SIZE_PER_REQUEST ||
                        keyStroke.getTimeStamp() - newestKeyStrokes.peekLast().getTimeStamp() > KEYSTROKE_TIME_INTERVAL_PER_WINDOW) {
                    state.divideKeyStrokes();
                }
            }

            state.enqueueKeyStroke(keyStroke);
            state.notifyKeyQueueChange(keyStroke);
        } finally {
            state.releaseAccessToKeyStrokes();
        }
    }

    /**
     * Modify the model to include the new status.
     *
     * @param status the status to enqueue
     */
    public void enqueueStatus(@NonNull ClientStatusPojo status) {
        // First, make sure to get the lock.
        state.obtainAccessToStatus();
        try {
            ClientStatusPojo oldStatus = state.getCurrentStatus();
            state.enqueueStatus(status);
            state.notifyStatusChange(oldStatus, status);
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
        state.obtainAccessToModel();
        fromMemory.obtainAccessToModel();
        try {
            state.loadStateFromMemory(fromMemory);

            // Make sure that the loaded state is unauthenticated.
            ClientStatusPojo currentStatus = fromMemory.getCurrentStatus();
            if (currentStatus != null && currentStatus.getAuthStatus() == AuthConstants.AUTHENTICATED) {
                ClientStatusPojo unAuthenticatedStatus = createStatusWithAuth(currentStatus, AuthConstants.UNAUTHENTICATED);
                state.enqueueStatus(unAuthenticatedStatus);
            }

            state.notifyModelChange();
        } finally {
            state.releaseAccessToModel();
            fromMemory.releaseAccessToModel();
        }
    }

    /**
     * Checks if the client state model read from memory is valid.
     *
     * @return true if the client state model read from memory is valid
     */
    public boolean checkStateModel(@NonNull ClientStateModel fromMemory) {
        return state.checkStateModel(fromMemory);
    }

    /**
     * Tell the model to clear itself.
     */
    public void clearModel() {
        state.obtainAccessToModel();
        try {
            state.clear();
        } finally {
            state.releaseAccessToModel();
        }
    }

    /**
     * Returns a new status with the authStatus set to the new authStatus.
     *
     * @param currentStatus the current status
     * @param newAuth the new authStatus
     */
    public ClientStatusPojo createStatusWithAuth(@NonNull ClientStatusPojo currentStatus, @NonNull AuthConstants newAuth) {
        return new ClientStatusPojo(currentStatus.getProfile(), newAuth, currentStatus.getSecurityStatus(),
                currentStatus.getAccessToken(), currentStatus.getPhoneNumber(), System.currentTimeMillis());
    }

    /**
     * Implementation of listener to the ClientStateModel's status.
     * The controller will send server requests periodically.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {
        //TODO: Implement statusChanged()
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
