package com.biokey.client.views.panels.challenges;

import lombok.Getter;

import javax.swing.*;
import java.awt.event.ActionListener;

public class TextMessageChallengePanelView implements ChallengePanelView {

    private JTextField code;
    private JButton sendButton;
    private JButton submitButton;
    private JButton resendButton;
    private JButton altButton;
    private JLabel informationLabel;

    @Getter private JPanel challengePanel;

    public String getCode() {
        return code.getText();
    }

    public void addSendAction(ActionListener l) {
        sendButton.addActionListener(l);
    }

    public void addSubmitAction(ActionListener l) {
        submitButton.addActionListener(l);
    }

    public void addResendAction(ActionListener l) {
        resendButton.addActionListener(l);
    }

    public void addAltAction(ActionListener l) {
        altButton.addActionListener(l);
    }

    public void setEnableSend(boolean enable) {
        sendButton.setEnabled(enable);
    }

    public void setEnableSubmit(boolean enable) {
        submitButton.setEnabled(enable);
    }

    public void setEnableResend(boolean enable) {
        resendButton.setEnabled(enable);
    }

    public void setEnableAlt(boolean enable) {
        altButton.setEnabled(enable);
    }

    public void setInformationText(String newInfo) {
        informationLabel.setText(newInfo);
    }

    public void clearCode() {
        code.setText("");
    }
}
