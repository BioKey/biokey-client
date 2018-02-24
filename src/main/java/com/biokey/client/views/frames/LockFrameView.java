package com.biokey.client.views.frames;

import javax.swing.*;
import java.awt.*;

/**
 * Defines the view for the lock screen and provides functionality to lock/unlock and swap panels into the lock screen.
 */
public class LockFrameView {

    private JFrame lockFrame = new JFrame();

    /**
     * Constructor sets up the view for the locked UX.
     */
    public LockFrameView() {
        // TODO: Sankalp should reconfigure this frame to work with locking!
        // TODO: Make sure to test for double screen, test disable special key presses (only allow alphanum), test...
        // lockFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // https://stackoverflow.com/questions/32077449/whats-the-point-of-setdefaultcloseoperationwindowconstants-exit-on-close
        lockFrame.setUndecorated(true);
        lockFrame.pack();
        // lockFrame.setAlwaysOnTop(true); // Pushes frame to the top.
        lockFrame.setResizable(false); // Can't change the size of frame.
        lockFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        lockFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height); // Fullscreen based on screen size.
    }

    /**
     * Tell this view to go into locked UX.
     */
    public void lock() {
        // TODO: Sankalp please verify these functions work!
        lockFrame.setVisible(true);
    }

    /**
     * Tell this view to exist locked UX.
     */
    public void unlock() {
        // TODO: Sankalp please verify these functions work!
        lockFrame.setVisible(false);
    }

    /**
     * Add the panel to the view.
     * @param panel panel to add to view
     */
    public void addPanel(JPanel panel) {
        lockFrame.getContentPane().add(panel);
        lockFrame.getContentPane().revalidate();
        lockFrame.pack();
        lockFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
    }

    /**
     * Remove the panel from the view.
     * @param panel panel to hide
     */
    public void hidePanel(JPanel panel) {
        lockFrame.getContentPane().add(panel);
    }

    /**
     * Remove all panels from the view.
     */
    public void hideAllPanels() {
        lockFrame.getContentPane().removeAll();
    }
}
