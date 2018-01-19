package com.biokey.client.models.pojo;

import com.biokey.client.constants.SyncStatusConstants;
import lombok.Data;
import lombok.NonNull;

@Data
public class KeyStrokePojo {

    @NonNull private final char key;
    @NonNull private final boolean keyDown;
    @NonNull private final long timeStamp;
    @NonNull private SyncStatusConstants syncedWithServer = SyncStatusConstants.UNSYNCED;

}
