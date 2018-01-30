package com.biokey.client.services;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service that records user key strokes in the background and registers them to the client state.
 */
public class KeyloggerDaemonService implements ClientStateModel.IClientStatusListener {

    @Autowired
    private ClientStateController controller;
    @Autowired
    private ClientStateModel state;

    /**
     * Implementation of listener to the ClientStateModel's status.
     * The status will contain a flag for whether the daemon should be running.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {

        /*
         * If the typing profile is loaded, start logging keystrokes.
         * If the typing profile becomes null, stop logging keystrokes.
         */
        if(newStatus.getProfile() != null) start();
        else stop();

        /*
         * If the client becomes authenticated, start logging keystrokes.
         * If the client becomes unauthenticated, stop logging keystrokes.
         */
        if(oldStatus.getAuthStatus() != newStatus.getAuthStatus()){
            if(newStatus.getAuthStatus() == AuthConstants.AUTHENTICATED) start();
            else stop();
        }
    }

    /**
     * Start running the daemon.
     *
     * @return true if daemon successfully started
     */
    private boolean start() {
        return false;
    }

    /**
     * Stop running the daemon.
     *
     * @return true if daemon successfully stopped
     */
    private boolean stop() {
        return false;
    }

    /**
     * Register keystrokes with the client state.
     */
    private void send() {
        return;
    }

}
