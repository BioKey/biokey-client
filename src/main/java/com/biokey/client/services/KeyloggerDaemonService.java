package com.biokey.client.services;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service that records user key strokes in the background and registers them to the client state.
 */
public class KeyloggerDaemonService implements ClientStateModel.IClientStateListener {

    @Autowired
    private ClientStateController controller;
    @Autowired
    private ClientStateModel state;

    /**
     * Implementation of listener to the ClientStateModel.
     * The status will contain a flag for whether the daemon should be running.
     */
    public void stateChanged() {

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
