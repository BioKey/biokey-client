package com.biokey.client.services;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.biokey.client.constants.AppConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import com.biokey.client.models.response.TypingProfileContainerResponse;
import com.biokey.client.models.response.UserContainerResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private ClientStateController controller;
    private ClientStateModel clientState;
    private String queueUrl;
    private AmazonSQS sqs =
            AmazonSQSClientBuilder.standard()
                    .withClientConfiguration(new ClientConfiguration().withRetryPolicy(PredefinedRetryPolicies.NO_RETRY_POLICY))
                    .withRegion(Regions.US_EAST_2).build();
    private Timer timer = new Timer(true);
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ServerListenerService(ClientStateController controller, ClientStateModel c_state) {
        this.controller = controller;
        this.clientState = c_state;
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
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
            clientState.obtainAccessToStatus();
            queueUrl = clientState.getCurrentStatus().getProfile().getSqsEndpoint();
            timer.schedule(new DequeueTask(), AppConstants.SQS_LISTENER_PERIOD);
            clientState.releaseAccessToStatus();
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
                System.out.println("Getting messages...");
                ReceiveMessageRequest req = new ReceiveMessageRequest(queueUrl).withMessageAttributeNames("All");
                List<Message> messages = sqs.receiveMessage(req).getMessages();
                System.out.println("Size: " + messages.size());
                messages.forEach(message -> {
                    log(message);
                    process(message);
                });
            }
            catch (AmazonSQSException e){
                System.out.println("SQS Error");
                System.out.println(e);
            } finally {
                timer.schedule(new DequeueTask(), AppConstants.SQS_LISTENER_PERIOD);
            }
        }

        private void log (Message message) {
            System.out.println("Message read!\nBody:");
            System.out.print(message.getBody());
            System.out.println("\nAttributes:");
            message.getMessageAttributes().forEach((att, val) -> System.out.println(att + ": " + val.getStringValue()));
        }

        /**
         * Read the message information and update the state accordingly.
         *
         * @param message The most recent message retrieved from the SQS server.
         */
        private void process (Message message) {
            try {
                String changeType = message.getMessageAttributes().get("ChangeType").getStringValue();
                if (changeType.equals("TypingProfile")) {
                    System.out.println("Change detected to the TypingProfile!");

                    // Clean JSON
                    String dirtyMessage = message.getBody();
                    String cleanMessage = cleanJson(dirtyMessage);

                    // Read JSON, enqueue message
                    TypingProfileContainerResponse res = mapper.readValue(cleanMessage, TypingProfileContainerResponse.class);
                    clientState.obtainAccessToModel();
                    clientState.enqueueStatus(constructStatus(res));
                    clientState.releaseAccessToModel();
                }
                else if (changeType.equals("User")) {
                    System.out.println("Change detected to the User!");

                    // Clean JSON, enqueue message if necessary
                    String dirtyMessage = message.getBody();
                    String cleanMessage = cleanJson(dirtyMessage);

                    //Read JSON
                    UserContainerResponse res = mapper.readValue(cleanMessage, UserContainerResponse.class);
                    if (res.getChangeType().equals("LOGOUT")) {
                        clientState.obtainAccessToStatus();
                        clientState.enqueueStatus(lockStatus(clientState.getCurrentStatus()));
                        clientState.releaseAccessToStatus();
                    }
                    else System.out.println("Inconsequential change. Discarding message.");
                }
            }
            catch (NullPointerException e) {
                System.out.println(e);
                System.out.println("There was an error reading the message.");
            }
            catch (IOException e) {
                System.out.println(e);
                System.out.println("There was an error reading the message.");
            }

            // Delete the message
            String messageHandle = message.getReceiptHandle();
            sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageHandle));
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

        /**
         * Constructs a new status from a given server message.
         *
         * @param res  The message received from the server
         * @return     The new status to be enqueued
         */
        private ClientStatusPojo constructStatus (TypingProfileContainerResponse res) {
            SecurityConstants newLockState;
            if (res.getTypingProfile().isLocked()) newLockState = SecurityConstants.LOCKED;
            else newLockState = SecurityConstants.UNLOCKED;
            return new ClientStatusPojo(
                    new TypingProfilePojo(
                            res.getTypingProfile().get_id(),
                            res.getTypingProfile().getMachine(),
                            res.getTypingProfile().getUser(),
                            res.getTypingProfile().getTensorFlowModel(),
                            res.getTypingProfile().getThreshold(),
                            res.getTypingProfile().getChallengeStrategies(),
                            res.getTypingProfile().getEndpoint()
                    ),
                    clientState.getCurrentStatus().getAuthStatus(),
                    newLockState,
                    clientState.getCurrentStatus().getAccessToken(),
                    res.getPhoneNumber(),
                    res.getGoogleAuthKey(),
                    System.currentTimeMillis()
            );
        }

        /**
         * Locks the current state.
         *
         * @return  The current state with 'securityStatus' set to LOCKED.
         */
        private ClientStatusPojo lockStatus (ClientStatusPojo currentStatus) {
            //If the client is already locked, don't need to do anything
            if (clientState.getCurrentStatus().getSecurityStatus() == SecurityConstants.LOCKED) return clientState.getCurrentStatus();
            else
                return new ClientStatusPojo (
                        currentStatus.getProfile(),
                        currentStatus.getAuthStatus(),
                        SecurityConstants.LOCKED,
                        currentStatus.getAccessToken(),
                        currentStatus.getPhoneNumber(),
                        currentStatus.getGoogleAuthKey(),
                        System.currentTimeMillis()
                );
        }
    }
}
