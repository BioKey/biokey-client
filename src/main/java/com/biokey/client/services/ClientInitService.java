package com.biokey.client.services;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.AppConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import com.biokey.client.models.response.LoginResponse;
import com.biokey.client.models.response.TypingProfileContainerResponse;
import com.biokey.client.views.GoogleAuthChallengeView;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
    private JTextField emailInput;
    private JPanel loginPanel;
    private JPasswordField passwordInput;
    private JButton submitButton;
    private JLabel informationLabel;

    public ClientInitService() {

        submitButton.addActionListener(new ActionListener() {
            /**
             * This is invoked when the user attempts to login with an eamil/password combo
             *
             * @param e unused
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    controller.sendLoginRequest(emailInput.getText(), new String (passwordInput.getPassword()), (ResponseEntity<LoginResponse> response) -> {
                        // First, make sure to get the lock.
                        state.obtainAccessToModel();
                        try {
                            // Check if the response was good.
                            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                                log.debug("Login failed and received response " + response);
                                //on failed login, do nothing other than show message to user that login failed.
                                return;
                            }

                            //if successful go onto next function to retreive state given token and mac

                            log.debug("Login Succeeded and received response: " + response);
                            // call next function to get model with access token
                            try {
                                getTypingProfileGivenAuthandMAC(response.getBody().getToken());
                            } catch (JsonProcessingException e2) {
                                e2.printStackTrace();
                            }
                        } finally {
                            state.releaseAccessToModel();
                        }
                    });

                } catch (JsonProcessingException e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    /**
     * Login to
     * @param token the token to use to authenticate the user
     * @throws JsonProcessingException
     */
    public void getTypingProfileGivenAuthandMAC (String token) throws JsonProcessingException
    {
        //hardcode
        controller.retrieveTypingProfileGivenAuthandMAC("ABC",token,(ResponseEntity<TypingProfileContainerResponse> response) -> {
            log.debug("Token: " + token);
            // First, make sure to get the lock.
            state.obtainAccessToModel();
            try {
                // Check if the response was good.
                if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                    log.debug("Error occurred when retreiving typing profile " + response);
                    //if this failed, leave the frame still running and update message
                    informationLabel.setText("Login failed. Please try again.");
                    return;
                }


                log.debug("Successfuly retreived typing profile: " + response);
                //if this succeeded, we can remove the frame
                submitButton.setEnabled(false);
                this.dispose(); //TODO: won't disappear for some reason need to figure this one out
                log.debug("Dispose now");


                //TODO: update client status. Not 100% sure what to do. Maybe we need an enque typing profile method?
               /* ClientStatusPojo currentStatus = state.getCurrentStatus();
                ClientStatusPojo newStatus = new ClientStatusPojo(currentStatus.getProfile(),AuthConstants.AUTHENTICATED,
                        currentStatus.getSecurityStatus(),currentStatus.getAccessToken(),currentStatus.getTimeStamp(),currentStatus.getPhoneNumber());
                state.enqueueStatus(newStatus);*/

            } finally {
                state.releaseAccessToModel();
            }
        });
    }
    /**
     * Implementation of listener to the ClientStateModel's status. The service will save the client state periodically.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {

        saveClientState();

        /*
         * If the typing profile is loaded, start the heartbeat.
         * If the typing profile becomes null, stop the heartbeat
         */
        if(newStatus.getProfile() != null) startHeartbeat();
        else stopHeartbeat();

        /*
         * If the client becomes authenticated, start the heartbeat.
         * If the client becomes unauthenticated, stop the heartbeat.
         */
        if(oldStatus.getAuthStatus() != newStatus.getAuthStatus()){
            if(newStatus.getAuthStatus() == AuthConstants.AUTHENTICATED) startHeartbeat();
            else stopHeartbeat();
        }
    }

    /**
     * Implementation of listener to the ClientStateModel's keystroke queues.
     * The service will save the client state periodically.
     */
    public void keystrokeQueueChanged(KeyStrokePojo newKey) {
        //TODO: Implement keystrokeQueueChanged()
        return;
    }

    /**
     * Implementation of listener to the ClientStateModel's analysis results queue.
     * The service will save the client state periodically.
     */
    public void analysisResultQueueChanged(AnalysisResultPojo newResult) {
        //TODO: Implement analysisResultQueueChanged()
        return;
    }

    /**
     * Entry point into this service. Tries to retrieve the client state and in the process
     * calls the {@link #checkCorrupt()} and at least one of the login functions.
     */
    public void retrieveClientState() {
        // TODO: call checkCorrupt() and login() -> which login() based on if retrieve was successful
        state.obtainAccessToModel();
        try {
            FileInputStream fis = new FileInputStream(AppConstants.LOCAL_STATE_PATH);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ClientStateModel fromMemory = (ClientStateModel) ois.readObject();
            controller.passStateToModel(fromMemory);
            log.debug("Retrieved client state from file");
        } catch (Exception e) {
            log.error("Could not retrieve initial client state from file", e);
            loginNoToken(); //JOSH added this. I hope this was ok and doesn't screw with BK's stuff...
        } finally {
            state.releaseAccessToModel();
        }
    }

    /**
     * Called when the status of the client changes. Saves the entire state to the disk and OS.
     *
     * @return true if the save was successful
     */
    public void saveClientState() {
        // TODO: make sure that when you are writing to file, you are deleting the old data and overwriting, not appending!
        Runnable r = () -> {
            state.obtainAccessToModel();
            try {
                FileOutputStream fos = new FileOutputStream(AppConstants.LOCAL_STATE_PATH);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(state);
                oos.close();
                log.debug("Saved client state to file");
            }
            catch (Exception e){
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
     *
     * @return true if the user successfully logged in
     */
    private boolean loginNoToken() {

        JFrame frame = new JFrame("Google Auth Challenge Strategy");
        frame.setContentPane(loginPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return false;
    }

    /**
     * Login with local credentials (auth token). Will call the no parameter version if the credentials are not validated by server.
     *
     * @return true if the user successfully logged in
     */
    private boolean loginWithToken() {

        controller.confirmAccessToken((ResponseEntity<String> response) -> {
            state.obtainAccessToStatus();

            try {
                // Check if the response was good.
                if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                    log.debug("Access token did not authenticate and received response: " + response);
                    // no status change. Just send them to login screen
                    loginNoToken();
                }

                // If it was good then enqueue new status where authenticated is true if it currently isn't
                log.debug("User successfully authenticated and received response: " + response);
                if (state.getCurrentStatus().getAuthStatus() == AuthConstants.UNAUTHENTICATED)
                {
                    ClientStatusPojo currentStatus = state.getCurrentStatus();
                    ClientStatusPojo newStatus = new ClientStatusPojo(currentStatus.getProfile(),AuthConstants.AUTHENTICATED,
                    currentStatus.getSecurityStatus(),currentStatus.getAccessToken(),currentStatus.getTimeStamp(),currentStatus.getPhoneNumber());
                    state.enqueueStatus(newStatus);
                }

            } finally {
                state.releaseAccessToStatus();
            }
        });
        return false; //can't figure out how to do this return properly
    }

    /**
     * Check the local files and OS for signs of corruption.
     *
     * @return true if corruption was detected
     */
    private boolean checkCorrupt() {
        return true;
    }

    /**
     * Lock the local files securely.
     *
     * @return true if the lock was successfully completed
     */
    private boolean lockLocalSave() {
        return false;
    }
}
