package com.biokey.client.views.panels.challenges;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ChallengeOptionPanelView {

    @Getter private JPanel challengeOptionPanel;

    public ChallengeOptionPanelView() {
        challengeOptionPanel.setLayout(new GridLayout(0, 1, 5, 5));
        challengeOptionPanel.add(new JLabel("Choose how you want to authenticate:"));
    }
    /**
     * Add another option with the specified label and action when clicked.
     *
     * @param label the label of the option
     * @param l the action when clicked
     */
    public void addOption(String label, ActionListener l) {
        JButton newOption = new JButton(label);
        newOption.addActionListener(l);
        challengeOptionPanel.add(newOption);
    }

    /**
     * Clear all options.
     */
    public void clearOptions() {
        challengeOptionPanel.removeAll();
        challengeOptionPanel.add(new JLabel("Choose how you want to authenticate:"));
    }
}
