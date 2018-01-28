package com.biokey.client.views;

import com.biokey.client.controllers.challenges.TextMessageStrategy;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TextMessageChallengeView {

    private JButton sendMessage;
    private JPanel panel;
    private JButton submitCodeButton;
    private JTextField code;
    private TextMessageStrategy textMessageStrategy;

    public TextMessageChallengeView() {
        textMessageStrategy = new TextMessageStrategy();
        sendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textMessageStrategy.sendMessage();
            }
        });
        submitCodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(textMessageStrategy.performChallenges(code.getText()));
            }
        });
    }

    public static void main (String [] args)
    {
        JFrame frame = new JFrame("Text Message Challenge Strategy");
        frame.setContentPane(new TextMessageChallengeView().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
