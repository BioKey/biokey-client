package com.biokey.client.views.panels;

import lombok.Getter;

import javax.swing.*;
import java.awt.event.ActionListener;

public class TextMessagePanelView {

    private JButton sendButton;
    private JTextField code;
    private JButton submitButton;
    private JButton resendButton;
    private JButton altButton;
    private JLabel informationLabel;

    @Getter private JPanel textMessagePanel;

    /**
     * Getter method for the string representation of the user's typed code.
     * @return the string representation of the user's typed code
     */
    public String getCode() {
        return code.getText();
    }

    /**
     * Setter method to add a new action listener to the send button.
     * @param l the action listener added to the send button
     */
    public void addSendAction(ActionListener l) {
        sendButton.addActionListener(l);
    }

    /**
     * Setter method to add a new action listener to the submit button.
     * @param l the action listener added to the submit button
     */
    public void addSubmitAction(ActionListener l) {
        submitButton.addActionListener(l);
    }

    /**
     * Setter method to add a new action listener to the resend button.
     * @param l the action listener added to the resend button
     */
    public void addResendAction(ActionListener l) {
        resendButton.addActionListener(l);
    }

    /**
     * Setter method to add a new action listener to the alt strategy button.
     * @param l the action listener added to the alt strategy button
     */
    public void addAltAction(ActionListener l) {
        altButton.addActionListener(l);
    }

    /**
     * Setter method to change the enabled status of the send button.
     * @param enable true if the button should be enabled
     */
    public void setEnableSend(boolean enable) {
        sendButton.setEnabled(enable);
    }

    /**
     * Setter method to change the enabled status of the submit button.
     * @param enable true if the button should be enabled
     */
    public void setEnableSubmit(boolean enable) {
        submitButton.setEnabled(enable);
    }

    /**
     * Setter method to change the enabled status of the resend button.
     * @param enable true if the button should be enabled
     */
    public void setEnableResend(boolean enable) {
        resendButton.setEnabled(enable);
    }

    /**
     * Setter method to change the enabled status of the alt strategy button.
     * @param enable true if the button should be enabled
     */
    public void setEnableAlt(boolean enable) {
        altButton.setEnabled(enable);
    }

    /**
     * Setter method to display text to the user.
     * @param newInfo the new text to display
     */
    public void setInformationText(String newInfo) {
        informationLabel.setText(newInfo);
    }
}