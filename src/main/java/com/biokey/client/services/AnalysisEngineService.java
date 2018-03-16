package com.biokey.client.services;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.EngineConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.GaussianFeaturePojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.views.frames.FakeAnalysisFrameView;
import com.biokey.client.views.frames.TrayFrameView;
import com.biokey.client.views.panels.AnalysisResultTrayPanelView;
import com.google.common.collect.Collections2;
import com.google.common.collect.EvictingQueue;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.security.Key;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service that runs the analysis model and reports analysis results that represent the likelihood that the user's
 * typing matches their current profile.
 */
public class AnalysisEngineService implements ClientStateModel.IClientStatusListener, ClientStateModel.IClientKeyListener {

    @Data
    private class KeyDownEvent {
        private long downTime;
        private long seqNumber;
        private int key;

        public KeyDownEvent(int key, long downTime) {
            this.key = key;
            this.downTime = downTime;
            this.seqNumber = engineSeqNumber++;
        }
    }

    @Data
    private class KeySequence {
        private String sequence;
        private long duration;
        private long indexStart;
        private long indexEnd;
        private double score;

        public KeySequence(String sequence, long duration, long indexStart, long indexEnd, double score) {
            this.sequence = sequence;
            this.duration = duration;
            this.indexStart = indexStart;
            this.indexEnd = indexEnd;
            this.score = score;
            System.out.println(this);
        }

        public String toString() {
            return sequence + "( " + duration + "ms, " + indexStart + " - " + indexEnd + " ) | " + score;
        }
    }

    private final ClientStateController controller;
    private final ClientStateModel state;
    private final AnalysisResultTrayPanelView analysisResultTrayPanelView;

    // Buffer Variables
    private static long engineSeqNumber = 0;
    private Stack<KeyDownEvent> currentSequence;
    private HashMap<Integer, Stack<KeyDownEvent>> runningSequences;
    private ArrayList<KeySequence> completedSequences;

    private int sizeOfEvictingQueue = 100;
    private EvictingQueue<double []> rawQueue = EvictingQueue.create(sizeOfEvictingQueue);
    private EvictingQueue<double []> queue40 = EvictingQueue.create(sizeOfEvictingQueue);
    private EvictingQueue<double []> queue100 = EvictingQueue.create(sizeOfEvictingQueue);

    // TODO: delete once the fake is no longer needed.
    private FakeAnalysisFrameView frame = new FakeAnalysisFrameView();

    private boolean isRunning = false;

