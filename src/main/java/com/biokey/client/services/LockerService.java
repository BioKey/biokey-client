package com.biokey.client.services;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service that responds to changes in client status and locks or unlocks the OS accordingly.
 */
public class LockerService implements ClientStateModel.IClientStateListener {

    @Autowired
    private ClientStateController controller;
    @Autowired
    private ClientStateModel state;

    /**
     * Implementation of listener to the ClientStateModel. The status will contain the accepted strategies to challenge
     * the user and a flag for whether the locker should lock or unlock.
     */
    public void stateChanged() {

    }

    /**
     * Lock the OS.
     *
     * @return true if OS successfully locked
     */
    private boolean lock() {
        return false;
    }

    /**
     * Unlock the OS.
     *
     * @return true if OS successfully unlocked
     */
    private boolean unlock() {
        return false;
    }

    /**
     * Issues the challenges based on accepted strategies and notify client state of the result.
     */
    private void issueChallenges() {
        return;
    }

}
