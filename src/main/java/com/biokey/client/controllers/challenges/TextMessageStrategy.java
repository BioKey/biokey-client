package com.biokey.client.controllers.challenges;

import java.io.Serializable;
import java.security.SecureRandom;

import com.biokey.client.constants.Credentials;
import com.biokey.client.models.ClientStateModel;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class TextMessageStrategy implements IChallengeStrategy, Serializable {

    private static Logger log = Logger.getLogger(TextMessageStrategy.class);
    private static final long serialVersionUID = 1001;

    @Getter private boolean initialized = false;
    private ClientStateModel state;
    @Getter private String challenge;

    @Autowired
    public TextMessageStrategy(ClientStateModel state) {
        this.state = state;
    }

    public void init() {
        if (initialized) return;
        Twilio.init(Credentials.TWILIO_ACCOUNT_SID, Credentials.TWILIO_AUTH_TOKEN);
        initialized = true;
    }

    public boolean issueChallenge() {
        if (!initialized) return false;

        // Generate the password
        SecureRandom random = new SecureRandom();
        challenge = Integer.toString(random.nextInt(1000000));

        // Obtain access to status to get the phone number.
        state.obtainAccessToStatus();
        try {
            if (state.getCurrentStatus() == null) {
                log.error("In challenge while current status does not exist.");
                return false;
            }
            // Send password to the user's phone number.
            Message.creator(new PhoneNumber(state.getCurrentStatus().getPhoneNumber()), new PhoneNumber(Credentials.TWILIO_FROM_PHONE_NUMBER), challenge).create();
        } catch(Exception e) {
            log.error("Twilio failed to send message. Perhaps TO or FROM phone number is incorrect.", e);
            return false;
        } finally {
            state.releaseAccessToStatus();
        }

        return true;
    }

    public boolean checkChallenge(String attempt) {
        if (!initialized) return false;
        boolean passed = (challenge != null) && challenge.equals(attempt);
        challenge = null;
        return passed;
    }

    public String getServerRepresentation() {
        return "TextMessage";
    }

    public String getCustomInformationText() {
        state.obtainAccessToStatus();
        try {
            if (state.getCurrentStatus() == null)  return "No Phone Number on record.";
            else return "Phone Number on record is: " + state.getCurrentStatus().getPhoneNumber() +
                        ". If this is incorrect please contact your administrator.";
        } finally {
            state.releaseAccessToStatus();
        }
    }
}
