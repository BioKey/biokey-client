package com.biokey.client.views.frames;

import javax.swing.*;
import java.awt.event.ActionEvent;

// TODO: delete once the fake is no longer needed.
public class FakeAnalysisFrameView extends JFrame {
    public JTextField analysisResultTextField;
    public JButton enqueueButton;
    public JLabel informationLabel;
    public JPanel fakeAnalysisPanel;

    public FakeAnalysisFrameView() {
        analysisResultTextField.addActionListener((ActionEvent e) -> {
            enqueueButton.doClick();
        });
    }
}
