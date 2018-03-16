package com.biokey.client.controllers;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.EngineConstants;
import com.biokey.client.constants.SyncStatusConstants;
import com.biokey.client.helpers.PojoHelper;
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
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriTemplate;

import java.util.Deque;
import java.util.Timer;
import java.util.TimerTask;

import static com.biokey.client.constants.AppConstants.*;
import static com.biokey.client.constants.UrlConstants.*;

/**
 * Handles requests by services to make changes to the client data.
 */
public class ClientStateController {

    private static Logger log = Logger.getLogger(ClientStateController.class);

    private final ClientStateModel state;
    private final ServerRequestExecutorHelper serverRequestExecutorHelper;
    private final Timer timer = new Timer(true);

    @Autowired
    public ClientStateController(ClientStateModel state,
                                 ServerRequestExecutorHelper serverRequestExecutorHelper) {
        this.state = state;
        this.serverRequestExecutorHelper = serverRequestExecutorHelper;

        // Schedule syncs with server.
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendClientStatus();

                try {
                    sendKeyStrokes();
                } catch (UnsupportedOperationException e) {}

                try {
                    sendAnalysisResult();
                } catch (UnsupportedOperationException e) {}
            }
        }, TIME_BETWEEN_SERVER_SYNCS, TIME_BETWEEN_SERVER_SYNCS);
    }

    /**
     * Sends the server a message at fixed time intervals to let it know when the client is alive.
     */
    public void sendHeartbeat(@NonNull String id, @NonNull ServerRequestExecutorHelper.ServerResponseHandler<String> handler) {
        state.obtainAccessToStatus();
        try {
            // Check if there is a current status.
            if (state.getCurrentStatus() == null) {
                log.error("Confirm access token called but no model was found.");
                handler.handleResponse(null);
                return;
            }

            serverRequestExecutorHelper.submitPostRequest(
                    new UriTemplate(SERVER_NAME + HEARTBEAT_POST_API_ENDPOINT).expand(id).toString(),
                    RequestBuilderHelper.headerMapWithToken(state.getCurrentStatus().getAccessToken()),
                    "",
                    String.class,
                    handler);
        } finally {
            state.releaseAccessToStatus();
        }
    }

    /**
     * Sends the server a request with the oldest client keystrokes not yet known to server.
     * Modifies the sync status for all keystrokes sent.
     *
     * @return true if the oldest keys were unsynced and an attempt to sync them was made
     */
    public boolean sendKeyStrokes() {
        // Check if we want to send keystrokes to server.
        if (!SEND_KEYSTROKES_TO_SERVER) throw new UnsupportedOperationException();

        // First, make sure to get the lock.
        state.obtainAccessToModel();
        try {
            // Change the state of keystrokes to SYNCING.
            KeyStrokesPojo keysToSend = state.getOldestKeyStrokes();
            ClientStatusPojo currentStatus = state.getCurrentStatus();
            if (currentStatus == null || keysToSend == null ||
                    !(keysToSend.getSyncedWithServer() == SyncStatusConstants.UNSYNCED)) return false;
            keysToSend.setSyncedWithServer(SyncStatusConstants.SYNCING);

            // Make the request.
            serverRequestExecutorHelper.submitPostRequest(
                    SERVER_NAME + KEYSTROKE_POST_API_ENDPOINT,
                    RequestBuilderHelper.headerMapWithToken(currentStatus.getAccessToken()),
                    RequestBuilderHelper.requestBodyToPostKeystrokes(keysToSend, currentStatus.getProfile().getId()),
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
            return false;
        } finally {
            state.releaseAccessToModel();
        }
    }

    /**
     * Sends the server a request with the oldest status not yet known to server.
     * Modifies the sync status for status sent.
     *
     * @return true if the status was unsynced and an attempt to sync them was made
     */
    public boolean sendClientStatus() {
        // First, make sure to get the lock.
        state.obtainAccessToStatus();
        try {
            // Change the state of status to SYNCING.
            ClientStatusPojo oldStatus = state.getOldestStatus();
            ClientStatusPojo currentStatus = state.getCurrentStatus();
            if (currentStatus == null || oldStatus == null ||
                    !(oldStatus.getSyncedWithServer() == SyncStatusConstants.UNSYNCED)) return false;
            oldStatus.setSyncedWithServer(SyncStatusConstants.SYNCING);

            // Make the request.
            serverRequestExecutorHelper.submitPutRequest(
                    new UriTemplate(SERVER_NAME + CLIENT_STATUS_PUT_API_ENDPOINT).expand(oldStatus.getProfile().getId()).toString(),
                    RequestBuilderHelper.headerMapWithToken(currentStatus.getAccessToken()),
                    RequestBuilderHelper.requestBodyToPostClientStatus(oldStatus),
                    String.class,
                    (ResponseEntity<String> response) -> {
                        // First, make sure to get the lock.
                        state.obtainAccessToModel();

                        try {
                            // Check if the response was good.
                            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                                log.debug("Client status failed to sync with server and received response: " + response);
                                oldStatus.setSyncedWithServer(SyncStatusConstants.UNSYNCED);
                                return;
                            }

                            // If it was good then update the model.
                            state.dequeueStatus();
                            log.debug("Client Status successfully synced with server and received response: " + response);
                        } finally {
                            state.releaseAccessToModel();
                        }
                    });

            return true;
        } catch (JsonProcessingException e) {
            log.error("Exception when trying to serialize client status to JSON", e);
            return false;
        } finally {
            state.releaseAccessToStatus();
        }
    }

    /**
     * Sends the server a request with the oldest analysis result not yet known to server.
     * Modifies the sync status for result sent.
     *
     * @return true if the result was unsynced and an attempt to sync them was made
     */
    public boolean sendAnalysisResult() {
        // Impossible to test because server has not implemented the API endpoint.
        // Check if we want to send results to server.
        if (!SEND_ANALYSIS_TO_SERVER) throw new UnsupportedOperationException();

        // First, make sure to get the lock.
        state.obtainAccessToModel();
        try {
            // Change the state of status to SYNCING.
            AnalysisResultPojo analysisResult = state.getOldestAnalysisResult();
            ClientStatusPojo currentStatus = state.getCurrentStatus();
            if (currentStatus == null || analysisResult == null ||
                    !(analysisResult.getSyncedWithServer() == SyncStatusConstants.UNSYNCED)) return false;
            analysisResult.setSyncedWithServer(SyncStatusConstants.SYNCING);

            // Make the request.
            serverRequestExecutorHelper.submitPostRequest(
                    SERVER_NAME + ANALYSIS_RESULT_POST_API_ENDPOINT,
                    RequestBuilderHelper.headerMapWithToken(currentStatus.getAccessToken()),
                    RequestBuilderHelper.requestBodyToPostAnalysisResult(analysisResult, currentStatus.getProfile().getId()),
                    String.class,
                    (ResponseEntity<String> response) -> {
                        // First, make sure to get the lock.
                        state.obtainAccessToModel();

                        try {
                            // Check if the response was good.
                            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                                log.debug("Analysis result failed to sync with server and received response: " + response);
                                analysisResult.setSyncedWithServer(SyncStatusConstants.UNSYNCED);
                                return;
                            }

                            // If it was good then update the model.
                            state.dequeueAnalysisResult();
                            log.debug("Analysis result successfully synced with server and received response: " + response);
                        } finally {
                            state.releaseAccessToModel();
                        }
                    });

            return true;
        } catch (JsonProcessingException e) {
            log.error("Exception when trying to serialize client status to JSON", e);
            return false;
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
     */
    public void sendLoginRequest(@NonNull String email, @NonNull String password,
                                 @NonNull ServerRequestExecutorHelper.ServerResponseHandler<LoginResponse> handler) {
        // First, make sure to get the lock.
        state.obtainAccessToStatus();
        try {
            // Make the request
            serverRequestExecutorHelper.submitPostRequest(
                    SERVER_NAME + LOGIN_POST_API_ENDPOINT,
                    RequestBuilderHelper.emptyHeaderMap(),
                    RequestBuilderHelper.requestBodyToPostLogin(email, password),
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
                    new UriTemplate(SERVER_NAME + TYPING_PROFILE_POST_API_ENDPOINT).expand(mac).toString(),
                    RequestBuilderHelper.headerMapWithToken(accessToken),
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
                    RequestBuilderHelper.headerMapWithToken(state.getCurrentStatus().getAccessToken()),
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
     * Modify the model to include the new analysis result.
     *
     * @param analysisResult the analysis result to enqueue
     */
    public void enqueueAnalysisResult(@NonNull AnalysisResultPojo analysisResult) {
        // First, make sure to get the lock.
        state.obtainAccessToAnalysisResult();
        try {
            state.enqueueAnalysisResult(analysisResult);
            state.notifyAnalysisResultQueueChange(analysisResult);
        } finally {
            state.releaseAccessToAnalysisResult();
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
                ClientStatusPojo unAuthenticatedStatus = PojoHelper.createStatus(currentStatus, AuthConstants.UNAUTHENTICATED);
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
     * Modify the model with the new google Auth key.
     *
     * @param googleAuthKey the new google Auth key
     */
    public void setGoogleAuthKey(@NonNull String googleAuthKey) {
        state.obtainAccessToStatus();
        try {
            // Check if there is a current status.
            ClientStatusPojo currentStatus = state.getCurrentStatus();
            if (currentStatus == null) {
                log.error("Set google auth key called but no model was found.");
                return;
            }

            enqueueStatus(new ClientStatusPojo(currentStatus.getProfile(), currentStatus.getAuthStatus(),
                    currentStatus.getSecurityStatus(), currentStatus.getAccessToken(),
                    currentStatus.getPhoneNumber(), googleAuthKey, System.currentTimeMillis()));
        } finally {
            state.releaseAccessToStatus();
        }
    }

}