    @Autowired
    public AnalysisEngineService(ClientStateController controller, ClientStateModel state, TrayFrameView trayFrameView,
                                 AnalysisResultTrayPanelView analysisResultTrayPanelView) {
        this.controller = controller;
        this.state = state;
        this.analysisResultTrayPanelView = analysisResultTrayPanelView;

        currentSequence = new Stack<>();
        runningSequences = new HashMap<>();
        completedSequences = new ArrayList<>();

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

        /*
        state.obtainAccessToStatus();
        int featureSize = state.getCurrentStatus().getProfile().getModel().getGaussianProfile().keySet().size();
        state.releaseAccessToStatus();

        double [] fullZeroes = new double[featureSize];
        double [] fullPointFives = new double[featureSize];

        for (int i = 0; i < featureSize; i++) {
            fullPointFives [i] = 0.5;
            fullZeroes [i] = 0;
        }
        //initialize evicting queues
        for (int i = 0; i <sizeOfEvictingQueue ; i++) {

            rawQueue.add(fullZeroes);
            queue40.add(fullPointFives);
            queue100.add(fullPointFives);
        }
        */


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
    public void keystrokeQueueChanged(KeyStrokePojo e) {
        if (e.isKeyDown()) {
            KeyDownEvent lastKey = (currentSequence.isEmpty() ? null : currentSequence.peek());
            KeyDownEvent newKey = new KeyDownEvent(e.getKey(), e.getTimeStamp());

            if (lastKey != null && newKey.getDownTime() - lastKey.getDownTime() > EngineConstants.SEQ_THRESHOLD) {
                // If a new sequence should start
                currentSequence = new Stack<>();
            }

            currentSequence.add(newKey);
            runningSequences.put(newKey.key, (Stack<KeyDownEvent>)currentSequence.clone());
        }
        else {
            Stack<KeyDownEvent> sequencesToFinish = runningSequences.replace(e.getKey(), null);
            if (sequencesToFinish == null) return;

            String runningSequence = "";
            long finishTime = e.getTimeStamp();

            KeyDownEvent lastKey = (sequencesToFinish.isEmpty() ? null : sequencesToFinish.peek());
            if (lastKey == null) return;
            while(!sequencesToFinish.isEmpty()) {
                KeyDownEvent startKey = sequencesToFinish.pop();
                runningSequence = startKey.getKey() + (runningSequence.length() == 0 ? "" : "-" + runningSequence);
                long duration = finishTime - startKey.getDownTime();

                state.obtainAccessToStatus();
                GaussianFeaturePojo stats = state.getCurrentStatus().getProfile().getModel().getGaussianProfile().get(runningSequence);
                state.releaseAccessToStatus();
                if (stats != null) {
                    double mean = stats.getMean();
                    double stdev = stats.getStdev();
                    double score = Math.exp(-Math.pow(Math.log(duration)-mean, 2)/(2*Math.pow(stdev, 2)));
                    completedSequences.add(new KeySequence(runningSequence, duration, startKey.getSeqNumber(), lastKey.getSeqNumber(), score));
                }
            }
        }
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

        state.obtainAccessToStatus();
        HashMap<String, GaussianFeaturePojo> gaussianProfile = state.getCurrentStatus().getProfile().getModel().getGaussianProfile();
        state.releaseAccessToStatus();

        // String [] possibleSequences = (String []) gaussianProfile.keySet().toArray();
        List<String> features = new ArrayList<String>(gaussianProfile.keySet().size());
        features.addAll(gaussianProfile.keySet());
        Collections.sort(features);
        System.out.println(features.size());

        double [] individualFeatureVector = new double [features.size()];
        double [] frame40FeatureVector = new double [features.size()];
        double [] frame100FeatureVector = new double [features.size()];

        for (int i = 0; i < individualFeatureVector.length; i++) {
            individualFeatureVector[i] = 0;
            frame40FeatureVector [i]=0.5;
            frame100FeatureVector[i]=0.5;
        }

        Map<String, Double> frame100 = completedSequences
                .stream()
                .filter(s -> engineSeqNumber - s.getIndexEnd() <= 100)
                .collect(
                        Collectors.groupingBy(
                                KeySequence::getSequence,
                                Collectors.averagingDouble(KeySequence::getScore)));

        Map<String, Double> frame40 = completedSequences
                .stream()
                .filter(s -> engineSeqNumber - s.getIndexEnd() <= 40)
                .collect(
                        Collectors.groupingBy(
                                KeySequence::getSequence,
                                Collectors.averagingDouble(KeySequence::getScore)));

        Map<String, Double> frameRaw = completedSequences
                .stream()
                .filter(s -> engineSeqNumber-1  == s.getIndexEnd())
                .collect(Collectors.toMap(KeySequence::getSequence, s -> Math.log(s.getDuration())));


        frameRaw.forEach((seq, score) -> {
            individualFeatureVector[features.indexOf(seq)] = score;
        });

        frame40.forEach((seq,score)->{
            frame40FeatureVector[features.indexOf(seq)]=score;
        });

        frame100.forEach((seq,score)->{
            frame100FeatureVector[features.indexOf(seq)]=score;
        });


        rawQueue.add(individualFeatureVector);
        queue40.add(frame40FeatureVector);
        queue100.add(frame100FeatureVector);

        // System.out.println(frame100);
        /*
        for (int i =0; i <individualFeatureVector.length;i++)
        {
            String sequence = possibleSequences[i];
            sequencesInLookback
        }
        */
        frame.informationLabel.setText("analyze() was called.");


    }
}
