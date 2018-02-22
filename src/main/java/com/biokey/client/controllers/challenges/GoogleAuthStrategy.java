package com.biokey.client.controllers.challenges;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.views.frames.GoogleAuthFrameView;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

public class GoogleAuthStrategy implements IChallengeStrategy, Serializable {

    private static final long serialVersionUID = 1000;

    private ClientStateModel state;
    private ServerRequestExecutorHelper serverRequestExecutorHelper;
    private GoogleAuthFrameView view;

    @Getter private boolean initialized = false;
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private GoogleAuthenticatorKey gKey;

    @Autowired
    public GoogleAuthStrategy(ClientStateModel state, ServerRequestExecutorHelper serverRequestExecutorHelper, GoogleAuthFrameView view) {
        this.state = state;
        this.serverRequestExecutorHelper = serverRequestExecutorHelper;
        this.view = view;
    }

    public void init() {
        if (initialized) return;

        gKey = gAuth.createCredentials();
        state.setGKey(gKey.getKey());

        String accountName;
        state.obtainAccessToStatus();
        try {
            accountName = "Machine@" + state.getCurrentStatus().getProfile().getMachineId();
        } catch (Exception e) {
            accountName = AppConstants.APP_NAME;
        } finally {
            state.releaseAccessToStatus();
        }

        System.out.println(GoogleAuthenticatorQRGenerator.getOtpAuthURL(AppConstants.APP_NAME, accountName, gKey));

        // Get the QR code then display it.
        serverRequestExecutorHelper.submitGetRequest(
                GoogleAuthenticatorQRGenerator.getOtpAuthURL(AppConstants.APP_NAME, accountName, gKey),
                new HttpHeaders(), byte[].class, (ResponseEntity<byte[]> response) -> {
                    // Display QR code. Once it is displayed, user has seen it and the strategy can be considered initialized.
                    view.displayImage(
                        (response == null) ? null : response.getBody(),
                        "Could not generate QR Code. Use following code in 2FA app: " + gKey.getKey());
                    initialized = true;
                });
    }

    public boolean issueChallenge() {
        // Nothing to do.
        return true;
    }

    public boolean checkChallenge(String attempt) {
        try {
            return gAuth.authorize(state.getGKey(), Integer.parseInt(attempt));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getServerRepresentation() {
        return "GoogleAuth";
    }
}
