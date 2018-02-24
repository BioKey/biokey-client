package com.biokey.client.models.response;

import lombok.Data;
import lombok.NonNull;

@Data
public class TypingProfileContainerResponse {
    @NonNull private final TypingProfileResponse typingProfile;
    private final String phoneNumber;
    private final String googleAuthKey;
}
