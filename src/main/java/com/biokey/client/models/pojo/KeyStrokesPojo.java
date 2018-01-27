package com.biokey.client.models.pojo;

import com.biokey.client.constants.SyncStatusConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NonNull;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Data
@JsonIgnoreProperties(value = { "syncedWithServer" })
public class KeyStrokesPojo {

    @NonNull private Queue<KeyStrokePojo> keyStrokes = new LinkedBlockingQueue<>();
    @NonNull private SyncStatusConstants syncedWithServer = SyncStatusConstants.UNSYNCED;
}
