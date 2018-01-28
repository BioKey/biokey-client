package com.biokey.client.services;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.KeyStrokePojo;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service that retrieves the client state from the disk and OS and ensures that it has not been corrupted.
 * If the local data does not exist, then the service prompts the user to login and download the client state.
 */
public class ClientInitService {

    @Autowired
    private ClientStateController controller;
    @Autowired
    private ClientStateModel state;

    /**
     * Implementation of listener to the ClientStateModel. The service will save the client state periodically.
     */
    @Getter
    private ClientStateModel.IClientStateListener listener = () -> {
        return;
    };

    /**
     * Entry point into this service. Tries to retrieve the client state and in the process
     * calls the {@link #checkCorrupt()} and at least one of the {@link #login()} functions.
     */
    public void retrieveClientState() {
        //TODO: delete the below, used to make sure autowiring is working!!!! :)

        KeyStrokePojo test = new KeyStrokePojo('t', true, 1);
        state.enqueueKeyStroke(test);
        System.out.println(state.getKeyStrokes().peek() == test);
        return;
    }

    /**
     * Called when the status of the client changes. Saves the entire state to the disk and OS.
     *
     * @return true if the save was successful
     */
    private boolean saveClientState() {
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
     * @param accessToken
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
