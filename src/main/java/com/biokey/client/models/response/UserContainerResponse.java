package com.biokey.client.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class UserContainerResponse {

    @JsonProperty("userChangeType")
    @NonNull private final String changeType;

    @JsonProperty("phoneNumber")
    private final String phoneNumber;

    @JsonProperty("googleAuthKey")
    private final String googleAuthKey;

}
