package com.biokey.client.models.pojo;

import com.biokey.client.constants.SyncStatusConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = { "syncedWithServer" })
public class AnalysisResultPojo implements Serializable {

    private static final long serialVersionUID = 300;

    private final long timeStamp;
    private final float probability;
    @NonNull private SyncStatusConstants syncedWithServer = SyncStatusConstants.UNSYNCED;

}
