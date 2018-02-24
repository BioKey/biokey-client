package com.biokey.client.controllers.challenges;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.controllers.ClientStateController;
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
    private ClientStateController controller;
    private ServerRequestExecutorHelper serverRequestExecutorHelper;
    private GoogleAuthFrameView view;

    @Getter private boolean initialized = false;
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @Autowired
    public GoogleAuthStrategy(ClientStateModel state, ClientStateController controller,
                              ServerRequestExecutorHelper serverRequestExecutorHelper, GoogleAuthFrameView view) {
        this.state = state;
        this.controller = controller;
        this.serverRequestExecutorHelper = serverRequestExecutorHelper;
        this.view = view;
    }

    public void init() {
        state.obtainAccessToStatus();
        try {
            // If the current status is not set then there is no user to attach to.
            if (state.getCurrentStatus() == null) return;
            // If initialized or gKey already set then return.
            if (initialized || !state.getCurrentStatus().getGoogleAuthKey().equals("")) return;

            GoogleAuthenticatorKey gKey = gAuth.createCredentials();
            String accountName = "Machine@" + state.getCurrentStatus().getProfile().getMachineId();

            // Get the QR code then display it.
            serverRequestExecutorHelper.submitGetRequest(
                    GoogleAuthenticatorQRGenerator.getOtpAuthURL(AppConstants.APP_NAME, accountName, gKey),
                    new HttpHeaders(), byte[].class, (ResponseEntity<byte[]> response) -> {
                        // Display QR code. Once it is displayed, user has seen it and the strategy can be considered initialized.
                        initialized = true;
                        view.displayImage(
                                (response == null) ? null : response.getBody(),
                                "Could not generate QR Code. Use following code in 2FA app: " + gKey.getKey());
                        controller.setGoogleAuthKey(gKey.getKey());
                    });
        } finally {
            state.releaseAccessToStatus();
        }
    }

    public boolean issueChallenge() {
        // Nothing to do.
        return true;
    }

    public boolean checkChallenge(String attempt) {
        state.obtainAccessToStatus();
        try {
            return initialized && // Must be initialized.
                    state.getCurrentStatus() != null && // Must have current status.
                    !state.getCurrentStatus().getGoogleAuthKey().equals("") && // Must have non-empty key,
                    gAuth.authorize(state.getCurrentStatus().getGoogleAuthKey(), Integer.parseInt(attempt)); // Attempt must match.
        } catch (NumberFormatException e) {
            return false;
        } finally {
            state.releaseAccessToStatus();
        }
    }

    public String getServerRepresentation() {
        return "GoogleAuth";
    }
}
