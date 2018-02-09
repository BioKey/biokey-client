package com.biokey.client.models.response;

import lombok.Data;

@Data
public class TypingProfileResponse {
    private final String _id;
    private final String user;
    private final String machine;
    private final boolean isLocked;
    private final String tensorFlowModel;
    private final String endpoint;
    private final String[] challengeStrategies;
    private final float[] threshold;
}
