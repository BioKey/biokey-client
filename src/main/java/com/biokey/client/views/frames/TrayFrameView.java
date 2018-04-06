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
        lockedImage = Toolkit.getDefaultToolkit().getImage(TrayFrameView.class.getResource("locked.png"));
        unlockedImage = Toolkit.getDefaultToolkit().getImage(TrayFrameView.class.getResource("unlocked.png"));

        // Define the frame.
        trayFrame.setUndecorated(true);
        trayFrame.setAlwaysOnTop(true);
        trayFrame.setBackground(new Color(0,0,0,0));
        trayFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        trayFrame.pack();
        trayFrame.setResizable(displayFrame);

        // Create the tray icon.
        icon = new TrayIcon(unlockedImage);
        icon.setImageAutoSize(true);

        final PopupMenu popup = new PopupMenu();
        // Create a pop-up menu components
        MenuItem debugItem = new MenuItem("Toggle Debug");
        MenuItem exitItem = new MenuItem("Exit");

        debugItem.addActionListener(e -> {
            displayFrame = !displayFrame;
            trayFrame.setVisible(displayFrame);
        });

        exitItem.addActionListener(e -> {
            System.exit(0);
        });

        //Add components to pop-up menu
        popup.add(debugItem);
        popup.add(exitItem);

        icon.setPopupMenu(popup);

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
