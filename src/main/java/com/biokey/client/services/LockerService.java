package com.biokey.client.services;

import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
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
    public void stateChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {

        /**
         * If the client has been newly challenged, issue a challenge.
         * If the client has failed the challenge, lock the OS.
         * If the client state is newly 'unlocked', ulock the OS.
         */
        if(oldStatus.getSecurityStatus() != newStatus.getSecurityStatus()) {
            if(newStatus.getSecurityStatus() == SecurityConstants.CHALLENGE) {
                issueChallenges();
            }
            if(newStatus.getSecurityStatus() == SecurityConstants.LOCKED) {
                lock();
            }
            if(newStatus.getSecurityStatus() == SecurityConstants.UNLOCKED) {
                unlock();
            }
        }
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
