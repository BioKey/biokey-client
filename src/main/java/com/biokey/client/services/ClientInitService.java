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
import lombok.NonNull;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Service that retrieves the client state from the disk and OS and ensures that it has not been corrupted.
 * If the local data does not exist, then the service prompts the user to login and download the client state.
 */
public class ClientInitService extends JFrame implements
        ClientStateModel.IClientStatusListener,
        ClientStateModel.IClientKeyListener,
        ClientStateModel.IClientAnalysisListener {

    private static Logger log = Logger.getLogger(ClientInitService.class);

    @Autowired
    private ClientStateController controller;
    @Autowired
    private ClientStateModel state;
    @Autowired
    private Map<String, IChallengeStrategy> strategies;

    private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
    private int newKeyCount = 0;

    private JTextField emailInput;
    private JPanel loginPanel;
    private JPasswordField passwordInput;
    private JButton submitButton;
    private JLabel informationLabel;

    public ClientInitService() {
        initLoginForm();
    }

    /**
     * Adds action listeners to the login form.
     */
    private void initLoginForm() {
        submitButton.addActionListener((ActionEvent e) -> {
            controller.sendLoginRequest(emailInput.getText(), new String(passwordInput.getPassword()),
                    (ResponseEntity<LoginResponse> response) -> {
                // Check if the response was good.
                if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody().getToken() == null) {
                    log.debug("Login failed and received response " + response);
                    // On failed login, show message to user that login failed.
                    informationLabel.setText("Login failed. Please try again.");
                    return;
                }

                // If successful, call next function to retrieve status.
                log.debug("Login Succeeded and received response: " + response);
                String mac = getMAC();
                if (mac == null) {
                    log.debug("Could not retrieve MAC address.");
                    informationLabel.setText("Login failed. Could not retrieve MAC address. Please try again.");
                } else retrieveStatusFromServer(getMAC(), response.getBody().getToken());
            });
        });
    }

    /**
     * Implementation of listener to the ClientStateModel's status. The service will save the client state periodically.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {
        saveClientState();

        // TODO: needs more thought on different cases
        /*
         * If the client becomes authenticated, start the heartbeat.
         * If the client becomes unauthenticated, stop the heartbeat.
         */
        if (oldStatus.getAuthStatus() != newStatus.getAuthStatus()){
            if(newStatus.getAuthStatus() == AuthConstants.AUTHENTICATED) startHeartbeat();
            else stopHeartbeat();
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
            log.debug("Could not retrieve initial client state from file", e);
            loginWithoutModel();
            return;
        } finally {
            state.releaseAccessToModel();
        }

        // TODO: checkCorrupt()
        if (controller.checkStateModel(fromMemory)) {
            log.debug("Retrieved client state from file");
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

    /**
     * Starts the controller's heartbeat function.
     *
     * @return true if the heartbeat was successfully started
     */
    private boolean startHeartbeat() {
        //TODO: Implement startHeartbeat().
        return false;
    }

    /**
     * Stops the controller's heartbeat function.
     *
     * @return true if the heartbeat was successfully stopped
     */
    private boolean stopHeartbeat() {
        //TODO: Implement stopHeartbeat().
        return false;
    }

    /**
     * Login without any local credentials. Will prompt the user for the credentials.
     */
    private void loginWithoutModel() {
        // TODO: somehow lock
        JFrame frame = new JFrame("Login");
        frame.setContentPane(loginPanel);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
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

                log.debug("User successfully authenticated and received response: " + response);
                // If response was good then enqueue new status.
                if (!state.getCurrentStatus().getAuthStatus().equals(AuthConstants.AUTHENTICATED)) { // TODO: check null
                    controller.enqueueStatus(controller.createStatusWithAuth(state.getCurrentStatus(), AuthConstants.AUTHENTICATED));
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
        // TODO: create new machine on server? create typing profile on server?
        controller.retrieveStatusFromServer(mac, token, (ResponseEntity<TypingProfileContainerResponse> response) -> {
            // First, make sure to get the lock.
            state.obtainAccessToStatus();
            try {
                // Check if the response was good.
                if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                    log.debug("Error occurred when retrieving typing profile " + response);
                    // On failed retrieval, show message to user that it failed.
                    informationLabel.setText("Login failed. Please try again.");
                }

                log.debug("Successfully retrieved typing profile: " + response);
                // If this succeeded, we can remove the frame.
                submitButton.setEnabled(false);
                this.dispose(); //TODO: make sure this disappears

                // Enqueue the response as the new status.
                ClientStatusPojo newStatus = castToClientStatus(response.getBody(), token);
                controller.enqueueStatus(newStatus);
            } finally {
                state.releaseAccessToStatus();
            }
        });
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
    private SecurityConstants castToSecurityConstant(@NonNull boolean isLocked) {
        return (isLocked) ? SecurityConstants.LOCKED : SecurityConstants.UNLOCKED;
    }
}
