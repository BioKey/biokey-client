package com.biokey.client.controllers;

import com.biokey.client.constants.SyncStatusConstants;
import com.biokey.client.helpers.RequestBuilderHelper;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.KeyStrokesPojo;
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
     * @param username the username of the user to be logged in
     * @param password the password of the user to be logged in
     * @throws JsonProcessingException if the cast to json fails
     */
    public void sendLoginRequest(@NonNull String username, @NonNull String password) throws JsonProcessingException {
        //        // First, make sure to get the lock.
        System.out.println("here");
        state.obtainAccessToStatus();

        try {
            // Make the request
            serverRequestExecutorHelper.submitPostRequest(
                    SERVER_NAME + LOGIN_POST_API_ENDPOINT,
                    new HttpHeaders(),
                    requestBuilderHelper.requestBodyToPostLogin(username, password),
                 //   LoginResponse.class, //TODO: define class LoginResponse in ./models/response
                    String.class,
                    (ResponseEntity<String> response) -> { //change back to LoginResponse
                        // First, make sure to get the lock.
                        state.obtainAccessToModel();
                        System.out.println(response);
                        try {
                            // Check if the response was good.
                            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                                log.debug("KeyStrokes failed to sync with server and received response: " + response); //TODO: update log statements
                                response.getBody(); //TODO: write logic here
                                return;
                            }

                            // If it was good then update the model.
                            response.getBody(); //TODO: write logic here
                            log.debug("KeyStrokes successfully synced with server and received response: " + response);
                        } finally {
                            state.releaseAccessToModel();
                        }
                    });

        } catch (JsonProcessingException e) {
            log.error("Exception when trying to serialize keystrokes to JSON", e);
            throw e;
        } finally {
            state.releaseAccessToStatus();
        }
    }

    /**
     * Sends the server a request with the access token to confirm the client is still authenticated.
     */
    public void confirmAccessToken() {
        // First, make sure to get the lock.
        state.obtainAccessToStatus();

        try {
            // Make the request
            serverRequestExecutorHelper.submitGetRequest(
                    SERVER_NAME + AUTH_GET_API_ENDPOINT,
                    requestBuilderHelper.headerMapWithToken(),
                    String.class,
                    (ResponseEntity<String> response) -> {
                        state.obtainAccessToStatus();

                        try {
                            // Check if the response was good.
                            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                                log.debug("Access token did not authenticate and received response: " + response);
                                state.enqueueStatus(null); // TODO: Placeholder, Josh should write the logic
                                return;
                            }

                            // If it was good then do nothing.
                            log.debug("User successfully authenticated and received response: " + response);
                        } finally {
                            state.releaseAccessToStatus();
                        }
                    });
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

    public static void main( String[] args )
    {
        // Load Spring context.
        ApplicationContext springContext = new AnnotationConfigApplicationContext(AppProvider.class);

        ClientStateModel model = springContext.getBean(ClientStateModel.class);

        // Add service 'status' listeners to model
        @SuppressWarnings("unchecked")
        HashSet<ClientStateModel.IClientStatusListener> serviceStatusListeners =
                (HashSet<ClientStateModel.IClientStatusListener>) springContext.getBean("statusListeners");
        model.setStatusListeners(serviceStatusListeners);

        // Add service 'key queue' listeners to model
        @SuppressWarnings("unchecked")
        HashSet<ClientStateModel.IClientKeyListener> keyQueueListeners =
                (HashSet<ClientStateModel.IClientKeyListener>) springContext.getBean("keyQueueListeners");
        model.setKeyQueueListeners(keyQueueListeners);

        // Add service 'analysis results queue' listeners to model
        @SuppressWarnings("unchecked")
        HashSet<ClientStateModel.IClientAnalysisListener> analysisQueueListeners =
                (HashSet<ClientStateModel.IClientAnalysisListener>) springContext.getBean("analysisQueueListeners");
        model.setAnalysisResultQueueListeners(analysisQueueListeners);

        // Retrieve client state and load into program to get all services running.
       ClientInitService clientInitService = springContext.getBean(ClientInitService.class);
       clientInitService.retrieveClientState();

        ClientStateController c = new ClientStateController();
        try {
            c.sendLoginRequest("test1@example.com", "password");
        }
        catch (Exception e)
        {
            System.out.println(e.getStackTrace());
        }

    }
}
