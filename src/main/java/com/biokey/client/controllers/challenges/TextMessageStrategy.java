package com.biokey.client.controllers.challenges;

import java.io.Serializable;
import java.security.SecureRandom;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.Setter;

public class TextMessageStrategy implements IChallengeStrategy, Serializable {

    private static final long serialVersionUID = 1001;

    private static final String ACCOUNT_SID = "AC6135115ec55c24ab759c1ae658b2fdfb";
    private static final String AUTH_TOKEN = "79f75626c0f84962e3dcf81b85caba52";

    private String password;
    @Override
    public boolean performChallenges(String challenge) {
        return challenge.equals(password);
    }

    public void sendMessage ()
    {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        password = generateMessage();
        Message.creator(new PhoneNumber("+16476688628"), new PhoneNumber("+16473602026"),
                password).create();
    }

    private String generateMessage ()
    {
        SecureRandom random = new SecureRandom();
        return Integer.toString(random.nextInt(1000000));

    }

}
