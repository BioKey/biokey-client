package com.biokey.client.views.panels.challenges;

import lombok.Getter;

import javax.swing.*;
import java.awt.event.ActionListener;

public class GoogleAuthChallengePanelView implements ChallengePanelView {

    private JTextField code;
    private JButton submitButton;
    private JButton altButton;
    private JLabel informationLabel;

    @Getter private JPanel challengePanel;

    public String getCode() {
        return code.getText();
    }

    public void addSendAction(ActionListener l) {
        // Do nothing.
    }

    public void addSubmitAction(ActionListener l) {
        submitButton.addActionListener(l);
    }

    public void addResendAction(ActionListener l) {
        // Do nothing.
    }

    public void addAltAction(ActionListener l) {
        altButton.addActionListener(l);
    }

    public void setEnableSend(boolean enable) {
        // Do nothing.
    }

    public void setEnableSubmit(boolean enable) {
        submitButton.setEnabled(enable);
    }

    public void setEnableResend(boolean enable) {
        // Do nothing.
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
