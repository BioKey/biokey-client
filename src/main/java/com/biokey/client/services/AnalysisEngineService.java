package com.biokey.client.services;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.EngineConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.*;
import com.biokey.client.views.frames.FakeAnalysisFrameView;
import com.biokey.client.views.frames.TrayFrameView;
import com.biokey.client.views.panels.AnalysisResultTrayPanelView;

import com.google.common.collect.Collections2;
import com.google.common.collect.EvictingQueue;

import com.google.common.collect.Queues;
import com.google.common.io.ByteStreams;

import com.sun.xml.internal.ws.api.pipe.Engine;
import lombok.Data;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ActionEvent;

import java.security.Key;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import java.io.*;
import java.nio.Buffer;



/**
 * Service that runs the analysis model and reports analysis results that represent the likelihood that the user's
 * typing matches their current profile.
 */
public class AnalysisEngineService implements ClientStateModel.IClientStatusListener, ClientStateModel.IClientKeyListener {

    private static Logger log = Logger.getLogger(AnalysisEngineService.class);

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
            // System.out.println(this);
        }

        public String toString() {
            return sequence + "( " + duration + "ms, " + indexStart + " - " + indexEnd + " ) | " + score;
        }
    }

    private class KerasModel {
        private BufferedReader in;
        private BufferedWriter out;
        private boolean initialized = false;
        private Process p;

        public KerasModel() {
            try {
                ProcessBuilder pb = new ProcessBuilder("python", "model.py");
                p = pb.start();
                in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

                BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                Runnable logErrors = () -> {
                    String error = null;
                    try {
                        while((error = err.readLine()) != null) {
                            log.error(error);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                (new Thread(logErrors)).start();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        public boolean init(String payload) {
            try {
                out.write("init: " + payload);
                out.newLine();
                out.flush();
                String result = in.readLine();
                log.debug(result);
                initialized = result.equals("INIT: true");
                return initialized;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return false;

        }

        public double predict(String payload) {
            if (!initialized) {
                log.error("Model must be initialized before predictions can be made");
                return -1;
            }
            try {
                out.write("predict: " + payload);
                out.newLine();
                out.flush();
                String response = in.readLine();
                log.debug(response);
                double result = Double.parseDouble(response.replaceFirst("PREDICT: ", ""));
                return result;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return -1;

        }

        public void kill() {
            if (p != null) p.destroy();
        }

        public boolean isRunning() {
            return (p != null && p.isAlive());
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
    private int featureSize;
    private Queue<List<Double>> rawQueue;
    private Queue<List<Double>> queue40;
    private Queue<List<Double>> queue100;

    private KerasModel model;

    private JSONObject loggedInputs = new JSONObject();
    private final Lock lock = new ReentrantLock(true);

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

        frame.setContentPane(frame.fakeAnalysisPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();


    }

    /**
     * Implementation of listener to the ClientStateModel's status. The status will contain the details
     * on the analysis model to run through the typing profile.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus, boolean isDeleteEvent) {
        if (isDeleteEvent) return;
        if (newStatus != null && newStatus.getAuthStatus() == AuthConstants.AUTHENTICATED &&
                newStatus.getProfile() != null && newStatus.getProfile().getModel() != null)  {
            start();
        }
        else stop();
    }

    /**
     * Implementation of listener to the ClientStateModel's keystroke queues. New keys need to be fed into the model.
     */
    public void keystrokeQueueChanged(KeyStrokePojo e, boolean isDeleteEvent) {
        if (isDeleteEvent) return;
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
            analyze();
        }
    }

    /**
     * Start running the analysis engine.
     */
    private void start() {
        if (isRunning) return;

        state.obtainAccessToStatus();
        featureSize = state.getCurrentStatus().getProfile().getModel().getGaussianProfile().keySet().size();
        EngineModelPojo modelDef = state.getCurrentStatus().getProfile().getModel();
        state.releaseAccessToStatus();

        rawQueue = Queues.synchronizedQueue(EvictingQueue.create(sizeOfEvictingQueue));
        queue40 = Queues.synchronizedQueue(EvictingQueue.create(sizeOfEvictingQueue));
        queue100 = Queues.synchronizedQueue(EvictingQueue.create(sizeOfEvictingQueue));


        List<Double> zeros = new ArrayList<Double>();
        List<Double> pointFives = new ArrayList<Double>();

        for (int i = 0; i < featureSize; i++) {
            zeros.add(0.0);
            pointFives.add(0.5);
        }
        //initialize evicting queues
        for (int i = 0; i <sizeOfEvictingQueue ; i++) {
            rawQueue.add(zeros);
            queue40.add(pointFives);
            queue100.add(pointFives);
        }

        if (model != null && model.isRunning()) model.kill();
        model = new KerasModel();

        try {
            JSONParser parser = new JSONParser();
            JSONObject payload = (JSONObject)parser.parse(new FileReader("/Users/connorgiles/Downloads/ensemble-c-2.json"));

                /*
            payload.put("model", modelDef.getModel());
            payload.put("weight", modelDef.getWeights());
            */
            boolean initResult = model.init(payload.toJSONString());
            System.out.println(initResult);
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        isRunning = true;
        // TODO: delete once the fake is no longer needed.
        frame.setVisible(true);
    }

    /**
     * Stop running the analysis engine.
     */
    private void stop() {
        if (model != null && model.isRunning()) {
            model.kill();
            model = null;
        }
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
        try {
            lock.lock();
            state.obtainAccessToStatus();
            HashMap<String, GaussianFeaturePojo> gaussianProfile = state.getCurrentStatus().getProfile().getModel().getGaussianProfile();
            state.releaseAccessToStatus();

            List<Double> individualFeatureVector = new ArrayList<>();
            List<Double> frame40FeatureVector = new ArrayList<>();
            List<Double> frame100FeatureVector = new ArrayList<>();


            for (int i = 0; i < featureSize; i++) {
                individualFeatureVector.add(0.0);
                frame40FeatureVector.add(0.5);
                frame100FeatureVector.add(0.5);
            }

            Map<String, Double> frame100 = completedSequences
                    .stream()
                    .filter(s -> engineSeqNumber - s.getIndexStart() <= 100)
                    .collect(
                            Collectors.groupingBy(
                                    KeySequence::getSequence,
                                    Collectors.averagingDouble(KeySequence::getScore)));

            Map<String, Double> frame40 = completedSequences
                    .stream()
                    .filter(s -> engineSeqNumber - s.getIndexStart() <= 40)
                    .collect(
                            Collectors.groupingBy(
                                    KeySequence::getSequence,
                                    Collectors.averagingDouble(KeySequence::getScore)));

            Map<String, Double> frameRaw = completedSequences
                    .stream()
                    .filter(s -> engineSeqNumber-1  == s.getIndexEnd())
                    .filter(s -> s.getDuration() > 0)
                    .collect(Collectors.toMap(KeySequence::getSequence, s -> Math.log(s.getDuration())));

            frameRaw.forEach((seq, score) -> {
                individualFeatureVector.set(gaussianProfile.get(seq).getIndex(), score);
            });

            frame40.forEach((seq,score)->{
                frame40FeatureVector.set(gaussianProfile.get(seq).getIndex(), score);
            });

            frame100.forEach((seq,score)->{
                frame100FeatureVector.set(gaussianProfile.get(seq).getIndex(), score);
            });


            rawQueue.add(individualFeatureVector);
            queue40.add(frame40FeatureVector);
            queue100.add(frame100FeatureVector);


            JSONObject inputs = new JSONObject();
            inputs.put("x_raw", rawQueue);
            inputs.put("x_40", queue40);
            inputs.put("x_100", queue100);


            if (model != null) {

                if (engineSeqNumber == 300) {
                    double pred = model.predict(inputs.toJSONString());
                    /*
                    try {
                        BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/connorgiles/Desktop/test_frame.json"));
                        bw.write(inputs.toJSONString());
                        bw.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    */
                }

            }

        }
        finally {
            lock.unlock();
        }
        frame.informationLabel.setText("analyze() was called.");


    }
}
