package com.biokey.client.models.response;

import com.biokey.client.models.pojo.EngineModelPojo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private final EngineModelPojo tensorFlowModel;
    @JsonProperty("endpoint")
    private final String endpoint;
    @JsonProperty("challengeStrategies")
    private final String[] challengeStrategies;
}
