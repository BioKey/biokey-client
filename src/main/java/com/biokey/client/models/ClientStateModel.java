package com.biokey.client.models;

import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.KeyStrokesPojo;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.util.Deque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Model serves as the single source of truth for all client state information.
 * State can be observed by any service who registers a listener.
 */
public class ClientStateModel {

    static Logger log = Logger.getLogger(ClientStateModel.class);

    /**
     * Interface describing the contract for listeners to the client data.
     * Listeners will be notified when a new status has been added.
     */
    public interface IClientStateListener {
        void stateChanged();
    }

    /**
     * Interface describing the contract for controllers to change client status.
     */
    public interface IStatusChangeController {
        ClientStatusPojo newStatus(ClientStatusPojo currentStatus);
    }

    @Getter private ClientStatusPojo currentStatus;
    private Semaphore statusLock = new Semaphore(1, true);
    private Queue<ClientStatusPojo> unsyncedStatuses = new LinkedBlockingQueue<>();
    private Queue<AnalysisResultPojo> unsyncedAnalysisResults = new LinkedBlockingQueue<>();
    private Deque<KeyStrokesPojo> unsyncedKeyStrokes = new LinkedBlockingDeque<>();
    private Queue<KeyStrokePojo> allKeyStrokes = new LinkedBlockingQueue<>(); // need a record of all keyStrokes for the analysis engine

    @Setter @NonNull
    private Set<IClientStateListener> stateListeners;

    /**
     * Add a new (immutable) status to unsynced queue.
     *
     * @param newStatusController controller that takes current status and returns new status
     * @return true if the thread acquired the lock and the status was enqueued
     */
    public boolean enqueueStatus(@NonNull IStatusChangeController newStatusController) {
        try {
            // Acquire the lock so no threads can also enqueue.
            statusLock.acquire();

            // Run the controller code and add the new status to member variables.
            ClientStatusPojo newStatus = newStatusController.newStatus(currentStatus);
            unsyncedStatuses.add(newStatus);
            currentStatus = newStatus;

            // Notify listeners of the change.
            notifyChange();
            return true;
        } catch (InterruptedException e) {
            log.debug("Another thread interrupted " + Thread.currentThread().getName() + " from getCurrentStatus()");
            return false;
        } finally {
            if (statusLock.availablePermits() == 0) statusLock.release();
        }
    }

    /**
     * Dequeue the oldest status from the unsynced queue.
     *
     * @return true if the oldest status in the queue has been removed
     */
    public boolean dequeueStatus() {
        if (unsyncedStatuses.isEmpty()) return false;
        unsyncedStatuses.remove();
        return true;
    }

    /**
     * Peek at the oldest status from the unsynced queue.
     *
     * @return the oldest status
     */
    public ClientStatusPojo getOldestStatus() {
        return unsyncedStatuses.peek();
    }

    /**
     * Add a new (immutable) analysis result to unsynced queue.
     *
     * @param result new (immutable) analysis result that will be enqueued
     */
    public void enqueueAnalysisResult(@NonNull AnalysisResultPojo result) {
        unsyncedAnalysisResults.add(result);
    }

    /**
     * Dequeue the oldest analysis result from the unsynced queue.
     *
     * @return true if the oldest analysis result in the queue has been removed
     */
    public boolean dequeueAnalysisResult() {
        if (unsyncedAnalysisResults.isEmpty()) return false;
        unsyncedAnalysisResults.remove();
        return true;
    }

    /**
     * Peek at the oldest analysis result from the unsynced queue.
     *
     * @return the oldest analysis result
     */
    public AnalysisResultPojo getOldestAnalysisResult() {
        return unsyncedAnalysisResults.peek();
    }

    /**
     * Add a new (immutable) key stroke to unsynced and all key strokes queues.
     *
     * @param keyStroke new key stroke that will be enqueued
     */
    public void enqueueKeyStroke(@NonNull KeyStrokePojo keyStroke) {
        if (unsyncedKeyStrokes.isEmpty()) unsyncedKeyStrokes.add(new KeyStrokesPojo());
        unsyncedKeyStrokes.getLast().getKeyStrokes().add(keyStroke);
        allKeyStrokes.add(keyStroke);
    }

    /**
     * Divides the unsynced key strokes queue and bundles all the key strokes since last division together.
     * dequeueSyncedKeyStrokes() will dequeue the last bundle.
     */
    public void divideKeyStrokes() {
        unsyncedKeyStrokes.add(new KeyStrokesPojo());
    }

    /**
     * Dequeue the oldest bundle of key strokes from the unsynced queue.
     *
     * @return true if the oldest key strokes in the queue has been removed
     */
    public boolean dequeueSyncedKeyStrokes() {
        if (unsyncedKeyStrokes.isEmpty()) return false;
        unsyncedKeyStrokes.remove();
        return true;
    }

    /**
     * Dequeue the oldest key stroke from the all key strokes queue.
     *
     * @return true if the oldest key stroke in the queue has been removed
     */
    public boolean dequeueAllKeyStrokes() {
        if (allKeyStrokes.isEmpty()) return false;
        allKeyStrokes.remove();
        return true;
    }

    /**
     * Peek at the oldest bundle of key strokes from the unsynced queue.
     *
     * @return the oldest key strokes
     */
    public KeyStrokesPojo getOldestKeyStrokes() {
        return unsyncedKeyStrokes.peek();
    }

    /**
     * Get all the keystrokes in client memory.
     *
     * @return all the keystrokes in client memory
     */
    public Queue<KeyStrokePojo> getKeyStrokes() {
        return allKeyStrokes;
    }

    /**
     * Notifies all the listeners of a status change.
     */
    private void notifyChange() {
         for (IClientStateListener listener : stateListeners) {
            listener.stateChanged();
        }
    }
}
