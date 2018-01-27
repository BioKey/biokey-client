package com.biokey.client.models.pojo;

import com.biokey.client.constants.SyncStatusConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NonNull;

@Data
@JsonIgnoreProperties(value = { "syncedWithServer" })
public class AnalysisResultPojo {

    private final long timeStamp;
    private final float probability;
    @NonNull private SyncStatusConstants syncedWithServer = SyncStatusConstants.UNSYNCED;

}
