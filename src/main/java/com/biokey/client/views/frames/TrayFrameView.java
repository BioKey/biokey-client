package com.biokey.client.views.frames;

import org.apache.log4j.Logger;
import org.springframework.util.ResourceUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;

/**
 * Defines the view for the screen opened from the system tray.
 */
public class TrayFrameView {

    private static Logger log = Logger.getLogger(TrayFrameView.class);

    private TrayIcon icon;
    private JFrame trayFrame = new JFrame();

    private Image lockedImage;
    private Image unlockedImage;

    private boolean displayFrame = false;

    /**
     * Initializes the tray icon with the unlocked image.
     */
    public TrayFrameView() {
        // Check if tray menu is supported.
        SystemTray tray;
        if (!SystemTray.isSupported()) {
            log.error("System tray is not supported.");
            return;
        } else tray = SystemTray.getSystemTray();

        // Load the images.
        try {
            lockedImage = Toolkit.getDefaultToolkit().getImage(ResourceUtils.getURL("src/main/resources/locked.png"));
            unlockedImage = Toolkit.getDefaultToolkit().getImage(ResourceUtils.getURL("src/main/resources/unlocked.png"));
        } catch (FileNotFoundException e) {
            log.error("Tray icon image could not be found.");
            return;
        }

        // Define the frame.
        trayFrame.setUndecorated(true);
        trayFrame.setAlwaysOnTop(true);
        trayFrame.setBackground(new Color(0,0,0,0));
        trayFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        trayFrame.pack();
        trayFrame.setResizable(false);

        // Create the tray icon.
        icon = new TrayIcon(unlockedImage);
        icon.setImageAutoSize(true);
        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                displayFrame = !displayFrame;
                trayFrame.setVisible(displayFrame);
            }
        });

        // Add icon to the tray.
        try {
            tray.add(icon);
        } catch (AWTException e) {
            log.error("Tray icon could not be added.");
        }
    }

    /**
     * Set tray icon based on whether client is authenticated.
     *
     * @param isAuthenticated true if client is authenticated
     */
    public void setTrayIcon(boolean isAuthenticated) {
        if (isAuthenticated) icon.setImage(lockedImage);
        else icon.setImage(unlockedImage);
    }

    /**
     * Add the panel to the view.
     * @param panel panel to add to view
     */
    public void addPanel(JPanel panel) {
        trayFrame.getContentPane().add(panel);
        trayFrame.getContentPane().revalidate();
        trayFrame.pack();
        trayFrame.setLocation(
                Toolkit.getDefaultToolkit().getScreenSize().width - trayFrame.getWidth() - 10,
                Toolkit.getDefaultToolkit().getScreenSize().height - trayFrame.getHeight() - 50);
    }

    /**
     * Remove the panel from the view.
     * @param panel panel to hide
     */
    public void hidePanel(JPanel panel) {
        trayFrame.getContentPane().add(panel);
    }

    /**
     * Remove all panels from the view.
     */
    public void hideAllPanels() {
        trayFrame.getContentPane().removeAll();
    }
}
