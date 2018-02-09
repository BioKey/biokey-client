package com.biokey.client.services;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service that runs the analysis model and reports analysis results that represent the likelihood that the user's
 * typing matches their current profile.
 */
public class AnalysisEngineService implements ClientStateModel.IClientStatusListener, ClientStateModel.IClientKeyListener {

    @Autowired
    private ClientStateController controller;
    @Autowired
    private ClientStateModel state;

    /**
     * Implementation of listener to the ClientStateModel's status. The status will contain the details
     * on the analysis model to run through the typing profile.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {
        // TODO: needs more thought on different cases
        /*
         * If the typing profile is loaded, start analyzing.
         * If the typing profile becomes null, stop analyzing.
         */
        if(newStatus.getProfile() != null) {
            start();
        }
        else {
            stop();
        }

        /*
         * If the client becomes authenticated, start analyzing.
         * If the client becomes unauthenticated, stop analyzing.
         */
        if(oldStatus.getAuthStatus() != newStatus.getAuthStatus()) {
            if(newStatus.getAuthStatus() == AuthConstants.AUTHENTICATED) {
                start();
            }
            else {
                stop();
            }
        }

        /*
         * If the client is newly challenged, stop logging keystrokes.
         * If the client is newly 'unlocked', start logging keystrokes
         */
        if(oldStatus.getSecurityStatus() != newStatus.getSecurityStatus()) {
            if(oldStatus.getSecurityStatus() == SecurityConstants.UNLOCKED) {
                stop();
            }
            else if(newStatus.getSecurityStatus() == SecurityConstants.UNLOCKED ) {
                start();
            }
        }
    }

    /**
     * Implementation of listener to the ClientStateModel's keystroke queues. New keys need to be fed into the model.
     */
    public void keystrokeQueueChanged(KeyStrokePojo newKey) {
        //TODO: Implement keystrokeQueueChanged()
        return;
    }

    /**
     * Start running the analysis engine.
     *
     * @return true if analysis engine successfully started
     */
    private boolean start() {
        return false;
    }

    /**
     * Stop running the analysis engine.
     *
     * @return true if analysis engine successfully stopped
     */
    private boolean stop() {
        return false;
    }

    /**
     * Use the current keystroke data to generate the likelihood that the user's typing matches their current profile.
     *
     * @return the likelihood expressed as a decimal value between 0 and 1.
     */
    private float analyze() {
        return 0;
    }
}
