package com.biokey.client.models.pojo;

import com.biokey.client.constants.SyncStatusConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

@Data
@JsonIgnoreProperties(value = { "syncedWithServer" })
public class KeyStrokesPojo implements Serializable {

    private static final long serialVersionUID = 500;

    @NonNull private Deque<KeyStrokePojo> keyStrokes = new LinkedBlockingDeque<>();
    @NonNull private SyncStatusConstants syncedWithServer = SyncStatusConstants.UNSYNCED;
}
