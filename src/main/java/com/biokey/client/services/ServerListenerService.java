package com.biokey.client.services;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service that listens for messages enqueued by the server and responds by registering the changes to the client state.
 */
public class ServerListenerService implements ClientStateModel.IClientStatusListener {

    private ClientStateController controller;
    private ClientStateModel state;

    @Autowired
    public ServerListenerService(ClientStateController controller, ClientStateModel state) {
        this.controller = controller;
        this.state = state;
    }

    /**
     * Implementation of listener to the ClientStateModel's status.
     * The status will contain a flag for whether the service should be running.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {
        start();
    }

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
