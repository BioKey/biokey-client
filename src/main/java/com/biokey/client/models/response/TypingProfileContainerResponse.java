package com.biokey.client.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
