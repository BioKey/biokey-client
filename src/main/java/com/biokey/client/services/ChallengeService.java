package com.biokey.client.services;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.controllers.challenges.IChallengeStrategy;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.event.ActionEvent;

/**
 * Service that responds to changes in client status and locks or unlocks the OS accordingly.
 */
public class ChallengeService implements ClientStateModel.IClientStatusListener, ClientStateModel.IClientAnalysisListener {

    private ClientStateController controller;
    private ClientStateModel state;

    @Autowired
    public ChallengeService(ClientStateController controller, ClientStateModel state) {
        this.controller = controller;
        this.state = state;
    }

    /**
     * Implementation of listener to the ClientStateModel's status. The status will contain the accepted strategies
     * to challenge the user and a flag for whether the locker should lock or unlock.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {

        // TODO: needs more thought on different cases
        /*
         * If the client has been newly challenged, issue a challenge.
         * If the client has failed the challenge, lock the OS.
         * If the client state is newly 'unlocked', unlock the OS.
         */
        /*
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
        */
    }

    /**
     * Implementation of listener to the ClientStateModel's analysis results queue. The queue contains updates to the
     * client's authentication, and the locker will decide whether a state change is necessary.
     */
    public void analysisResultQueueChanged(AnalysisResultPojo newResult) {
        //TODO: Implement analysisResultQueueChanged()
        return;
    }

    /**
     * Issues the challenges based on accepted strategies and notify client state of the result.
     */
    private void issueChallenges() {
        // TODO: set visible, show user the phone number...
        /**
        view.addSendAction((ActionEvent aE) -> {
            try {
                password = sendPassword();
            } catch (IChallengeStrategy.ChallengeException e) {
                log.error("Text Message Challenge: send message failed.", e);
                this.view.setInformationText("Failed to send text message. Please contact administrator.");
            }
        });
         */
        return;
    }

}