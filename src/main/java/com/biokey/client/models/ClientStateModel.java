package com.biokey.client.models;

import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.KeyStrokesPojo;
import lombok.NonNull;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.security.AccessControlException;
import java.util.Deque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Model serves as the single source of truth for all client state information.
 * State can be observed by any service who registers a listener.
 */
public class ClientStateModel implements Serializable {

    static Logger log = Logger.getLogger(ClientStateModel.class);

    /**
     * Interface describing the contract for listeners to the client state.
     * Listeners will be notified when a new status has been added.
     */
    public interface IClientStateListener {
        void stateChanged(ClientStatusPojo oldStatus, ClientStatusPojo newStatus);
    }

    /**
     * Interface describing the contract for listeners to the unsynced status queue.
     * Listeners will be notified when the queue is modified.
     */
    public interface IClientStatusQueueListener {
        void statusQueueChanged();
    }

    /**
     * Interface describing the contract for listeners to the keystroke queues.
     * Listeners will be notified when the queues are modified.
     */
    public interface IClientKeyListener {
        void keystrokeQueueChanged();
    }

    /**
     * Interface describing the contract for listeners to the analysis result queue.
     * Listeners will be notified when the queue is modified.
     */
    public interface IClientAnalysisListener {
        void analysisResultQueueChanged();
    }

    private ClientStatusPojo currentStatus;
    private final Queue<ClientStatusPojo> unsyncedStatuses = new LinkedBlockingQueue<>();
    private final Queue<AnalysisResultPojo> unsyncedAnalysisResults = new LinkedBlockingQueue<>();
    private final Deque<KeyStrokesPojo> unsyncedKeyStrokes = new LinkedBlockingDeque<>();
    private final Queue<KeyStrokePojo> allKeyStrokes = new LinkedBlockingQueue<>(); // need a record of all keyStrokes for the analysis engine

    private final ReentrantLock statusLock = new ReentrantLock(true);
    private final ReentrantLock analysisResultLock = new ReentrantLock(true);
    private final ReentrantLock keyStrokesLock = new ReentrantLock(true);

    @Setter @NonNull
    private Set<IClientStateListener> stateListeners;

    @Setter @NonNull
    private Set<IClientStatusQueueListener> statusQueueListeners;

    @Setter @NonNull
    private Set<IClientKeyListener> keyQueueListeners;

    @Setter @NonNull
    private Set<IClientAnalysisListener> analysisResultQueueListeners;

    /**
     * Obtain access to status. If a thread is not holding this lock, it can not get or modify the status.
     * A thread that has called this method must make sure to release the lock through a finally block.
     */
    public void obtainAccessToStatus() {
        statusLock.lock();
    }

    /**
     * Release access to get and modify status.
     */
    public void releaseAccessToStatus() {
        statusLock.unlock();
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
     * Getter method for the current status.
     *
     * @return current status
     */
    public ClientStatusPojo getCurrentStatus() {
        if (!statusLock.isHeldByCurrentThread()) throw new AccessControlException("statusLock needs to be acquired by the thread");
        return currentStatus;
    }

    /**
     * Setter method for the status.
     */
    public void setStatus(ClientStatusPojo status) {
        if (!statusLock.isHeldByCurrentThread()) throw new AccessControlException("statusLock needs to be acquired by the thread");
        currentStatus = status;
        return;
    }

    /**
     * Add a new (immutable) status to unsynced queue.
     *
     * @param status new status to add to queue
     */
    public void enqueueStatus(@NonNull ClientStatusPojo status) {
        if (!statusLock.isHeldByCurrentThread()) throw new AccessControlException("statusLock needs to be acquired by the thread");
        unsyncedStatuses.add(status);
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
     * Dequeue the oldest bundle of key strokes from the unsynced queue.
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
     * Peek at the oldest bundle of key strokes from the unsynced queue.
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
     */
    public void notifyStatusChange(ClientStatusPojo oldStatus, ClientStatusPojo newStatus) {

        for (IClientStateListener listener : stateListeners) {
             Runnable r = () -> listener.stateChanged(oldStatus, newStatus);
             r.run();
        }
    }

    /**
     * Notifies all the listeners of a status queue change.
     */
    public void notifyStatusQueueChange() {
        for (IClientStatusQueueListener listener : statusQueueListeners) {
            Runnable r = () -> listener.statusQueueChanged();
            r.run();
        }
    }

    /**
     * Notifies all the listeners of a key queue change.
     */
    public void notifyKeyQueueChange() {
        for (IClientKeyListener listener : keyQueueListeners) {
            Runnable r = () -> listener.keystrokeQueueChanged();
            r.run();
        }
    }

    /**
     * Notifies all the listeners of an analysis result queue change.
     */
    public void notifyAnalysisResultQueueChange() {
        for (IClientAnalysisListener listener : analysisResultQueueListeners) {
            Runnable r = () -> listener.analysisResultQueueChanged();
            r.run();
        }
    }
    
}
