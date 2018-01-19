package com.biokey.client.models.pojo;

import com.biokey.client.constants.StatusConstants;
import com.biokey.client.constants.SyncStatusConstants;
import lombok.Data;
import lombok.NonNull;

@Data
public class ClientStatusPojo {

    @NonNull private final TypingProfilePojo profile;
    @NonNull private final StatusConstants topLevelStatus;
    @NonNull private final String accessToken;
    @NonNull private SyncStatusConstants syncedWithServer;

}
