package com.biokey.client.views.frames;

import com.biokey.client.controllers.challenges.GoogleAuthStrategy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GoogleAuthQRFrameView {

    private JLabel imageLabel;
    private JPanel googleAuthPanel;
    private JTextField confirmField;
    GoogleAuthStrategy auth;
    JFrame frame;

    public GoogleAuthQRFrameView() {
    }

    public void displayImage(byte[] img, String defaultText, GoogleAuthStrategy auth) {
        if (img == null) imageLabel.setText(defaultText);
        else imageLabel.setIcon(new ImageIcon(img));

        this.auth = auth;


        frame = new JFrame("Google Auth QR Code");
        frame.setContentPane(googleAuthPanel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);

        confirmField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if(c < '0' || c > '9')
                    e.consume();
            }
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                if (auth.validateChallenge(confirmField.getText())) {
                    if (auth.checkChallenge(confirmField.getText())) {
                        frame.dispose();
                    }
                    else {
                        confirmField.setText("");
                    }
                }

            }
        });

        frame.pack();
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
        frame.setVisible(true);
    }
}
