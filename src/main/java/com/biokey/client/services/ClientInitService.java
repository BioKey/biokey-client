package com.biokey.client.services;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.controllers.challenges.IChallengeStrategy;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import com.biokey.client.models.response.LoginResponse;
import com.biokey.client.models.response.TypingProfileContainerResponse;
import com.biokey.client.models.response.TypingProfileResponse;
import com.biokey.client.views.frames.LockFrameView;
import com.biokey.client.views.panels.LoginPanelView;
import lombok.NonNull;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.NetworkInterface;
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

    private ClientStateController controller;
    private ClientStateModel state;
    private Map<String, IChallengeStrategy> strategies;
    private LockFrameView lockFrameView;
    private LoginPanelView view;

    private Preferences prefs = Preferences.userRoot().node(ClientInitService.class.getName());
    private int newKeyCount = 0;

    private Timer timer = new Timer();

    @Autowired
    public ClientInitService(ClientStateController controller, ClientStateModel state,
                             Map<String, IChallengeStrategy> strategies,
                             LockFrameView lockFrameView, LoginPanelView view)  {

        this.controller = controller;
        this.state = state;
        this.strategies = strategies;
        this.lockFrameView = lockFrameView;
        this.view = view;



        // Add action to login view on submit.
        view.addSubmitAction((ActionEvent e) -> {
            // Disable submit.
            view.setEnableSubmit(false);

            // Send login request to the server.
            controller.sendLoginRequest(view.getEmail(), view.getPassword(),
                    (ResponseEntity<LoginResponse> response) -> {
                        // Check if the response was good.
                        if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody().getToken() == null) {
                            log.debug("Login failed and received response " + response);
                            // On failed login, show message to user that login failed.
                            view.setEnableSubmit(true);
                            view.setInformationText("Login failed. Please try again.");
                            return;
                        }

                        // If successful, call next function to retrieve status.
                        log.debug("Login Succeeded and received response: " + response);
                        String mac = getMAC();
                        if (mac == null) {
                            log.debug("Could not retrieve MAC address.");
                            view.setEnableSubmit(true);
                            view.setInformationText("Login failed. Could not retrieve MAC address. Please try again.");
                        } else retrieveStatusFromServer(getMAC(), response.getBody().getToken());
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

        // Second, logic for the heartbeat.
        if (oldAuthStatus != newAuthStatus){
            if (newAuthStatus == AuthConstants.AUTHENTICATED) startHeartbeat();
            else stopHeartbeat();
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
            byte[] stateBytes = prefs.getByteArray(AppConstants.CLIENT_STATE_PREFERENCES_ID, new byte[0]);
            fromMemory = (ClientStateModel) SerializationUtils.deserialize(stateBytes);
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
     * Called when the status of the client changes. Saves the entire state to Preferences.
     */
    public void saveClientState() {
        Runnable r = () -> {
            state.obtainAccessToModel();
            try {
                byte[] stateBytes = SerializationUtils.serialize(state);
                prefs.putByteArray(AppConstants.CLIENT_STATE_PREFERENCES_ID, stateBytes);
                // TODO: lockLocalSave()
                log.debug("Saved client state to file");
            }
            catch (Exception e) {
                log.debug("Could not save client state to file", e);
            } finally {
                state.releaseAccessToModel();
            }
        };
        r.run();
    }

    private void sendHeartbeat ()
    {
        String mac = getMAC();
        controller.sendHeartbeat(mac,
                (ResponseEntity<String> response) -> {
                    // First, make sure to get the lock.
                    state.obtainAccessToModel();
                    try {

                        TimerTask callNextHeartbeat = new TimerTask() {
                            @Override
                            public void run() {
                                sendHeartbeat();
                            }
                        };

                        // Check if the response was good.
                        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                            log.debug("Heartbeat failed and received response: " + response);

                            //if error is 401, then we are no longer authenticated
                            if (response.getStatusCodeValue()==401) {
                                ClientStatusPojo currentStatus = state.getCurrentStatus();
                                ClientStatusPojo unAuthenticatedStatus = controller.createStatusWithAuth(currentStatus, AuthConstants.UNAUTHENTICATED);
                                controller.enqueueStatus(unAuthenticatedStatus);
                            }

                            if (response.getStatusCodeValue() ==400) //try to send another one
                                timer.schedule (callNextHeartbeat, AppConstants.TIME_BETWEEN_HEARTBEATS);

                            return;
                        }

                        log.debug("Heartbeat succeeded and received response: " + response);


                        //call next one
                        timer.schedule (callNextHeartbeat, AppConstants.TIME_BETWEEN_HEARTBEATS);

                    } finally {
                        state.releaseAccessToModel();
                    }});
        return;
    }
    /**
     * Starts the controller's heartbeat function.
     *
     * @return true if the heartbeat was successfully started
     */
    private void startHeartbeat() {
        sendHeartbeat();
        return;
    }

    /**
     * Stops the controller's heartbeat function.
     *
     * @return true if the heartbeat was successfully stopped
     */
    private void stopHeartbeat() {
        timer.cancel();
        return;
    }

    /**
     * Login without any local credentials. Will prompt the user for the credentials.
     */
    private void loginWithoutModel() {
        // First, clear any client data that may have been corrupted.
        clearClientData();

        // Initiate the view.
        view.setEnableSubmit(true);
        lockFrameView.addPanel(view.getLoginPanel());
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
                    // No status change. Just send them to login screen.
                    loginWithoutModel();
                }

                // Check if there is a current status.
                ClientStatusPojo currentStatus = state.getCurrentStatus();
                if (currentStatus == null) {
                    log.error("Login with model but no model was found.");
                    retrieveClientState();
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
                    view.setEnableSubmit(true);
                    view.setInformationText("Login failed. Please try again.");
                }

                log.debug("Successfully retrieved typing profile: " + response);
                // If this succeeded, we can remove the frame.
                lockFrameView.unlock();
                lockFrameView.hidePanel(view.getLoginPanel());

                // Enqueue the response as the new status.
                ClientStatusPojo newStatus = castToClientStatus(response.getBody(), token);
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
        // TODO: Implement lockLocalSave()
        return false;
    }

    /**
     * Cast the response from the server to a new client status.
     *
     * @param responseContainer response from the server
     * @param token the access token used to call the server
     * @return new client status based on response
     */
    private ClientStatusPojo castToClientStatus(@NonNull TypingProfileContainerResponse responseContainer, @NonNull String token) {
        TypingProfileResponse response = responseContainer.getTypingProfile();
        if (response == null) return null;

        return new ClientStatusPojo(
                new TypingProfilePojo(response.get_id(), response.getMachine(), response.getUser(),
                        response.getTensorFlowModel(),
                        response.getThreshold(),
                        castToChallengeStrategy(response.getChallengeStrategies()),
                response.getEndpoint()),
                AuthConstants.AUTHENTICATED,
                castToSecurityConstant(response.isLocked()),
                token,
                responseContainer.getPhoneNumber(),
                System.currentTimeMillis());
    }

    /**
     * Get computer's MAC address as a string representation.
     *
     * @return string representation of MAC
     */
    private String getMAC() {
        //TODO Tony fix this somehow
        try {
            byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
            return new String(mac);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Casts the string representations of challenge strategies from the server to the correct IChallengeStrategy impl.
     *
     * @param challengeStrategies array of string representations of challenge strategies from the server
     * @return array of accepted IChallengeStrategy impl
     */
    private IChallengeStrategy[] castToChallengeStrategy(@NonNull String[] challengeStrategies) {
        if (challengeStrategies.length == 0) return null;

        List<IChallengeStrategy> acceptedStrategies = new ArrayList<>();
        for (int i = 0; i < challengeStrategies.length; i++) {
            if (strategies.containsKey(challengeStrategies[i])) {
                acceptedStrategies.add(strategies.get(challengeStrategies[i]));
            }
        }
        return acceptedStrategies.toArray(new IChallengeStrategy[acceptedStrategies.size()]);
    }

    /**
     * Cast the boolean representation of security constant to the correct enum object.
     *
     * @param isLocked boolean representation of security constant.
     * @return the correct enum object
     */
    private SecurityConstants castToSecurityConstant(boolean isLocked) {
        return (isLocked) ? SecurityConstants.LOCKED : SecurityConstants.UNLOCKED;
    }
}
