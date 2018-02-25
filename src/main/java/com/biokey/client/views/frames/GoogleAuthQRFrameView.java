package com.biokey.client.views.frames;

import javax.swing.*;

public class GoogleAuthQRFrameView {

    private JLabel imageLabel;
    private JPanel googleAuthPanel;

    public void displayImage(byte[] img, String defaultText) {
        if (img == null) imageLabel.setText(defaultText);
        else imageLabel.setIcon(new ImageIcon(img));

        JFrame frame = new JFrame("Google Auth QR Code");
        frame.setContentPane(googleAuthPanel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
