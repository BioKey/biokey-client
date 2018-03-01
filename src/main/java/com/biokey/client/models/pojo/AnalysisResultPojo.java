package com.biokey.client.models.pojo;

import com.biokey.client.constants.SyncStatusConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
@JsonAppend(attrs = {
        @JsonAppend.Attr(value = "typingProfile")
})
@JsonIgnoreProperties(value = { "syncedWithServer" })
public class AnalysisResultPojo implements Serializable {

    private static final long serialVersionUID = 300;

    private final long timeStamp;
    private final float probability;
    @NonNull private SyncStatusConstants syncedWithServer = SyncStatusConstants.UNSYNCED;

}
