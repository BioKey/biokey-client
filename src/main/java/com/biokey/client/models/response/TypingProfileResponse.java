package com.biokey.client.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TypingProfileResponse {
    @JsonProperty("_id")
    private final String _id;
    @JsonProperty("user")
    private final String user;
    @JsonProperty("machine")
    private final String machine;
    @JsonProperty("isLocked")
    private final boolean isLocked;
    @JsonProperty("tensorFlowModel")
    private final String tensorFlowModel;
    @JsonProperty("endpoint")
    private final String endpoint;
    @JsonProperty("challengeStrategies")
    private final String[] challengeStrategies;
    @JsonProperty("threshold")
    private final float[] threshold;
}
