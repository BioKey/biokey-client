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

    private ClientStateController controller;
    private ClientStateModel state;

    @Autowired
    public KeyloggerDaemonService(ClientStateController controller, ClientStateModel state) {
        this.controller = controller;
        this.state = state;
    }

    /**
     * Implementation of listener to the ClientStateModel's status.
     * The status will contain a flag for whether the daemon should be running.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {

        // TODO: needs more thought on different cases
        /*
         * If the client becomes authenticated, start logging keystrokes.
         * If the client becomes unauthenticated, stop logging keystrokes.
         */
        /*
        if(oldStatus.getAuthStatus() != newStatus.getAuthStatus()){
            if(newStatus.getAuthStatus() == AuthConstants.AUTHENTICATED) start();
            else stop();
        }
        */
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
