package com.biokey.client.controllers.challenges;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.constants.SyncStatusConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.views.frames.GoogleAuthQRFrameView;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class GoogleAuthStrategy implements IChallengeStrategy, Serializable {

    private static final long serialVersionUID = 1000;
    private static Logger log = Logger.getLogger(GoogleAuthStrategy.class);

    private ClientStateModel state;
    private ClientStateController controller;
    private ServerRequestExecutorHelper serverRequestExecutorHelper;
    private GoogleAuthQRFrameView qrFrameView;

    private SyncStatusConstants initialized = SyncStatusConstants.UNSYNCED;
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @Autowired
    public GoogleAuthStrategy(ClientStateModel state, ClientStateController controller,
                              ServerRequestExecutorHelper serverRequestExecutorHelper, GoogleAuthQRFrameView qrFrameView) {
        this.state = state;
        this.controller = controller;
        this.serverRequestExecutorHelper = serverRequestExecutorHelper;
        this.qrFrameView = qrFrameView;
    }

    public void init() {
        state.obtainAccessToStatus();
        try {
            // If the current status is not set then there is no user to attach to.
            if (state.getCurrentStatus() == null) return;
            // If initialized or gKey already set then return.
            if (initialized != SyncStatusConstants.UNSYNCED) return;
            // If there is a key, set initialized to true and return.
            if (!state.getCurrentStatus().getGoogleAuthKey().equals("")) {
                initialized = SyncStatusConstants.INSYNC;
                return;
            }

            GoogleAuthenticatorKey gKey = gAuth.createCredentials();
            String accountName = "Machine@" + state.getCurrentStatus().getProfile().getMachineId();
            initialized = SyncStatusConstants.SYNCING;

            System.out.println(GoogleAuthenticatorQRGenerator.getOtpAuthURL(AppConstants.APP_NAME, accountName, gKey));

            // Get the QR code then display it.
            serverRequestExecutorHelper.submitGetRequest(
                    URLDecoder.decode(GoogleAuthenticatorQRGenerator.getOtpAuthURL(AppConstants.APP_NAME, accountName, gKey), "UTF-8"),
                    new HttpHeaders(), byte[].class, (ResponseEntity<byte[]> response) -> {
                        // Display QR code. Once it is displayed, user has seen it and the strategy can be considered initialized.
                        initialized = SyncStatusConstants.INSYNC;
                        qrFrameView.displayImage(
                                (response == null) ? null : response.getBody(),
                                "Could not generate QR Code. Use following code in 2FA app: " + gKey.getKey());
                        controller.setGoogleAuthKey(gKey.getKey());
                    });
        } catch (UnsupportedEncodingException e) {
            log.error("UTF-8 encoding is not supported by URLEncoder.", e);
        } finally {
            state.releaseAccessToStatus();
        }
    }

    public boolean isInitialized() {
        return initialized == SyncStatusConstants.INSYNC;
    }

    public boolean issueChallenge() {
        // Nothing to do.
        return true;
    }

    public boolean checkChallenge(String attempt) {
        state.obtainAccessToStatus();
        try {
            return initialized == SyncStatusConstants.INSYNC && // Must be initialized.
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

    public String getCustomInformationText() {
        return "If you lost your 2FA device, please contact your administrator.";
    }
}
