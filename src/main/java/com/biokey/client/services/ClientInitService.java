package com.biokey.client.services;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.AppConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Service that retrieves the client state from the disk and OS and ensures that it has not been corrupted.
 * If the local data does not exist, then the service prompts the user to login and download the client state.
 */
public class ClientInitService implements
        ClientStateModel.IClientStatusListener,
        ClientStateModel.IClientKeyListener,
        ClientStateModel.IClientAnalysisListener {

    private static Logger log = Logger.getLogger(ClientInitService.class);

    @Autowired
    private ClientStateController controller;
    @Autowired
    private ClientStateModel state;

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
     * calls the {@link #checkCorrupt()} and at least one of the {@link #login()} functions.
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
    private boolean login() {
        return false;
    }

    /**
     * Login with local credentials. Will call the no parameter version if the credentials are not validated by server.
     *
     * @param accessToken token created from prior login to keep client logged in
     * @return true if the user successfully logged in
     */
    private boolean login(String accessToken) {
        return false;
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
