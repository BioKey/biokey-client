package com.biokey.client.views.panels;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;

import static java.awt.color.ColorSpace.TYPE_Luv;

public class AnalysisResultTrayPanelView {

    class GPanel extends JPanel {

        private float graphPercent;

        public GPanel() {
            super.setPreferredSize(new Dimension(100, 30));
            super.setOpaque(false);
            this.graphPercent = 0;
        }

        public void setResult(float graphPercent) {
            this.graphPercent = graphPercent;
            this.repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int parentWidth =  getParent().getWidth();
            g.setColor(new Color(1, 0, 0,(1-graphPercent)*2.f/3.f+0.3f));
            int width = (int)(graphPercent*parentWidth);
            g.fillRect(parentWidth-width ,0, width, 20);
        }
    }

    @Getter private JPanel analysisResultTrayPanel;
    private JLabel analysisResultLabel;
    private JPanel graphPanel;
    private GPanel graph;

    public AnalysisResultTrayPanelView() {
        graph = new GPanel();
        Color current = analysisResultTrayPanel.getBackground();
        analysisResultTrayPanel.setBackground(new Color(current.getRed(), current.getGreen(), current.getBlue(), current.getAlpha()/2));
        graphPanel.setLayout(new GridLayout());
        graphPanel.add(graph);
    }

    /**
     * Setter method to display analysis result to the user.
     * @param newResult the new analysis result to display
     */
    public void setAnalysisResultText(float newResult) {
        analysisResultLabel.setText(Float.toString(newResult));
        graph.setResult(newResult);
    }
}
