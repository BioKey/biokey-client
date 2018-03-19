package com.biokey.client.services;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import org.apache.log4j.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.dispatcher.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.util.logging.Level;

/**
 * Service that records user key strokes in the background and registers them to the client state.
 */
public class KeyloggerDaemonService implements ClientStateModel.IClientStatusListener, NativeKeyListener {

    private static Logger log = Logger.getLogger(KeyloggerDaemonService.class);

    private final ClientStateController controller;

    private boolean isRunning = false;

    @Autowired
    public KeyloggerDaemonService(ClientStateController controller) {
        this.controller = controller;

        // Get the logger for "org.jnativehook" and set the level to warning.
        java.util.logging.Logger jNativeHookLog = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        jNativeHookLog.setLevel(Level.WARNING);

        GlobalScreen.setEventDispatcher(new SwingDispatchService());

        // Don't forget to disable the parent handlers.
        jNativeHookLog.setUseParentHandlers(false);
    }

    /**
     * Implementation of listener to the ClientStateModel's status.
     * The status will contain a flag for whether the daemon should be running.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus, boolean isDeleteEvent) {
        if (isDeleteEvent) return;
        if (newStatus != null && newStatus.getAuthStatus() == AuthConstants.AUTHENTICATED &&
                newStatus.getSecurityStatus() == SecurityConstants.UNLOCKED) start();
        else stop();
    }

    /**
     * Start running the daemon.
     */
    private void start() {
        if (isRunning) return;
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            isRunning = true;
        } catch (NativeHookException e) {
            stop();
            JOptionPane.showMessageDialog(null, "There was a problem registering the native hook.", "Alert", JOptionPane.WARNING_MESSAGE);
            log.error("There was an exception registering the native hook.", e);
        } catch (Exception e) {
            log.error("There was an unknown exception when registering the native hook.", e);
        }
    }

    /**
     * Stop running the daemon.
     */
    private void stop() {
        if (!isRunning) return;
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.unregisterNativeHook();
            isRunning = false;
        } catch (NativeHookException e) {
            log.error("There was an exception deregistering the native hook.", e);
        }
    }

    /**
     * Unused.
     *
     * @param nativeKeyEvent unused
     */
    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
        // Do nothing.
    }

    /**
     * Enqueue the "up" part of a keystroke.
     *
     * @param nativeKeyEvent the key event
     */
    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        controller.enqueueKeyStroke(new KeyStrokePojo(nativeKeyEvent.getKeyCode(),true, System.currentTimeMillis()));
    }

    /**
     * Enqueue the "up" part of a keystroke.
     *
     * @param nativeKeyEvent the key event
     */
    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        controller.enqueueKeyStroke(new KeyStrokePojo(nativeKeyEvent.getKeyCode(),false, System.currentTimeMillis()));
    }
}
