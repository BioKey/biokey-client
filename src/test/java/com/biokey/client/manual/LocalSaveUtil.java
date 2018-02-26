package com.biokey.client.manual;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.helpers.PojoHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import com.biokey.client.services.ClientInitService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class LocalSaveUtil extends JFrame {

    private Preferences prefs = Preferences.userRoot().node(ClientInitService.class.getName());

    private JTextField idTextField;
    private JTextField machineIdTextField;
    private JTextField userIdTextField;
    private JTextField modelTextField;
    private JTextField thresholdTextField;
    private JTextField challengesTextField;
    private JTextField sqsTextField;
    private JTextField authStatusTextField;
    private JTextField securityStatusTextField;
    private JTextField accessTokenTextField;
    private JTextField phoneNumberTextField;
    private JTextField googleAuthKeyTextField;
    private JTextField timestampTextField;
    private JTextField syncStatusTextField;
    private JButton saveButton;
    private JButton retrieveButton;
    private JButton clearButton;
    private JTextArea informationTextArea;
    private JPanel saveUtilPanel;
    private JTextArea jsonTextArea;


    private LocalSaveUtil() {
        // Retrieve button reads from memory and puts the result into the text fields.
        retrieveButton.addActionListener((ActionEvent aE) -> onRetrieve());

        // Save button reads from text fields and tries to save as current status.
        saveButton.addActionListener((ActionEvent aE) -> onSave());

        clearButton.addActionListener((ActionEvent aE) -> onClear());
    }

    private void onRetrieve() {
        informationTextArea.setText("");
        ClientStateModel fromMemory;
        try {
            fromMemory = ClientInitService.retrieveFromPreferences();
        } catch (Exception e) {
            informationTextArea.setText("Could not retrieve from Preferences. Have you logged in before?\n" + e.toString());
            clearTextFields();
            return;
        }

        fromMemory.obtainAccessToModel();

        try { jsonTextArea.setText(new ObjectMapper().writeValueAsString(fromMemory)); }
        catch (Exception e) { jsonTextArea.setText(""); }

        try { idTextField.setText(fromMemory.getCurrentStatus().getProfile().getId()); }
        catch (Exception e) { idTextField.setText(""); }

        try { machineIdTextField.setText(fromMemory.getCurrentStatus().getProfile().getMachineId()); }
        catch (Exception e) { machineIdTextField.setText(""); }

        try { userIdTextField.setText(fromMemory.getCurrentStatus().getProfile().getUserId()); }
        catch (Exception e) { userIdTextField.setText(""); }

        try { modelTextField.setText(fromMemory.getCurrentStatus().getProfile().getModel()); }
        catch (Exception e) { modelTextField.setText(""); }

        try {
            String thresholdString = Arrays.toString(fromMemory.getCurrentStatus().getProfile().getThreshold());
            // Make sure to remove the [] from string representation.
            if (thresholdString.length() <= 2) thresholdTextField.setText("");
            else thresholdTextField.setText(thresholdString.substring(1, thresholdString.length() - 1)); }
        catch (Exception e) { thresholdTextField.setText(""); }

        try {
            StringBuilder sb = new StringBuilder();
            for (String challenge : fromMemory.getCurrentStatus().getProfile().getAcceptedChallengeStrategies()) {
                sb.append(challenge).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length()); // Delete the last comma.
            challengesTextField.setText(sb.toString()); }
        catch (Exception e) { challengesTextField.setText(""); }

        try { sqsTextField.setText(fromMemory.getCurrentStatus().getProfile().getSqsEndpoint()); }
        catch (Exception e) { sqsTextField.setText(""); }

        try { authStatusTextField.setText(fromMemory.getCurrentStatus().getAuthStatus().toString()); }
        catch (Exception e) { authStatusTextField.setText(""); }

        try { securityStatusTextField.setText(fromMemory.getCurrentStatus().getSecurityStatus().toString()); }
        catch (Exception e) { securityStatusTextField.setText(""); }

        try { accessTokenTextField.setText(fromMemory.getCurrentStatus().getAccessToken()); }
        catch (Exception e) { accessTokenTextField.setText(""); }

        try { phoneNumberTextField.setText(fromMemory.getCurrentStatus().getPhoneNumber()); }
        catch (Exception e) { phoneNumberTextField.setText(""); }

        try { googleAuthKeyTextField.setText(fromMemory.getCurrentStatus().getGoogleAuthKey()); }
        catch (Exception e) { googleAuthKeyTextField.setText(""); }

        try { timestampTextField.setText(Long.toString(fromMemory.getCurrentStatus().getTimeStamp())); }
        catch (Exception e) { timestampTextField.setText(""); }

        try { syncStatusTextField.setText(fromMemory.getCurrentStatus().getSyncedWithServer().toString()); }
        catch (Exception e) { accessTokenTextField.setText(""); }

        fromMemory.releaseAccessToModel();
        informationTextArea.setText("Successfully retrieved.");
    }

    private void onSave() {
        informationTextArea.setText("");
        ClientStateModel fromMemory = new ClientStateModel(Executors.newCachedThreadPool());
        fromMemory.obtainAccessToModel();
        try {
            @SuppressWarnings("unchecked")
            TypingProfilePojo newTypingProfile = new TypingProfilePojo(
                    idTextField.getText(),
                    machineIdTextField.getText(),
                    userIdTextField.getText(),
                    modelTextField.getText(),
                    PojoHelper.castToThreshold(thresholdTextField.getText()),
                    PojoHelper.castToChallengeStrategyArray(challengesTextField.getText()),
                    sqsTextField.getText());

            ClientStatusPojo newStatus = new ClientStatusPojo(
                    newTypingProfile,
                    AuthConstants.valueOf(authStatusTextField.getText()),
                    SecurityConstants.valueOf(securityStatusTextField.getText()),
                    accessTokenTextField.getText(),
                    phoneNumberTextField.getText(),
                    googleAuthKeyTextField.getText(),
                    Long.parseLong("0" + timestampTextField.getText()));

            fromMemory.enqueueStatus(newStatus);
            ClientInitService.saveToPreferences(fromMemory);
            informationTextArea.setText("Successfully saved.");
        } catch (Exception e) {
            informationTextArea.setText("Could not save to Preferences. " + e.toString());
        } finally {
            fromMemory.releaseAccessToModel();
        }
    }

    private void onClear() {
        informationTextArea.setText("");
        try {
            prefs.clear();
            clearTextFields();
            informationTextArea.setText("Successfully cleared.");
        } catch (BackingStoreException e) {
            informationTextArea.setText("Could not clear. " + e.toString());
        }
    }

    private void clearTextFields() {
        idTextField.setText("");
        machineIdTextField.setText("");
        userIdTextField.setText("");
        modelTextField.setText("");
        thresholdTextField.setText("");
        challengesTextField.setText("");
        sqsTextField.setText("");
        authStatusTextField.setText("");
        securityStatusTextField.setText("");
        accessTokenTextField.setText("");
        phoneNumberTextField.setText("");
        timestampTextField.setText("");
        syncStatusTextField.setText("");
    }

    public static void main( String[] args ) {
        LocalSaveUtil frame = new LocalSaveUtil();
        frame.setTitle("Local Save Utility");
        frame.setContentPane(frame.saveUtilPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.onRetrieve();
    }
}
