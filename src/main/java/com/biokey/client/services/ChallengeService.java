package com.biokey.client.services;

import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.controllers.challenges.IChallengeStrategy;
import com.biokey.client.helpers.PojoHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.views.frames.LockFrameView;
import com.biokey.client.views.panels.LockedPanelView;
import com.biokey.client.views.panels.challenges.ChallengeOptionPanelView;
import com.biokey.client.views.panels.challenges.ChallengePanelView;
import net.ericaro.neoitertools.Lambda;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static com.biokey.client.constants.AppConstants.DEFAULT_THRESHOLD;
import static com.biokey.client.constants.AppConstants.MAX_CHALLENGE_ATTEMPTS;

/**
 * Service that responds to changes in client status and locks or unlocks the OS accordingly.
 */
public class ChallengeService implements ClientStateModel.IClientStatusListener, ClientStateModel.IClientAnalysisListener {

    private static Logger log = Logger.getLogger(ChallengeService.class);

    private final ClientStateController controller;
    private final ClientStateModel state;
    private final LockFrameView lockFrame;
    private final ChallengeOptionPanelView optionView;
    private final LockedPanelView lockPanel;
    private final Map<String, IChallengeStrategy> strategies;
    private final Map<IChallengeStrategy, ChallengePanelView> strategyViewPairs;

    private int remainingAttempts = MAX_CHALLENGE_ATTEMPTS;

    private LinkedBlockingQueue<Float> previous10Results;
    private LinkedBlockingQueue<Float> previous20Results;


    @Autowired
    public ChallengeService(
            ClientStateController controller, ClientStateModel state,
            LockFrameView lockFrameView, ChallengeOptionPanelView challengeOptionPanelView, LockedPanelView lockPanel,
            Map<String, IChallengeStrategy> strategies, Map<IChallengeStrategy, ChallengePanelView> strategyViewPairs) {

        this.controller = controller;
        this.state = state;
        this.strategies = strategies;
        this.strategyViewPairs = strategyViewPairs;
        this.lockFrame = lockFrameView;
        this.optionView = challengeOptionPanelView;
        this.lockPanel = lockPanel;

        previous20Results = new LinkedBlockingQueue<>();
        previous10Results = new LinkedBlockingQueue<>();

        // Define relationship between strategy and view.
        for (IChallengeStrategy strategy : strategyViewPairs.keySet()) {
            ChallengePanelView view = strategyViewPairs.get(strategy);

            // Add the following actions.
            view.addSendAction((ActionEvent aE) -> {
                if (remainingAttempts <= 0) return;
                view.setEnableSend(false);
                view.setEnableResend(true);
                strategy.issueChallenge();
            });
            view.addResendAction((ActionEvent aE) -> {
                if (remainingAttempts <= 0) return;
                strategy.issueChallenge();
            });
            view.addAltAction((ActionEvent aE) -> {
                // lockFrameView.removeAllPanels();
                lockFrameView.addPanel(optionView.getChallengeOptionPanel());
                lockFrameView.removePanel(view.getChallengePanel());

            });
            view.addKeyAction((ActionEvent aE) -> {
                boolean isValid = strategy.validateChallenge(view.getCode());
                if (isValid) {
                    state.obtainAccessToStatus();
                    try {
                        ClientStatusPojo currentStatus = state.getCurrentStatus();
                        if (currentStatus == null) {
                            // If for some reason it is null, there is a big problem, let another service handle it.
                            log.error("No model detected in the middle of challenge.");
                            return;
                        }

                        // Clear code and decrement attempts.
                        remainingAttempts--;

                        // Check if attempt was good.
                        if (strategy.checkChallenge(view.getCode())) {
                            // Passed, enqueue UNLOCKED status.
                            controller.enqueueStatus(PojoHelper.createStatus(currentStatus, SecurityConstants.UNLOCKED));
                        } else if (remainingAttempts <= 0) {
                            // Failed too many times, enqueue LOCKED status.
                            controller.enqueueStatus(PojoHelper.createStatus(currentStatus, SecurityConstants.LOCKED));
                        } else {
                            // Failed, so let the user know.
                            view.setInformationText("Hmmm... not what we were expecting. You have " + remainingAttempts + " attempts left.");
                        }
                    } finally {
                        view.clearCode();
                        state.releaseAccessToStatus();
                    }
                }
            });
        }
    }

