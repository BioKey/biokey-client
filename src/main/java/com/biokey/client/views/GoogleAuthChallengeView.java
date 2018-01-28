package com.biokey.client.views;
import com.biokey.client.controllers.challenges.GoogleAuthStrategy;

import javax.swing.*;

import com.biokey.client.providers.AppProvider;
import com.biokey.client.services.ClientInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GoogleAuthChallengeView extends JFrame{
   // @Autowired
    GoogleAuthStrategy strategy = new GoogleAuthStrategy();


    private JPanel panel;
    private JTextField authPassword;
    private JButton submitButton;

    public GoogleAuthChallengeView ()
    {
        System.out.println("here");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean valid = strategy.performChallenges(authPassword.getText());
                System.out.println(valid);
            }
        });
    }

    public static void main (String [] args)
    {
        JFrame frame = new JFrame("Google Auth Challenge Strategy");
        frame.setContentPane(new GoogleAuthChallengeView().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
