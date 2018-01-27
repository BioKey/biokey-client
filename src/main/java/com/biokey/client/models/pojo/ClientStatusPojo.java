package com.biokey.client.models.pojo;

import com.biokey.client.constants.StatusConstants;
import com.biokey.client.constants.SyncStatusConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NonNull;

@Data
@JsonIgnoreProperties(value = { "syncedWithServer" })
public class ClientStatusPojo {

    @NonNull private final TypingProfilePojo profile;
    @NonNull private final StatusConstants topLevelStatus;
    @NonNull private final String accessToken;
    private final long timeStamp;
    @NonNull private SyncStatusConstants syncedWithServer = SyncStatusConstants.UNSYNCED;

}
