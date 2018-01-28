package com.biokey.client.controllers.challenges;

import com.biokey.client.views.GoogleAuthChallengeView;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.Setter;

import javax.swing.*;
import java.awt.font.NumericShaper;

public class GoogleAuthStrategy implements IChallengeStrategy{

    @Override
    public boolean performChallenges(String challenge) {
        int password;
        boolean isValid;
        try {
            password = Integer.parseInt(challenge);
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            isValid = gAuth.authorize("633X7JWPCWSTJC53", password);
        } catch (NumberFormatException nfe) {
            isValid = false;
        }
        return isValid;
    }

    /*public static void main (String [] args)
    {
        new GoogleAuthStrategy();*/

        /* one time run at start for user to generate code
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        System.out.println (key.getKey()); // need to store this somewhere for each user since it is used for reauth
        //633X7JWPCWSTJC53 is the current key on my phone
        */

        // for testing purposes
        /*

        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        int code = gAuth.getTotpPassword("633X7JWPCWSTJC53");
        System.out.println(code);
        */

        /*
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean isCodeValid = gAuth.authorize()
        */

        //trying to get it nicely as a scannable QR code but not really sure what this whole accountName thing is
        //System.out.println(GoogleAuthenticatorQRGenerator.getOtpAuthURL(null,"Josh",key));

  //  }
}
