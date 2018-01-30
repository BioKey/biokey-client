package com.biokey.client.constants;

import java.io.Serializable;

/**
 * Label for parts of the client state that indicates whether the server knows about changes to the state.
 */
public enum SyncStatusConstants {
    UNSYNCED, SYNCING, INSYNC
}
