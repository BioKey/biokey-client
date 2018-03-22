package com.biokey.client.views.frames;

import com.biokey.client.helpers.LockerHelper;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Defines the view for the lock screen and provides functionality to lock/unlock and swap panels into the lock screen.
 */
public class LockFrameView {
    private JFrame[] lockFrames;
    private LockerHelper lockerHelper;

    /**
     * Constructor sets up the vie3e3ew for the locked UX.
     */
    public LockFrameView() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        this.lockFrames = new JFrame[gs.length];

        for (int j = 0; j < gs.length; j++) {
            GraphicsDevice gd = gs[j];
            this.lockFrames[j] = new JFrame(gd.getDefaultConfiguration());

            this.lockFrames[j].setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            this.lockFrames[j].setUndecorated(true);
            this.lockFrames[j].pack();
            this.lockFrames[j].setAlwaysOnTop(true);
            this.lockFrames[j].setResizable(false);
            this.lockFrames[j].setExtendedState(JFrame.MAXIMIZED_BOTH);
            this.lockFrames[j].setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
        }
        lockerHelper = new LockerHelper(lockFrames);
    }

    /*** Tell this to go into locked UX.
     */
    public void lock() {
        for (JFrame lockFrame : lockFrames) {
            lockFrame.setVisible(true);
        }
        // lockerHelper.start();
    }

    /**
     * Tell this view to exist locked UX.
     */
    public void unlock() {
        // lockerHelper.stop();
        for (JFrame lockFrame : lockFrames) {
            lockFrame.setVisible(false);
        }
    }

    /**
     * Add the panel to the view.
     * @param panel panel to add to view
     */
    public void addPanel(JPanel panel) {
        for (JFrame lockFrame : lockFrames) {
            lockFrame.getContentPane().add(panel);
            revalidateContentPane();
        }
    }

    /**
     * Remove the panel from the view.
     * @param panel panel to hide
     */
    public void removePanel(JPanel panel) {
        for (JFrame lockFrame : lockFrames) {
            lockFrame.getContentPane().remove(panel);
            revalidateContentPane();
        }
    }

    /**
     * Remove all panels from the view.
     */
    public void removeAllPanels() {
        for (JFrame lockFrame : lockFrames) {
            lockFrame.getContentPane().removeAll();
            revalidateContentPane();
        }
    }

    /**
     * Revalidate content pane, repack, and make sure size is still maximized.
     */
    private void revalidateContentPane() {
        for (JFrame lockFrame : lockFrames) {
            lockFrame.getContentPane().revalidate();
            lockFrame.pack();
            lockFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
        }
    }
}
