package com.biokey.client.services;

import com.biokey.client.models.pojo.ClientStatusPojo;

/**
 * Interface describing the actions that all listeners to the client data must provide.
 */
public interface IClientStateListener {

    /**
     * The logic that will be run when the client data determines it has changed.
     *
     * @param status the current status of the client is passed when listener is notified
     */
    void stateChanged(ClientStatusPojo status);
}
