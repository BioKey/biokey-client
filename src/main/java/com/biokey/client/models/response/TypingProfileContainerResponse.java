package com.biokey.client.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class TypingProfileContainerResponse {

    @JsonProperty("typingProfile")
    @NonNull private final TypingProfileResponse typingProfile;

    @JsonProperty("phoneNumber")
    private final String phoneNumber;

    @JsonProperty("googleAuthKey")
    private final String googleAuthKey;

    @JsonProperty("timeStamp")
    private final long timeStamp;
}
