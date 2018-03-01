package com.biokey.client.services;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.biokey.client.constants.AppConstants;
import com.biokey.client.constants.AuthConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.helpers.PojoHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.response.TypingProfileContainerResponse;
import com.biokey.client.models.response.UserContainerResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Service that listens for messages enqueued by the server and responds by registering the changes to the client state.
 */
public class ServerListenerService implements ClientStateModel.IClientStatusListener {

    private static Logger log = Logger.getLogger(ChallengeService.class);

    private final ClientStateController controller;
    private final ClientStateModel clientState;
    private final AmazonSQS sqs =
            AmazonSQSClientBuilder.standard()
                    .withClientConfiguration(new ClientConfiguration().withRetryPolicy(PredefinedRetryPolicies.NO_RETRY_POLICY))
                    .withRegion(Regions.US_EAST_2).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private Timer timer = new Timer(true);
    private boolean isStarted = false;

    @Autowired
    public ServerListenerService(ClientStateController controller, ClientStateModel clientStateModel) {
        this.controller = controller;
        this.clientState = clientStateModel;
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }

    /**
     * Implementation of listener to the ClientStateModel's status.
     * The status will contain a flag for whether the service should be running.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {
        if (newStatus != null) start();
        else stop();
    }

    /**
     * Start running the server listener.
     */
    private void start() {
        if (isStarted) return;
        clientState.obtainAccessToStatus();
        try {
            if (clientState.getCurrentStatus() == null) {
                // If for some reason it is null, there is a big problem, let another service handle it.
                log.error("No model detected when starting server listener.");
                return;
            }
            timer.schedule(new DequeueTask(clientState.getCurrentStatus().getProfile().getSqsEndpoint()), AppConstants.SQS_LISTENER_PERIOD);
            isStarted = true;
        } finally {
            clientState.releaseAccessToStatus();
        }
    }

    /**
     * Stop running the server listener.
     */
    private void stop() {
        timer.cancel();
        timer = new Timer(true);
        isStarted = false;
    }

    /**
     * Read any available server messages and register changes to the client state. Scheduled as a TimerTask.
     */
    private class DequeueTask extends TimerTask {

        private final String queueUrl;

        public DequeueTask(String queueUrl) {
            this.queueUrl = queueUrl;
        }

        public void run() {
            try {
                ReceiveMessageRequest req = new ReceiveMessageRequest(queueUrl).withMessageAttributeNames("All");
                List<Message> messages = sqs.receiveMessage(req).getMessages();
                messages.forEach(message -> {
                    log(message);
                    process(message);
                });
            }
            catch (AmazonSQSException e){
                log.warn("SQS Error", e);
            } finally {
                timer.schedule(new DequeueTask(queueUrl), AppConstants.SQS_LISTENER_PERIOD);
            }
        }

        private void log(Message message) {
            log.debug("Message read!\nBody: " + message.getBody() + "\nAttributes:\n");
            message.getMessageAttributes().forEach((att, val) -> System.out.println(att + ": " + val.getStringValue()));
        }

        /**
         * Read the message information and update the state accordingly.
         *
         * @param message The most recent message retrieved from the SQS server.
         */
        private void process(Message message) {
            clientState.obtainAccessToStatus();
            ClientStatusPojo currentStatus = clientState.getCurrentStatus();
            if (currentStatus == null) {
                // If for some reason it is null, there is a big problem, let another service handle it.
                log.error("No model detected when starting server listener.");
                return;
            }
            try {
                String changeType = message.getMessageAttributes().get("ChangeType").getStringValue();

                // If any part of the message is null, send directly to catch block.
                if (changeType.equals("TypingProfile")) {
                    String cleanMessage = cleanJson(message.getBody());

                    // Read JSON, enqueue status.
                    TypingProfileContainerResponse res = mapper.readValue(cleanMessage, TypingProfileContainerResponse.class);
                    controller.enqueueStatus(
                            PojoHelper.castToClientStatus(res, currentStatus.getAccessToken(), currentStatus.getAuthStatus()));
                }
                else if (changeType.equals("User")) {
                    String cleanMessage = cleanJson(message.getBody());

                    // Read JSON, enqueue status.
                    UserContainerResponse res = mapper.readValue(cleanMessage, UserContainerResponse.class);
                    ClientStatusPojo newStatus = PojoHelper.createStatus(currentStatus, res.getPhoneNumber(), res.getGoogleAuthKey());
                    if (res.getChangeType().equals("LOGOUT")) {
                        newStatus = PojoHelper.createStatus(newStatus, AuthConstants.UNAUTHENTICATED);
                    }
                    controller.enqueueStatus(newStatus);
                }
            }
            catch (NullPointerException | IOException e) {
                log.warn("There was an error reading the message.", e);
            } finally {
                clientState.releaseAccessToStatus();
                // Delete the message.
                String messageHandle = message.getReceiptHandle();
                sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageHandle));
            }
        }

        /**
         * Converts JSON to a parse-able format.
         *
         * @param json  The un-escaped JSON
         * @return      The escaped JSON
         */
        private String cleanJson (String json) {
            json = json.replace("\\", "");
            json = json.replace("”", "\"");
            json = json.replace("\"{", "{");
            json = json.replace("}\"", "}");
            return json;
        }
    }
}
