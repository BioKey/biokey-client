package com.biokey.client.views.panels.challenges;

import lombok.Getter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GoogleAuthChallengePanelView implements ChallengePanelView {

    private JTextField code;
    private JButton submitButton;
    private JButton altButton;
    private JLabel informationLabel;

    @Getter private JPanel challengePanel;

    public GoogleAuthChallengePanelView() {
        super();
        code.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if(c < '0' || c > '9')
                    e.consume();
            }
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {}
        });
    }

    public String getCode() {
        return code.getText();
    }

    public void addSendAction(ActionListener l) {
        // Do nothing.
    }

    public void addSubmitAction(ActionListener l) {
        submitButton.addActionListener(l);
        code.addActionListener(l);
    }

    public void addResendAction(ActionListener l) {
        // Do nothing.
    }

    public void addKeyAction(ActionListener l) { code.addKeyListener(new KeyListener() {
        public void keyTyped(KeyEvent e) {}
        public void keyPressed(KeyEvent e) {}
        public void keyReleased(KeyEvent e) { l.actionPerformed(new ActionEvent(this, e.getID(), code.getText())); }
    }); }

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
        informationLabel.setText("<html><div style='text-align: center;'>" + newInfo.replace("\n", "<br/>") + "</div></html>");
    }

    public void drawFocus() {
        code.requestFocusInWindow();
    }

    public void clearCode() {
        code.setText("");
    }
}
