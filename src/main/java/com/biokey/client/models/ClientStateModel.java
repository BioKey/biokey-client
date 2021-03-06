package com.biokey.client.models;

import com.biokey.client.models.pojo.*;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.security.AccessControlException;
import java.util.Deque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
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
        void statusChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus, boolean isDeleteEvent);
    }

    /**
     * Interface describing the contract for listeners to the keystroke queues.
     * Listeners will be notified when the queues are modified.
     */
    public interface IClientKeyListener {
        void keystrokeQueueChanged(KeyStrokePojo newKey, boolean isDeleteEvent);
    }

    /**
     * Interface describing the contract for listeners to the analysis result queue.
     * Listeners will be notified when the queue is modified.
     */
    public interface IClientAnalysisListener {
        void analysisResultQueueChanged(AnalysisResultPojo newResult, boolean isDeleteEvent);
    }

    private static final long serialVersionUID = 100;

    private ClientStatusPojo currentStatus;
    private Deque<ClientStatusPojo> unsyncedStatuses = new LinkedBlockingDeque<>();
    private Deque<AnalysisResultsPojo> unsyncedAnalysisResults = new LinkedBlockingDeque<>();
    private Deque<KeyStrokesPojo> unsyncedKeyStrokes = new LinkedBlockingDeque<>();
    private Deque<KeyStrokePojo> allKeyStrokes = new LinkedBlockingDeque<>(); // need a record of all keyStrokes for the analysis engine

    private final ReentrantLock statusLock = new ReentrantLock(true);
    private boolean retrievedStatusBeforeEnqueue = false;
    private final ReentrantLock analysisResultLock = new ReentrantLock(true);
    private final ReentrantLock keyStrokesLock = new ReentrantLock(true);
    private final transient ExecutorService executor;

    @Setter @NonNull
    private transient Set<IClientStatusListener> statusListeners;

    @Setter @NonNull
    private transient Set<IClientKeyListener> keyQueueListeners;

    @Setter @NonNull
    private transient Set<IClientAnalysisListener> analysisResultQueueListeners;

    public ClientStateModel(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Obtain access to status. If a thread is not holding this lock, it can not get or modify the status.
     * A thread that has called this method must make sure to release the lock through a finally block.
     */
    public void obtainAccessToStatus() {
        if (!statusLock.isHeldByCurrentThread()) statusLock.lock();
        retrievedStatusBeforeEnqueue = false;
    }

    /**
     * Release access to get and modify status.
     */
    public void releaseAccessToStatus() {
        if (statusLock.isHeldByCurrentThread()) statusLock.unlock();
        retrievedStatusBeforeEnqueue = false;
    }

    /**
     * Obtain access to analysis results. If a thread is not holding this lock, it can not modify the analysis results.
     * A thread that has called this method must make sure to release the lock through a finally block.
     */
    public void obtainAccessToAnalysisResult() {
        if (!analysisResultLock.isHeldByCurrentThread()) analysisResultLock.lock();
    }

    /**
     * Release access to get and modify analysis results.
     */
    public void releaseAccessToAnalysisResult() {
        if (analysisResultLock.isHeldByCurrentThread()) analysisResultLock.unlock();
    }

    /**
     * Obtain access to key strokes. If a thread is not holding this lock, it can not get or modify the key strokes.
     * A thread that has called this method must make sure to release the lock through a finally block.
     */
    public void obtainAccessToKeyStrokes() {
        if (!keyStrokesLock.isHeldByCurrentThread()) keyStrokesLock.lock();
    }

    /**
     * Release access to get and modify key strokes.
     */
    public void releaseAccessToKeyStrokes() {
        if (keyStrokesLock.isHeldByCurrentThread()) keyStrokesLock.unlock();
    }

    /**
     * Obtain access to the entire model. If a thread is not holding this lock, it can not get or modify the model.
     * A thread that has called this method must make sure to release the lock through a finally block.
     */
    public void obtainAccessToModel() {
        if (!statusLock.isHeldByCurrentThread()) statusLock.lock();
        if (!analysisResultLock.isHeldByCurrentThread()) analysisResultLock.lock();
        if (!keyStrokesLock.isHeldByCurrentThread()) keyStrokesLock.lock();
        retrievedStatusBeforeEnqueue = true;
    }

    /**
     * Release access to get and modify the entire model.
     */
    public void releaseAccessToModel() {
        if (statusLock.isHeldByCurrentThread()) statusLock.unlock();
        if (analysisResultLock.isHeldByCurrentThread()) analysisResultLock.unlock();
        if (keyStrokesLock.isHeldByCurrentThread()) keyStrokesLock.unlock();
        retrievedStatusBeforeEnqueue = false;
    }

    /**
     * Loads state from another instance of ClientStateModel.
     *
     * @param fromMemory client state to be loaded
     */
    public void loadStateFromMemory(@NonNull ClientStateModel fromMemory) {
        if (!statusLock.isHeldByCurrentThread() ||
                !keyStrokesLock.isHeldByCurrentThread() ||
                !analysisResultLock.isHeldByCurrentThread())
            throw new AccessControlException("Locks needs to be acquired by the thread");

        // Copy to the current ClientStateModel.
        this.currentStatus = fromMemory.currentStatus;
        this.unsyncedStatuses = fromMemory.unsyncedStatuses;
        this.unsyncedKeyStrokes = fromMemory.unsyncedKeyStrokes;
        this.unsyncedAnalysisResults = fromMemory.unsyncedAnalysisResults;
        this.allKeyStrokes = fromMemory.allKeyStrokes;

        // Notify all the listeners?
    }

    /**
     * Checks if the client state model read from memory is valid.
     *
     * @param fromMemory the client state model read from memory
     * @return true if the client state model read from memory is valid
     */
    public boolean checkStateModel(@NonNull ClientStateModel fromMemory) {
        // Check if any of the members are null.
        if (fromMemory.currentStatus == null || fromMemory.unsyncedStatuses == null ||
                fromMemory.unsyncedKeyStrokes == null || fromMemory.unsyncedAnalysisResults == null ||
                fromMemory.allKeyStrokes == null) return false;

        // Check if current status' fields are null.
        return !(fromMemory.currentStatus.getAccessToken() == null);
    }

    /**
     * Clear all the data from the model.
     */
    public void clear() {
        if (!statusLock.isHeldByCurrentThread() ||
                !keyStrokesLock.isHeldByCurrentThread() ||
                !analysisResultLock.isHeldByCurrentThread())
            throw new AccessControlException("Locks needs to be acquired by the thread");

        this.unsyncedStatuses.clear();
        this.unsyncedKeyStrokes.clear();
        this.unsyncedAnalysisResults.clear();
        this.allKeyStrokes.clear();
        this.currentStatus = null;
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

        // ClientStatusPojo oldStatus = currentStatus;
        unsyncedStatuses.add(status);
        currentStatus = status;
        // notifyStatusChange(oldStatus, currentStatus);
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
        if (unsyncedAnalysisResults.isEmpty()) unsyncedAnalysisResults.add(new AnalysisResultsPojo());
        unsyncedAnalysisResults.getLast().getAnalysisResults().add(result);
        // notifyAnalysisResultQueueChange(result);
    }

    /**
     * Dequeue the oldest analysis results from the unsynced queue.
     *
     * @throws AccessControlException if analysisResultLock is not held by the current thread
     * @return true if the oldest analysis results in the queue has been removed
     */
    public boolean dequeueAnalysisResults() {
        if (!analysisResultLock.isHeldByCurrentThread()) throw new AccessControlException("analysisResultLock needs to be acquired by the thread");
        if (unsyncedAnalysisResults.isEmpty()) return false;
        unsyncedAnalysisResults.remove();
        return true;
    }

    /**
     * Divides the unsynced analysis results queue and bundles all the analysis results since last division together.
     * dequeueAnalysisResults() will dequeue the last bundle.
     */
    public void divideAnalysisResults() {
        if (!analysisResultLock.isHeldByCurrentThread()) throw new AccessControlException("analysisResultLock needs to be acquired by the thread");
        unsyncedAnalysisResults.add(new AnalysisResultsPojo());
    }

    /**
     * Peek at the oldest analysis results from the unsynced queue.
     *
     * @return the oldest analysis results
     */
    public AnalysisResultsPojo getOldestAnalysisResults() {
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
        // notifyKeyQueueChange(keyStroke);
    }

    /**
     * Divides the unsynced key strokes queue and bundles all the key strokes since last division together.
     * dequeueOneFromUnsyncedKeyStrokes() will dequeue the last bundle.
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
    public boolean dequeueOneFromUnsyncedKeyStrokes() {
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
    public boolean dequeueOneFromAllKeyStrokes() {
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
    public KeyStrokesPojo getOldestUnsyncedKeyStrokes() {
        if (!keyStrokesLock.isHeldByCurrentThread()) throw new AccessControlException("keyStrokesLock needs to be acquired by the thread");
        return unsyncedKeyStrokes.peek();
    }

    /**
     * Peek at the newest window of key strokes from the unsynced queue.
     *
     * @return the oldest key strokes
     */
    public KeyStrokesPojo getNewestUnsyncedKeyStrokes() {
        if (!keyStrokesLock.isHeldByCurrentThread()) throw new AccessControlException("keyStrokesLock needs to be acquired by the thread");
        return unsyncedKeyStrokes.peekLast();
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
     * @param isDeleteEvent if the queue change was a result of a delete event instead of an enqueue event
     */
    public void notifyStatusChange(ClientStatusPojo oldStatus, ClientStatusPojo newStatus, boolean isDeleteEvent) {
        for (IClientStatusListener listener : statusListeners) {
             executor.execute(() -> listener.statusChanged(oldStatus, newStatus, isDeleteEvent));
        }
    }

    /**
     * Notifies all the listeners of a key queue change.
     *
     * @param newKey the newest keystroke
     * @param isDeleteEvent if the queue change was a result of a delete event instead of an enqueue event
     */
    public void notifyKeyQueueChange(KeyStrokePojo newKey, boolean isDeleteEvent) {
        for (IClientKeyListener listener : keyQueueListeners) {
            executor.execute(() -> listener.keystrokeQueueChanged(newKey, isDeleteEvent));
        }
    }

    /**
     * Notifies all the listeners of an analysis result queue change.
     *
     * @param newResult the newest analysis result
     * @param isDeleteEvent if the queue change was a result of a delete event instead of an enqueue event
     */
    public void notifyAnalysisResultQueueChange(AnalysisResultPojo newResult, boolean isDeleteEvent) {
        for (IClientAnalysisListener listener : analysisResultQueueListeners) {
            executor.execute(() -> listener.analysisResultQueueChanged(newResult, isDeleteEvent));
        }
    }

    /**
     * Notifies all the listeners of all the queues that the model has changed.
     */
    public void notifyModelChange() {
        notifyStatusChange(null, currentStatus, false);
        // notifyKeyQueueChange(allKeyStrokes.peekLast());
        // notifyAnalysisResultQueueChange(unsyncedAnalysisResults.peekLast());
    }
}
