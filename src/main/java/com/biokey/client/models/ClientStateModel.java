package com.biokey.client.models;

import com.biokey.client.constants.SyncStatusConstants;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.services.IClientStateListener;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Model serves as the single source of truth for all client state information.
 * State can be observed by any service who registers a listener.
 */
@Getter
public class ClientStateModel {

    private Queue<ClientStatusPojo> unsyncedStatuses = new ArrayBlockingQueue<>();
    private Queue<KeyStrokePojo> keyStrokes = new ArrayBlockingQueue<>();
    private Queue<AnalysisResultPojo> analysisResults = new ArrayBlockingQueue<>();
    private Set<IClientStateListener> stateListeners = new HashSet<>();

    /**
     * Add a new status to the queue of unsynced statuses.
     *
     * @param status new (immutable) status of the client that will be enqueued
     */
    public void enqueueStatus(@NonNull ClientStatusPojo status) {
        unsyncedStatuses.add(status);
        notifyChange(status);
    }

    /**
     * Dequeue the oldest status if it has been synced since being enqueued.
     *
     * @return true if the oldest status in the queue has been removed
     */
    public boolean dequeueStatus() {
        if (unsyncedStatuses.isEmpty() || !unsyncedStatuses.peek().getSyncedWithServer().equals(SyncStatusConstants.INSYNC))
            return false;
        unsyncedStatuses.remove();
        return true;
    }

    /**
     * Add a keystroke to the list of unsynced keystrokes.
     *
     * @param keyStroke new (immutable) keystroke that will be enqueued
     */
    public void addKeyStroke(@NonNull KeyStrokePojo keyStroke) {
        keyStrokes.add(keyStroke);
    }

    /**
     * Clears all the synced keystrokes starting from the first enqueued.
     * Assumes that the keystrokes are synced in order; will stop removing keystrokes once an unsynced one is found.
     */
    public void clearKeyStrokes() {
        while (!keyStrokes.isEmpty() && keyStrokes.peek().getSyncedWithServer().equals(SyncStatusConstants.INSYNC)) {
            keyStrokes.poll();
        }
    }

    /**
     * Add an analysis result to the list of unsynced results.
     *
     * @param analysisResult new (immutable) analysis result that will be enqueued
     */
    public void addAnalysisResult(@NonNull AnalysisResultPojo analysisResult) {
        analysisResults.add(analysisResult);
    }

    /**
     * Clears all the synced analysis results starting from the first enqueued.
     * Assumes that the results are synced in order and will stop removing results once an unsynced one is found.
     */
    public void clearAnalysisResults() {
        while (!analysisResults.isEmpty() && analysisResults.peek().getSyncedWithServer().equals(SyncStatusConstants.INSYNC)) {
            analysisResults.poll();
        }
    }

    /**
     * Notifies all the listeners of a status change.
     *
     * @param status the status object representing the new status
     */
    private void notifyChange(@NonNull ClientStatusPojo status) {
        for (IClientStateListener listener : stateListeners) {
            listener.stateChanged(status);
        }
    }

    /**
     * Register a new listener that will be notified once the status changes.
     *
     * @param listener listener instance to be added to list
     */
    public void addListener(@NonNull IClientStateListener listener) {
        stateListeners.add(listener);
    }

    /**
     * Deregister a listener so the service will no longer be notified once the status changes.
     *
     * @param listener listener instance to be deregistered
     * @return true if the listener instance was found
     */
    public boolean removeListener(@NonNull IClientStateListener listener) {
        return stateListeners.remove(listener);
    }
}
