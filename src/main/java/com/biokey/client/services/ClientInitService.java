package com.biokey.client.services;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.helpers.PojoHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.response.LoginResponse;
import com.biokey.client.models.response.TypingProfileContainerResponse;
import com.biokey.client.views.frames.LockFrameView;
import com.biokey.client.views.frames.TrayFrameView;
import com.biokey.client.views.panels.LoginPanelView;
import lombok.NonNull;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.awt.event.ActionEvent;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Service that retrieves the client state from the disk and OS and ensures that it has not been corrupted.
 * If the local data does not exist, then the service prompts the user to login and download the client state.
 */
public class ClientInitService implements
        ClientStateModel.IClientStatusListener,
        ClientStateModel.IClientKeyListener,
        ClientStateModel.IClientAnalysisListener {

    private static Logger log = Logger.getLogger(ClientInitService.class);

    private final ClientStateController controller;
    private final ClientStateModel state;
    private final LockFrameView lockFrameView;
    private final LoginPanelView loginPanelView;
    private final TrayFrameView trayFrameView;

    private static Preferences prefs = Preferences.userRoot().node(ClientInitService.class.getName());
    private int newKeyCount = 0;

    private Timer heartbeatTimer = new Timer();
    private Timer loginTimer = new Timer();

    @Autowired
    public ClientInitService(ClientStateController controller, ClientStateModel state,
                             LockFrameView lockFrameView, LoginPanelView loginPanelView, TrayFrameView trayFrameView)  {

        this.controller = controller;
        this.state = state;
        this.lockFrameView = lockFrameView;
        this.loginPanelView = loginPanelView;
        this.trayFrameView = trayFrameView;

        // Add action to login loginPanelView on submit.
        loginPanelView.addSubmitAction((ActionEvent e) -> {
            // Disable submit.
            loginPanelView.setEnableSubmit(false);

            // Send login request to the server.
            controller.sendLoginRequest(loginPanelView.getEmail(), loginPanelView.getPassword(),
                    (ResponseEntity<LoginResponse> response) -> {
                        // Check if the response was good.
                        if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody().getToken() == null) {
                            log.debug("Login failed and received response " + response);
                            // On failed login, show message to user that login failed.
                            loginPanelView.setEnableSubmit(true);
                            loginPanelView.setInformationText("Login failed. Please try again.");
                            return;
                        }

                        // If successful, call next function to retrieve status.
                        log.debug("Login Succeeded and received response: " + response);
                        String mac = PojoHelper.getMAC();
                        if (mac == null) {
                            log.debug("Could not retrieve MAC address.");
                            loginPanelView.setEnableSubmit(true);
                            loginPanelView.setInformationText("Login failed. Could not retrieve MAC address. Please try again.");
                        } else retrieveStatusFromServer(mac, response.getBody().getToken());
                    });
        });
    }

    /**
     * Implementation of listener to the ClientStateModel's status. The service will save the client state periodically.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {
        log.debug("Status Changed!");
        // First, run a save.
        saveClientState();

        AuthConstants oldAuthStatus = (oldStatus == null) ? null : oldStatus.getAuthStatus();
        AuthConstants newAuthStatus = (newStatus == null) ? null : newStatus.getAuthStatus();
        SecurityConstants oldLockedStatus = (oldStatus == null) ? null : oldStatus.getSecurityStatus();
        SecurityConstants newLockedStatus = (newStatus == null) ? null : newStatus.getSecurityStatus();

        // Second, logic for the heartbeat and tray icon.
        if (oldAuthStatus != newAuthStatus){
            if (newAuthStatus == AuthConstants.AUTHENTICATED) {
                startHeartbeat();
                trayFrameView.setTrayIcon(true);
            }
            else {
                stopHeartbeat();
                trayFrameView.setTrayIcon(false);
            }
        }

        // Third, logic to login.
        // Check 1: if new status is null, something bad happened, reset using loginWithoutModel.
        if (newStatus == null) {
            loginWithoutModel();
        }
        // Check 2: if client changed to unauthenticated, and the client is unlocked, then try loginWithModel.
        else if (oldAuthStatus != newAuthStatus && newAuthStatus == AuthConstants.UNAUTHENTICATED &&
                newLockedStatus == SecurityConstants.UNLOCKED) {
            loginWithModel();
        }
        // Check 3: if client remains unauthenticated, and the client changes to unlocked, then try loginWithModel.
        else if (oldAuthStatus == newAuthStatus && newAuthStatus == AuthConstants.UNAUTHENTICATED &&
                oldLockedStatus != newLockedStatus && newLockedStatus == SecurityConstants.UNLOCKED) {
            loginWithModel();
        }
    }

    /**
     * Implementation of listener to the ClientStateModel's keystroke queues.
     * The service will save the client state periodically.
     */
    public void keystrokeQueueChanged(KeyStrokePojo newKey) {
        if (++newKeyCount >= AppConstants.KEYSTROKE_WINDOW_SIZE_PER_SAVE) {
            newKeyCount = 0;
            saveClientState();
        }
    }

    /**
     * Implementation of listener to the ClientStateModel's analysis results queue.
     * The service will save the client state periodically.
     */
    public void analysisResultQueueChanged(AnalysisResultPojo newResult) {
        saveClientState();
    }

    /**
     * Entry point into this service. Tries to retrieve the client state and in the process
     * calls the {@link #checkCorrupt()} and at least one of the login functions.
     */
    public void retrieveClientState() {
        ClientStateModel fromMemory;

        // Ensure no saves are currently happening.
        state.obtainAccessToModel();
        try {
            fromMemory = retrieveFromPreferences();
            if (fromMemory == null) {
                loginWithoutModel();
                return;
            }
        } catch (Exception e) {
            log.debug("Could not retrieve initial client state from preferences", e);
            loginWithoutModel();
            return;
        } finally {
            state.releaseAccessToModel();
        }

        // TODO: checkCorrupt()
        if (controller.checkStateModel(fromMemory)) {
            log.debug("Retrieved client state from preferences");
            controller.passStateToModel(fromMemory);
            // Don't try to login here because the retrieved model might lock the computer.
            // We should only try to login after the computer is unlocked.
        }
        else loginWithoutModel();
    }

    /**
     * Retrieve model from preferences.
     *
     * @return model retrieved from preferences
     */
    public static ClientStateModel retrieveFromPreferences() {
        // Get data about the size to retrieve.
        int blocks = prefs.getInt(AppConstants.CLIENT_STATE_PREFERENCES_ID + ".blocks", 0);
        int totalSize = (blocks - 1) * Preferences.MAX_VALUE_LENGTH * 3 / 4 +
                prefs.getByteArray(AppConstants.CLIENT_STATE_PREFERENCES_ID + "." + (blocks - 1), new byte[0]).length;

        // Retrieve from Preferences.
        byte[] stateByteArray = new byte[totalSize];
        for (int i = 0; i < blocks; i++) {
            byte[] block = prefs.getByteArray(AppConstants.CLIENT_STATE_PREFERENCES_ID + "." + i, new byte[0]);
            System.arraycopy(block, 0, stateByteArray, i * Preferences.MAX_VALUE_LENGTH * 3 / 4, block.length);
        }

        // Cast to ClientStateModel.
        return (ClientStateModel) SerializationUtils.deserialize(stateByteArray);
    }

    /**
     * Called when the status of the client changes. Saves the entire state to Preferences.
     */
    public void saveClientState() {
        Runnable r = () -> {
            state.obtainAccessToModel();
            try {
                saveToPreferences(state);
                // TODO: lockLocalSave()
                log.debug("Saved client state to file.");
            } catch (Exception e) {
                log.debug("Could not save client state to file.", e);
            } finally {
                state.releaseAccessToModel();
            }
        };
        r.run();
    }

    /**
     * Save model to preferences.
     *
     * @param state the model to save to preferences
     */
    public static void saveToPreferences(ClientStateModel state) {
        // Clear save to Preferences.
        try {
            prefs.clear();
        } catch (BackingStoreException e) {
            log.error("Caught BackingStoreException when trying to clear saved Preferences", e);
        }

        byte[] stateBytes = SerializationUtils.serialize(state);
        int blocks = 0;
        while (blocks * Preferences.MAX_VALUE_LENGTH * 3 / 4 < stateBytes.length) {
            prefs.putByteArray(AppConstants.CLIENT_STATE_PREFERENCES_ID + "." + blocks,
                    Arrays.copyOfRange(stateBytes, blocks++ * Preferences.MAX_VALUE_LENGTH * 3 / 4, blocks * Preferences.MAX_VALUE_LENGTH * 3 / 4));
        }
        prefs.putInt(AppConstants.CLIENT_STATE_PREFERENCES_ID + ".blocks", blocks);
    }

    /**
     * Starts the controller's heartbeat function.
     */
    private void startHeartbeat() {
        // Enqueue the next heartbeat. First heartbeat should not occur now.
        TimerTask callNextHeartbeat = new TimerTask() {
            @Override
            public void run() {
                sendHeartBeat();
            }
        };
        heartbeatTimer.scheduleAtFixedRate(callNextHeartbeat, AppConstants.TIME_BETWEEN_HEARTBEATS, AppConstants.TIME_BETWEEN_HEARTBEATS);
    }

    /**
     * Stops the controller's heartbeat function.
     */
    private void stopHeartbeat() {
        heartbeatTimer.cancel();
        heartbeatTimer = new Timer();
    }

    /**
     * Sends a heartbeat using the controller's interface.
     */
    private void sendHeartBeat() {
        state.obtainAccessToStatus();
        try {
            controller.sendHeartbeat(state.getCurrentStatus().getProfile().getId(), (ResponseEntity<String> response) -> {
                // First, make sure to get the lock.
                state.obtainAccessToModel();
                try {
                    // Check if the response was good.
                    if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                        log.debug("Heartbeat failed and received response: " + response);

                        // Client is probably offline. Do nothing.
                        if (response == null) return;

                        // If error is 401, then client is no longer authenticated.
                        if (response.getStatusCodeValue() == 401) {
                            controller.enqueueStatus(controller.createStatusWithAuth(
                                    state.getCurrentStatus(), AuthConstants.UNAUTHENTICATED));
                            return;
                        }

                        // If error is something else unknown, then client is no longer authenticated.
                        controller.enqueueStatus(controller.createStatusWithAuth(
                                state.getCurrentStatus(), AuthConstants.UNAUTHENTICATED));

                    } else log.debug("Heartbeat succeeded and received response: " + response);
                } finally {
                    state.releaseAccessToModel();
                }
            });
        } finally {
            state.releaseAccessToStatus();
        }
    }

    /**
     * Login without any local credentials. Will prompt the user for the credentials.
     */
    private void loginWithoutModel() {
        // First, clear any client data that may have been corrupted.
        clearClientData();

        // Initiate the loginPanelView.
        loginPanelView.setEnableSubmit(true);
        lockFrameView.addPanel(loginPanelView.getLoginPanel());
        lockFrameView.lock();
    }

    /**
     * Login with local credentials (auth token). Will call the no parameter version if the credentials are not validated by server.
     */
    private void loginWithModel() {
        controller.confirmAccessToken((ResponseEntity<String> response) -> {
            // First, make sure to get the lock.
            state.obtainAccessToStatus();
            try {
                // Check if the response was good.
                if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                    log.debug("Access token did not authenticate and received response: " + response);

                    // Client is probably offline. Try logging in again.
                    if (response == null) {
                        TimerTask callNextLogin = new TimerTask() {
                            @Override
                            public void run() {
                                loginWithModel();
                            }
                        };
                        loginTimer.schedule(callNextLogin, AppConstants.TIME_BETWEEN_HEARTBEATS);
                        return;
                    }

                    // If there is another error then just send user to login.
                    loginWithoutModel();
                }

                // Check if there is a current status.
                ClientStatusPojo currentStatus = state.getCurrentStatus();
                if (currentStatus == null) {
                    log.error("Login with model but no model was found.");
                    loginWithoutModel();
                    return;
                }

                // If response was good then enqueue new status.
                log.debug("User successfully authenticated and received response: " + response);
                if (!(currentStatus.getAuthStatus() == AuthConstants.AUTHENTICATED)) {
                    controller.enqueueStatus(controller.createStatusWithAuth(currentStatus, AuthConstants.AUTHENTICATED));
                }
            } finally {
                state.releaseAccessToStatus();
            }
        });
    }

    /**
     * Retrieve status from server given the authToken and computer's MAC address.
     *
     * @param mac the MAC address associated with the computer
     * @param token the token to use to authenticate the user
     */
    private void retrieveStatusFromServer(@NonNull String mac, @NonNull String token) {
        controller.retrieveStatusFromServer(mac, token, (ResponseEntity<TypingProfileContainerResponse> response) -> {
            // First, make sure to get the lock.
            state.obtainAccessToStatus();
            try {
                // Check if the response was good.
                if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                    log.debug("Error occurred when retrieving typing profile " + response);
                    // On failed retrieval, show message to user that it failed.
                    loginPanelView.setEnableSubmit(true);
                    loginPanelView.setInformationText("Login failed. Please try again.");
                }

                log.debug("Successfully retrieved typing profile: " + response);
                // If this succeeded, we can remove the frame.
                lockFrameView.unlock();
                lockFrameView.removePanel(loginPanelView.getLoginPanel());

                // Enqueue the response as the new status.
                ClientStatusPojo newStatus = PojoHelper.castToClientStatus(response.getBody(), token);
                state.getCurrentStatus(); // Superfluous call because we don't care what the old status was.
                controller.enqueueStatus(newStatus);
            } finally {
                state.releaseAccessToStatus();
            }
        });
    }

    /**
     * Clear both the model and Preferences.
     */
    private void clearClientData() {
        // Clear ClientStateModel.
        controller.clearModel();
        // Clear save to Preferences.
        try {
            prefs.clear();
        } catch (BackingStoreException e) {
            log.error("Caught BackingStoreException when trying to clear saved Preferences", e);
        }
    }

    /**
     * Check the local files and OS for signs of corruption.
     *
     * @return true if corruption was detected
     */
    private boolean checkCorrupt() {
        // TODO: Implement checkCorrupt()
        return true;
    }

    /**
     * Lock the local files securely.
     *
     * @return true if the lock was successfully completed
     */
    private boolean lockLocalSave() {
        // TODO: Implement lockLocalSave(), prevent non-admin user from killing this process and prevent local file deletion.
        return false;
    }
}
