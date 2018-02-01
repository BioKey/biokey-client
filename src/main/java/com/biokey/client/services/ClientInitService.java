package com.biokey.client.services;

import com.biokey.client.App;
import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.AppConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.security.MessageDigest;
import java.util.concurrent.ExecutionException;
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
    private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
    private static int newKeyCount = 0;
    private static final MessageDigest sha1 = DigestUtils.getSha1Digest();

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
        if (++newKeyCount >= AppConstants.KEYSTROKE_WINDOW_SIZE_PER_SAVE) {
            saveClientState();
        }
        //TODO: Implement keystrokeQueueChanged()
        return;
    }

    /**
     * Implementation of listener to the ClientStateModel's analysis results queue.
     * The service will save the client state periodically.
     */
    public void analysisResultQueueChanged(AnalysisResultPojo newResult) {
        saveClientState();
        //TODO: Implement analysisResultQueueChanged()
        return;
    }

    /**
     * Entry point into this service. Tries to retrieve the client state and in the process
     * calls the {@link } and at least one of the {@link #login()} functions.
     */
    public void retrieveClientState() {

        state.obtainAccessToModel();
        try {
            /* Read from file
            FileInputStream fis = new FileInputStream(AppConstants.LOCAL_STATE_PATH);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ClientStateModel fromMemory = (ClientStateModel) ois.readObject();
            */

            byte[] stateBytes = prefs.getByteArray(AppConstants.CLIENT_STATE_PREFERENCES_ID, new byte[0]);

            if (stateBytes.length == 0) {
                log.debug("New user!");
                login();
            }
            else if (checkCorrupt(stateBytes)) {
                ClientStateModel fromMemory = (ClientStateModel) SerializationUtils.deserialize(stateBytes);
                controller.passStateToModel(fromMemory);
                login(state.getCurrentStatus().getAccessToken());
                log.debug("Retrieved client state from file");
            }
            else {
                log.debug("Checksum invalid!");
            }
        } catch (Exception e) {
            if(e instanceof ClassCastException || e instanceof SerializationException) {
                log.debug("Client state could not be cast");
                login();
                // TODO: Finalize behaviour. Lock? Allow a new login?
            }
            else log.debug("Could not retrieve initial client state from file", e);

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
        Runnable r = () -> {
            state.obtainAccessToModel();
            try {
                /* Write to file
                FileOutputStream fos = new FileOutputStream(AppConstants.LOCAL_STATE_PATH, false);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(state);
                oos.flush()
                oos.close();
                */

                /* Checksums
                byte[] checksum = sha1.digest(SerializationUtils.serialize(stateBytes));
                prefs.put(AppConstants.CLIENT_STATE_CHECKSUM_PREFERENCES_ID, new String(checksum));
                */

                byte[] stateBytes = SerializationUtils.serialize(state);
                prefs.putByteArray(AppConstants.CLIENT_STATE_PREFERENCES_ID, stateBytes);

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
        return true;
    }

    /**
     * Login with local credentials. Will call the no parameter version if the credentials are not validated by server.
     *
     * @param accessToken token created from prior login to keep client logged in
     * @return true if the user successfully logged in
     */
    private boolean login(String accessToken) {
        return true;
    }

    /**
     * Check the local files and OS for signs of corruption.
     *
     * @return true if corruption was detected
     */
    private boolean checkCorrupt(byte [] stateBytes) {
        // TODO: Implement checkCorrupt()
        // Read from Preferences, calculate checksum
        /*
        String candidateChecksum = new String(sha1.digest(SerializationUtils.serialize(stateBytes)));
        String targetChecksum = prefs.get(AppConstants.CLIENT_STATE_CHECKSUM_PREFERENCES_ID,"");
        log.debug(candidateChecksum);
        return targetChecksum.equals(candidateChecksum);
        */
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
