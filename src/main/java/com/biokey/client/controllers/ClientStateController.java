package com.biokey.client.controllers;

import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
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

        // TODO: Implement sendStatusChange().

        // ClientStatusPojo oldStatus = state.getCurrentStatus(); //Gets old state
        // ClientStatusPojo currentStatus = state.unsyncedStatuses.peek; //Gets new state
        // try state.setStatus(); //Tries to set the new state

        // If it works, pop and notify listeners of the change.
        // state.unsyncedStatuses.pop
        // state.notifyStatusChange(oldStatus, currentStatus);

        // Send the status change to the server.

        return false;
    }

    /**
     * Sends the server a request with all of the client analysis results not yet known to server.
     * Modifies client data after request is successfully sent.
     *
     * @return true if the request to the server succeeded
     */
    public boolean sendAnalysisResults() {

        // TODO: Implement sendAnalysisResults()

        state.notifyAnalysisResultQueueChange();

        // Send the change to the server.

        return false;
    }

    /**
     * Sends the server a request with all of the client keystrokes not yet known to server.
     * Modifies client data after request is successfully sent.
     *
     * @return true if the request to the server succeeded
     */
    public boolean sendKeyStrokes() {

        // TODO: Implement sendKeyStrokes

        state.notifyKeyQueueChange();

        // Send the change to the server.

        return false;
    }

    /**
     * Sends the server a request with all of the client data changes not yet known to server.
     * Modifies client data after request is successfully sent.
     *
     * @return true if the request to the server succeeded
     */
    private boolean sendPastStates() {

        // TODO: Implement sendPastStates

        state.notifyStatusQueueChange();

        // Send the change to the server.

        return false;
    }

}
