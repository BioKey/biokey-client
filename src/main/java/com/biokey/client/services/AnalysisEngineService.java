package com.biokey.client.services;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.views.frames.FakeAnalysisFrameView;
import com.biokey.client.views.frames.TrayFrameView;
import com.biokey.client.views.panels.AnalysisResultTrayPanelView;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Service that runs the analysis model and reports analysis results that represent the likelihood that the user's
 * typing matches their current profile.
 */
public class AnalysisEngineService implements ClientStateModel.IClientStatusListener, ClientStateModel.IClientKeyListener {

    private final ClientStateController controller;
    private final AnalysisResultTrayPanelView analysisResultTrayPanelView;

    // TODO: delete once the fake is no longer needed.
    private FakeAnalysisFrameView frame = new FakeAnalysisFrameView();

    private boolean isRunning = false;

    @Autowired
    public AnalysisEngineService(ClientStateController controller, TrayFrameView trayFrameView,
                                 AnalysisResultTrayPanelView analysisResultTrayPanelView) {
        this.controller = controller;
        this.analysisResultTrayPanelView = analysisResultTrayPanelView;

        trayFrameView.addPanel(analysisResultTrayPanelView.getAnalysisResultTrayPanel());

        // TODO: delete once the fake is no longer needed.
        frame.enqueueButton.addActionListener((ActionEvent aE) -> {
            try {
                float newAnalysisResult = Float.parseFloat(frame.analysisResultTextField.getText());
                controller.enqueueAnalysisResult(new AnalysisResultPojo(System.currentTimeMillis(), newAnalysisResult));
                analysisResultTrayPanelView.setAnalysisResultText(newAnalysisResult);
            } catch (Exception e) {
                frame.informationLabel.setText("Invalid analysis result.");
            }
        });
        frame.setContentPane(frame.fakeAnalysisPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
    }

    /**
     * Implementation of listener to the ClientStateModel's status. The status will contain the details
     * on the analysis model to run through the typing profile.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {
        if (newStatus != null && newStatus.getAuthStatus() == AuthConstants.AUTHENTICATED) start();
        else stop();
    }

    /**
     * Implementation of listener to the ClientStateModel's keystroke queues. New keys need to be fed into the model.
     */
    public void keystrokeQueueChanged(KeyStrokePojo newKey) {
        analyze();
    }

    /**
     * Start running the analysis engine.
     */
    private void start() {
        isRunning = true;
        // TODO: delete once the fake is no longer needed.
        frame.setVisible(true);
    }

    /**
     * Stop running the analysis engine.
     */
    private void stop() {
        isRunning = false;
        // TODO: delete once the fake is no longer needed.
        frame.setVisible(false);
    }

    /**
     * Use the current keystroke data to generate the likelihood that the user's typing matches their current profile.
     */
    private void analyze() {
        if (!isRunning) return;
        // TODO: delete once the fake is no longer needed.
        frame.informationLabel.setText("analyze() was called.");
    }
}
