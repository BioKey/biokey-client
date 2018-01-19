package com.biokey.client.models.pojo;

import com.biokey.client.constants.SyncStatusConstants;
import lombok.Data;
import lombok.NonNull;

@Data
public class AnalysisResultPojo {

    @NonNull private final long timeStamp;
    @NonNull private final float probability;
    @NonNull private SyncStatusConstants syncedWithServer = SyncStatusConstants.UNSYNCED;

}
