package com.biokey.client.services;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service that listens for messages enqueued by the server and responds by registering the changes to the client state.
 */
public class ServerListenerService {

    @Autowired
    private ClientStateController controller;
    @Autowired
    private ClientStateModel state;

    /**
     * Implementation of listener to the ClientStateModel.
     * The status will contain a flag for whether the service should be running.
     */
    @Getter
    private ClientStateModel.IClientStateListener listener = () -> {
        return;
    };

    /**
     * Start running the server listener.
     *
     * @return true if server listener successfully started
     */
    private boolean start() {
        return false;
    }

    /**
     * Stop running the server listener.
     *
     * @return true if server listener successfully stopped
     */
    private boolean stop() {
        return false;
    }

    /**
     * Read a server message and register the change to the client state.
     */
    private void dequeueServerMessage() {
        return;
    }
}
