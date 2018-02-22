package com.biokey.client.services;

import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.model.Message;
import com.biokey.client.constants.AppConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import org.springframework.beans.factory.annotation.Autowired;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Service that listens for messages enqueued by the server and responds by registering the changes to the client state.
 */
public class ServerListenerService implements ClientStateModel.IClientStatusListener {

    private ClientStateController controller;
    private ClientStateModel state;
    private String queueUrl;
    private AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion(Regions.US_EAST_2).build();
    private Timer timer = new Timer(true);

    @Autowired
    public ServerListenerService(ClientStateController controller, ClientStateModel state) {
        this.controller = controller;
        this.state = state;
    }

    /**
     * Implementation of listener to the ClientStateModel's status.
     * The status will contain a flag for whether the service should be running.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {
        // TODO: Implement status changed
        start();
    }

    /**
     * Start running the server listener.
     *
     * @return true if server listener successfully started
     */
    private boolean start() {
        try {
            state.obtainAccessToStatus();
            queueUrl = state.getCurrentStatus().getProfile().getSqsEndpoint();
            timer.scheduleAtFixedRate(new DequeueTask(), 0, AppConstants.SQS_LISTENER_PERIOD);
            state.releaseAccessToStatus();
            return true;
        } catch (IllegalStateException e) {
            System.out.println("Timer error");
            System.out.println(e);
            return false;
        }
    }

    /**
     * Stop running the server listener.
     *
     * @return true if server listener successfully stopped
     */
    private boolean stop() {
        timer.cancel();
        return true;
    }

    /**
     * Read any available server messages and register changes to the client state.
     * Scheduled as a TimerTask.
     */
    private class DequeueTask extends TimerTask {

        public void run() {
            try {
                List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
                messages.forEach(message -> {
                    System.out.println("Message read!");
                    System.out.println(message);
                    process(message);
                });
            }
            catch (AmazonSQSException e){
                System.out.println("SQS Error");
                System.out.println(e);
            }
            return;
        }

        private void process (Message message) {
            // TODO: message processing logic
        }
    }
}
