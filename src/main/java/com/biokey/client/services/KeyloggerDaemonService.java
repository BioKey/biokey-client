package com.biokey.client.services;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import lombok.Getter;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.dispatcher.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service that records user key strokes in the background and registers them to the client state.
 */
public class KeyloggerDaemonService implements ClientStateModel.IClientStatusListener, NativeKeyListener {

    private ClientStateController controller;
    private ClientStateModel state;

    @Autowired
    public KeyloggerDaemonService(ClientStateController controller, ClientStateModel state) {
        this.controller = controller;
        this.state = state;

        // Get the logger for "org.jnativehook" and set the level to warning.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);

        GlobalScreen.setEventDispatcher(new SwingDispatchService());

        // Don't forget to disable the parent handlers.
        logger.setUseParentHandlers(false);
    }

    /**
     * Implementation of listener to the ClientStateModel's status.
     * The status will contain a flag for whether the daemon should be running.
     */
    public void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {

        // TODO: needs more thought on different cases - think I'm happy with this logic. Can't think of anything else that it would be.
        /*
         * If the client becomes authenticated, start logging keystrokes.
         * If the client becomes unauthenticated, stop logging keystrokes.
         */

        if(oldStatus.getAuthStatus() != newStatus.getAuthStatus()){
            if(newStatus.getAuthStatus() == AuthConstants.AUTHENTICATED) start();
            else stop();
        }

    }

    /**
     * Start running the daemon.
     *
     * @return true if daemon successfully started
     */
    private boolean start() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            return true;
        }
        catch (NativeHookException ex) {
            stop();
            JOptionPane.showMessageDialog(null, "There was a problem registering the native hook.", "Alert", JOptionPane.WARNING_MESSAGE);
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
        }
        catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        return false;
    }

    /**
     * Stop running the daemon.
     *
     * @return true if daemon successfully stopped
     */
    private boolean stop() {
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.unregisterNativeHook();
            return true;
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Register keystrokes with the client state.
     */
    private void send() {
        return;
    }


    /**
     * Unused
     * @param nativeKeyEvent unused
     */
    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
        //do nothing

    }

    /**
     * Enqueue the "up" part of a keystroke
     * @param nativeKeyEvent the key event
     */
    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        controller.enqueueKeyStroke(new KeyStrokePojo(nativeKeyEvent.getKeyChar(),true,System.currentTimeMillis()));
    }

    /**
     * Enqueue the "up" part of a keystroke
     * @param nativeKeyEvent the key event
     */
    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        controller.enqueueKeyStroke(new KeyStrokePojo(nativeKeyEvent.getKeyChar(),false,System.currentTimeMillis()));
    }
}
