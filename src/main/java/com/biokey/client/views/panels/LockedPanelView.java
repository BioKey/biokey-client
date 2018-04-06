package com.biokey.client.views.panels;

import lombok.Getter;
import org.springframework.util.ResourceUtils;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.net.URL;

public class LockedPanelView {

    @Getter private JPanel lockedPanel;
    private JPanel imagePanel;

    public LockedPanelView() {
        // Make it to a Icon.
        Icon icon = new ImageIcon(LockedPanelView.class.getResource("wait.gif"));

        // Make a new JLabel that shows icon.
        JLabel Gif = new JLabel(icon);
        this.imagePanel.setLayout(new FlowLayout());
        this.imagePanel.add(Gif);
    }
}
