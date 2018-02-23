package com.biokey.client.views.panels;

import lombok.Getter;

import javax.swing.*;

public class AnalysisResultTrayPanelView {

    @Getter private JPanel analysisResultTrayPanel;
    private JLabel analysisResultLabel;

    /**
     * Setter method to display analysis result to the user.
     * @param newResult the new analysis result to display
     */
    public void setAnalysisResultText(float newResult) {
        analysisResultLabel.setText(Float.toString(newResult));
    }
}