    /**
     * Implementation of listener to the ClientStateModel's status. The status will contain the accepted strategies
     * to challenge the user and a flag for whether the locker should lock or unlock.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus, boolean isDeleteEvent) {
        if (isDeleteEvent) return;
        SecurityConstants oldLockedStatus = (oldStatus == null) ? null : oldStatus.getSecurityStatus();
        SecurityConstants newLockedStatus = (newStatus == null) ? null : newStatus.getSecurityStatus();

        // First, init the challenge services.
        if (newStatus != null) {
            IChallengeStrategy[] challengeStrategies =
                    PojoHelper.castToChallengeStrategy(strategies, newStatus.getProfile().getAcceptedChallengeStrategies());
            if (challengeStrategies != null) {
                for (IChallengeStrategy strategy : challengeStrategies) strategy.init();
                if (challengeStrategies.length == 0) log.warn("Administrator has not set up any ways to authenticate.");
            }
        }

        // Second, check if we should show or hide challenge panels.
        if (oldLockedStatus != newLockedStatus && newLockedStatus == SecurityConstants.CHALLENGE) issueChallenges();
        else if (oldLockedStatus != newLockedStatus && newLockedStatus != null) hideChallenges();

        // Third, check if we should lock.
        if (oldLockedStatus != newLockedStatus && newLockedStatus == SecurityConstants.UNLOCKED)
        {
            lockFrame.unlock();
            previous20Results.clear();
            previous10Results.clear();
        }
        else if (oldLockedStatus != newLockedStatus && newLockedStatus != null) {
            lockFrame.lock();
            if (newLockedStatus == SecurityConstants.LOCKED) lockFrame.addPanel(lockPanel.getLockedPanel());
        }
    }

    /**
     * Implementation of listener to the ClientStateModel's analysis results queue. The queue contains updates to the
     * client's authentication, and the locker will decide whether a state change is necessary.
     */
    public void analysisResultQueueChanged(AnalysisResultPojo newResult, boolean isDeleteEvent) {
        if (isDeleteEvent) return;
        state.obtainAccessToStatus();
        try {

            // Check first if the result and model is present.
            if (newResult == null || state.getCurrentStatus() == null) return;

            previous20Results.add(newResult.getProbability());
            if (previous20Results.size()>20) previous20Results.take();
            previous10Results.add(newResult.getProbability());
            if (previous10Results.size()>10) previous10Results.take();
            double sum20 = previous20Results.stream().mapToDouble(f -> f.doubleValue()).sum();
            double average20 = sum20/20;

            long countLast10Below10Percent = previous10Results.stream().filter(f -> f<=0.1).count();

            if (previous20Results.size()<20) return; //this is the 20 key buffer

            // If newResult does not meet the threshold then issueChallenges.
            // TODO: How the threshold works is largely up to the analysis engine.
            if (newResult.getProbability() < 0.02||average20<0.15||countLast10Below10Percent>=5) {
                state.obtainAccessToStatus();
                try {
                    ClientStatusPojo currentStatus = state.getCurrentStatus();
                    if (currentStatus == null) {
                        // If for some reason it is null, there is a big problem, let another service handle it.
                        log.error("No model detected when analysis is being run.");
                        return;
                    }
                    controller.enqueueStatus(PojoHelper.createStatus(currentStatus, SecurityConstants.CHALLENGE));
                } finally {
                    state.releaseAccessToStatus();
                }
            }

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally {
            state.releaseAccessToStatus();
        }
    }

    /**
     * Issues the challenges based on accepted strategies and notify client state of the result.
     */
    private void issueChallenges() {
        // First clear the optionView and add it to the lock frame.
        optionView.clearOptions();
        lockFrame.addPanel(optionView.getChallengeOptionPanel());

        // Reset the number of attempts.
        remainingAttempts = MAX_CHALLENGE_ATTEMPTS;

        state.obtainAccessToStatus();
        try {
            IChallengeStrategy[] challengeStrategies =
                    PojoHelper.castToChallengeStrategy(strategies, state.getCurrentStatus().getProfile().getAcceptedChallengeStrategies());
            if (challengeStrategies != null) {
                // Warn if there aren't any challenge strategies.
                if (challengeStrategies.length == 0) log.warn("Administrator has not set up any ways to authenticate.");

                for (IChallengeStrategy strategy : challengeStrategies) {
                    // Reset every strategy's view.
                    strategyViewReset(strategy);

                    // Add each strategy as an option to the option view.
                    optionView.addOption(strategy.getServerRepresentation(), (ActionEvent aE) -> {
                        // If the strategy was clicked, then hide the option view and show the strategy specific view.
                        lockFrame.addPanel(strategyViewPairs.get(strategy).getChallengePanel());
                        lockFrame.removePanel(optionView.getChallengeOptionPanel());
                        ChallengePanelView view = strategyViewPairs.get(strategy);
                        view.drawFocus();
                    });
                }
            }
        } finally {
            state.releaseAccessToStatus();
        }
    }

    /**
     * Hides the challenges.
     */
    private void hideChallenges() {
        lockFrame.removeAllPanels();
    }

    /**
     * Reset the view associated with a strategy.
     *
     * @param strategy the strategy whose view is reset
     */
    private void strategyViewReset(IChallengeStrategy strategy) {
        ChallengePanelView view = strategyViewPairs.get(strategy);
        view.setEnableSend(true);
        view.setEnableResend(false);
        view.setEnableAlt(true);
        view.setEnableSubmit(false);
        view.setInformationText(strategy.getCustomInformationText());
        view.clearCode();
    }
}
