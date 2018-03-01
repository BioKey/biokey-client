package com.biokey.client.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class UserContainerResponse {

    @JsonProperty("changeType")
    @NonNull private final String changeType;

    @JsonProperty("phoneNumber")
    private final String phoneNumber;

    @JsonProperty("googleAuthKey")
    private final String googleAuthKey;

}
