package com.biokey.client.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class TypingProfileContainerResponse {

    @JsonProperty("update")
    @NonNull private final TypingProfileResponse typingProfile;

    @JsonProperty("phoneNumber")
    private final String phoneNumber;

    @JsonProperty("googleAuthKey")
    private final String googleAuthKey;
}
