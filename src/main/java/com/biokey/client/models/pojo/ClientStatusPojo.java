package com.biokey.client.models.pojo;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.constants.SyncStatusConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = { "syncedWithServer" })
public class ClientStatusPojo implements Serializable {

    private static final long serialVersionUID = 200;

    @NonNull private final TypingProfilePojo profile;
    @NonNull private final AuthConstants authStatus;
    @NonNull private final SecurityConstants securityStatus;
    private final String accessToken;
    @NonNull private final long timeStamp;
    @NonNull private SyncStatusConstants syncedWithServer = SyncStatusConstants.UNSYNCED;
    private final String phoneNumber;

}
