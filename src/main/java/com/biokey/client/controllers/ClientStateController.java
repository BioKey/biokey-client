package com.biokey.client.controllers;

import com.biokey.client.models.ClientStateModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handles requests by services to make changes to the client data.
 */
public class ClientStateController {

    @Autowired
    ClientStateModel state;

    /**
     * Sends the server a message at fixed time intervals to let it know when the client is alive.
     */
    public void sendHeartbeat() {
        return; //TODO: Implement heartbeat().
    }

    /**
     * Sends the server a request notifying it about an action that has occurred that changed the client data.
     * Modifies client data after request. If the request was not successful, saves a copy of the past data.
     *
     * @return true if the request to the server succeeded
     */
    public boolean sendRequest() {
        return false; //TODO: Generic request as a template, delete after all the requests have been determined.
    }

    /**
     * Sends the server a request notifying it about a status change in the client.
     * Modifies client data after request. If the request was not successful, saves a copy of the past data.
     *
     * @return true if the request to the server succeeded
     */
    public boolean sendStatusChange() {
        return false; //TODO: Implement sendStatusChange().
    }

    /**
     * Sends the server a request with all of the client analysis results not yet known to server.
     * Modifies client data after request is successfully sent.
     *
     * @return true if the request to the server succeeded
     */
    public boolean sendAnalysisResults() {
        return false;
    }

    /**
     * Sends the server a request with all of the client keystrokes not yet known to server.
     * Modifies client data after request is successfully sent.
     *
     * @return true if the request to the server succeeded
     */
    public boolean sendKeyStrokes() {
        return false;
    }

    /**
     * Sends the server a request with all of the client data changes not yet known to server.
     * Modifies client data after request is successfully sent.
     *
     * @return true if the request to the server succeeded
     */
    private boolean sendPastStates() {
        return false;
    }

}
