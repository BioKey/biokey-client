package com.biokey.client.models;

import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.KeyStrokesPojo;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.security.AccessControlException;
import java.util.Deque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Model serves as the single source of truth for all client state information.
 * State can be observed by any service who registers a listener.
 */
public class ClientStateModel implements Serializable {

    /**
     * Interface describing the contract for listeners to the client status.
     * Listeners will be notified when a new status has been added.
     */
    public interface IClientStatusListener {
        void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus);
    }

    /**
     * Interface describing the contract for listeners to the keystroke queues.
     * Listeners will be notified when the queues are modified.
     */
    public interface IClientKeyListener {
        void keystrokeQueueChanged(KeyStrokePojo newKey);
    }

    /**
     * Interface describing the contract for listeners to the analysis result queue.
     * Listeners will be notified when the queue is modified.
     */
    public interface IClientAnalysisListener {
        void analysisResultQueueChanged(AnalysisResultPojo newResult);
    }

    private static final long serialVersionUID = 100;

    private ClientStatusPojo currentStatus;
    private Deque<ClientStatusPojo> unsyncedStatuses = new LinkedBlockingDeque<>();
    private Deque<AnalysisResultPojo> unsyncedAnalysisResults = new LinkedBlockingDeque<>();
    private Deque<KeyStrokesPojo> unsyncedKeyStrokes = new LinkedBlockingDeque<>();
    private Deque<KeyStrokePojo> allKeyStrokes = new LinkedBlockingDeque<>(); // need a record of all keyStrokes for the analysis engine

    private transient final ReentrantLock statusLock = new ReentrantLock(true);
    private transient boolean retrievedStatusBeforeEnqueue = false;
    private transient final ReentrantLock analysisResultLock = new ReentrantLock(true);
    private transient final ReentrantLock keyStrokesLock = new ReentrantLock(true);

    @Setter @NonNull
    private transient Set<IClientStatusListener> statusListeners;

    @Setter @NonNull
    private transient Set<IClientKeyListener> keyQueueListeners;

    @Setter @NonNull
    private transient Set<IClientAnalysisListener> analysisResultQueueListeners;

    /**
     * Obtain access to status. If a thread is not holding this lock, it can not get or modify the status.
     * A thread that has called this method must make sure to release the lock through a finally block.
     */
    public void obtainAccessToStatus() {
        statusLock.lock();
        retrievedStatusBeforeEnqueue = false;
    }

    /**
     * Release access to get and modify status.
     */
    public void releaseAccessToStatus() {
        statusLock.unlock();
        retrievedStatusBeforeEnqueue = false;
    }

    /**
     * Obtain access to analysis results. If a thread is not holding this lock, it can not modify the analysis results.
     * A thread that has called this method must make sure to release the lock through a finally block.
     */
    public void obtainAccessToAnalysisResult() {
        analysisResultLock.lock();
    }

    /**
     * Release access to get and modify analysis results.
     */
    public void releaseAccessToAnalysisResult() {
        analysisResultLock.unlock();
    }

    /**
     * Obtain access to key strokes. If a thread is not holding this lock, it can not get or modify the key strokes.
     * A thread that has called this method must make sure to release the lock through a finally block.
     */
    public void obtainAccessToKeyStrokes() {
        keyStrokesLock.lock();
    }

    /**
     * Release access to get and modify key strokes.
     */
    public void releaseAccessToKeyStrokes() {
        keyStrokesLock.unlock();
    }

    /**
     * Obtain access to the entire model. If a thread is not holding this lock, it can not get or modify the model.
     * A thread that has called this method must make sure to release the lock through a finally block.
     */
    public void obtainAccessToModel() {
        statusLock.lock();
        analysisResultLock.lock();
        keyStrokesLock.lock();
        retrievedStatusBeforeEnqueue = true;
    }

    /**
     * Release access to get and modify the entire model.
     */
    public void releaseAccessToModel() {
        statusLock.unlock();
        analysisResultLock.unlock();
        keyStrokesLock.unlock();
        retrievedStatusBeforeEnqueue = false;
    }

    /**
     * Loads state from another instance of ClientStateModel.
     *
     * @param fromMemory client state to be loaded
     */
    public void loadStateFromMemory(ClientStateModel fromMemory) {
        if (!statusLock.isHeldByCurrentThread() ||
                !keyStrokesLock.isHeldByCurrentThread() ||
                !analysisResultLock.isHeldByCurrentThread())
            throw new AccessControlException("statusLock needs to be acquired by the thread");

        // Copy to the current ClientStateModel.
        this.currentStatus = fromMemory.currentStatus;
        this.unsyncedStatuses = fromMemory.unsyncedStatuses;
        this.unsyncedKeyStrokes = fromMemory.unsyncedKeyStrokes;
        this.unsyncedAnalysisResults = fromMemory.unsyncedAnalysisResults;
        this.allKeyStrokes = fromMemory.allKeyStrokes;

        // Notify all the listeners.
        notifyStatusChange(null, currentStatus);
        notifyKeyQueueChange(allKeyStrokes.peekLast());
        notifyAnalysisResultQueueChange(unsyncedAnalysisResults.peekLast());
    }

    /**
     * Getter method for the current status.
     *
     * @return current status
     */
    public ClientStatusPojo getCurrentStatus() {
        if (!statusLock.isHeldByCurrentThread()) throw new AccessControlException("statusLock needs to be acquired by the thread");
        retrievedStatusBeforeEnqueue = true;
        return currentStatus;
    }

    /**
     * Add a new (immutable) status to unsynced queue.
     *
     * @param status new status to add to queue
     */
    public void enqueueStatus(@NonNull ClientStatusPojo status) {
        if (!statusLock.isHeldByCurrentThread() || !retrievedStatusBeforeEnqueue)
            throw new AccessControlException("statusLock needs to be acquired by the thread");

        ClientStatusPojo oldStatus = currentStatus;
        unsyncedStatuses.add(status);
        currentStatus = status;

        // Notify listeners of the change.
        notifyStatusChange(oldStatus, currentStatus);
    }

    /**
     * Dequeue the oldest status from the unsynced queue.
     *
     * @return true if the oldest status in the queue has been removed
     */
    public boolean dequeueStatus() {
        if (!statusLock.isHeldByCurrentThread()) throw new AccessControlException("statusLock needs to be acquired by the thread");

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
        if (!statusLock.isHeldByCurrentThread()) throw new AccessControlException("statusLock needs to be acquired by the thread");
        return unsyncedStatuses.peek();
    }

    /**
     * Add a new (immutable) analysis result to unsynced queue.
     *
     * @param result new (immutable) analysis result that will be enqueued
     */
    public void enqueueAnalysisResult(@NonNull AnalysisResultPojo result) {
        if (!analysisResultLock.isHeldByCurrentThread()) throw new AccessControlException("analysisResultLock needs to be acquired by the thread");
        unsyncedAnalysisResults.add(result);
        notifyAnalysisResultQueueChange(result);
    }

    /**
     * Dequeue the oldest analysis result from the unsynced queue.
     *
     * @throws AccessControlException if analysisResultLock is not held by the current thread
     * @return true if the oldest analysis result in the queue has been removed
     */
    public boolean dequeueAnalysisResult() {
        if (!analysisResultLock.isHeldByCurrentThread()) throw new AccessControlException("analysisResultLock needs to be acquired by the thread");
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
        if (!analysisResultLock.isHeldByCurrentThread()) throw new AccessControlException("analysisResultLock needs to be acquired by the thread");
        return unsyncedAnalysisResults.peek();
    }

    /**
     * Add a new (immutable) key stroke to unsynced and all key strokes queues.
     *
     * @param keyStroke new key stroke that will be enqueued
     */
    public void enqueueKeyStroke(@NonNull KeyStrokePojo keyStroke) {
        if (!keyStrokesLock.isHeldByCurrentThread()) throw new AccessControlException("keyStrokesLock needs to be acquired by the thread");
        if (unsyncedKeyStrokes.isEmpty()) unsyncedKeyStrokes.add(new KeyStrokesPojo());
        unsyncedKeyStrokes.getLast().getKeyStrokes().add(keyStroke);
        allKeyStrokes.add(keyStroke);
        notifyKeyQueueChange(keyStroke);
    }

    /**
     * Divides the unsynced key strokes queue and bundles all the key strokes since last division together.
     * dequeueSyncedKeyStrokes() will dequeue the last bundle.
     */
    public void divideKeyStrokes() {
        if (!keyStrokesLock.isHeldByCurrentThread()) throw new AccessControlException("keyStrokesLock needs to be acquired by the thread");
        unsyncedKeyStrokes.add(new KeyStrokesPojo());
    }

    /**
     * Dequeue the oldest window of key strokes from the unsynced queue.
     *
     * @return true if the oldest key strokes in the queue has been removed
     */
    public boolean dequeueSyncedKeyStrokes() {
        if (!keyStrokesLock.isHeldByCurrentThread()) throw new AccessControlException("keyStrokesLock needs to be acquired by the thread");
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
        if (!keyStrokesLock.isHeldByCurrentThread()) throw new AccessControlException("keyStrokesLock needs to be acquired by the thread");
        if (allKeyStrokes.isEmpty()) return false;
        allKeyStrokes.remove();
        return true;
    }

    /**
     * Peek at the oldest window of key strokes from the unsynced queue.
     *
     * @return the oldest key strokes
     */
    public KeyStrokesPojo getOldestKeyStrokes() {
        if (!keyStrokesLock.isHeldByCurrentThread()) throw new AccessControlException("keyStrokesLock needs to be acquired by the thread");
        return unsyncedKeyStrokes.peek();
    }

    /**
     * Get all the keystrokes in client memory.
     *
     * @return all the keystrokes in client memory
     */
    public Queue<KeyStrokePojo> getKeyStrokes() {
        if (!keyStrokesLock.isHeldByCurrentThread()) throw new AccessControlException("keyStrokesLock needs to be acquired by the thread");
        return allKeyStrokes;
    }

    /**
     * Notifies all the listeners of a status change.
     *
     * @param oldStatus the status that was replaced
     * @param newStatus the new current status
     */
    private void notifyStatusChange(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {
        for (IClientStatusListener listener : statusListeners) {
             Runnable r = (Runnable & Serializable) () -> listener.statusChanged(oldStatus, newStatus);
             r.run();
        }
    }

    /**
     * Notifies all the listeners of a key queue change.
     *
     * @param newKey the newest keystroke
     */
    private void notifyKeyQueueChange(KeyStrokePojo newKey) {
        for (IClientKeyListener listener : keyQueueListeners) {
            Runnable r = (Runnable & Serializable) () -> listener.keystrokeQueueChanged(newKey);
            r.run();
        }
    }

    /**
     * Notifies all the listeners of an analysis result queue change.
     *
     * @param newResult the newest analysis result
     */
    private void notifyAnalysisResultQueueChange(AnalysisResultPojo newResult) {
        for (IClientAnalysisListener listener : analysisResultQueueListeners) {
            Runnable r = (Runnable & Serializable) () -> listener.analysisResultQueueChanged(newResult);
            r.run();
        }
    }
    
}
