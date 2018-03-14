package com.biokey.client.views.panels;

import lombok.Getter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class LoginPanelView {

    private JTextField emailInput;
    private JPasswordField passwordInput;
    private JButton submitButton;
    private JLabel informationLabel;

    @Getter private JPanel loginPanel;

    public LoginPanelView() {
        super();
        submitButton.setEnabled(false);
        emailInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                updateButtonState();
            }
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        passwordInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                updateButtonState();
            }
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }

    private void updateButtonState() {
        // TODO: Not quite right... Needs to perform after key event
        boolean shouldEnable = getEmail().length() + 1 > 0 && getPassword().length() + 1 > 0;
        submitButton.setEnabled(shouldEnable);
    }

    /**
     * Getter method for the string representation of the user's typed email.
     * @return the string representation of the user's typed email
     */
    public String getEmail() {
        return emailInput.getText();
    }

    /**
     * Getter method for the string representation of the user's typed password.
     * @return the string representation of the user's typed password
     */
    public String getPassword() {
        return new String(passwordInput.getPassword());
    }

    /**
     * Setter method to add a new action listener to the submit button.
     * @param l the action listener added to the submit button
     */
    public void addSubmitAction(ActionListener l) {
        submitButton.addActionListener(l);
    }

    /**
     * Setter method to change the enabled status of the submit button.
     * @param enable true if the button should be enabled
     */
    public void setEnableSubmit(boolean enable) {
        submitButton.setEnabled(enable);
    }

    /**
     * Setter method to display text to the user.
     * @param newInfo the new text to display
     */
    public void setInformationText(String newInfo) {
        informationLabel.setText(newInfo);
    }

}
