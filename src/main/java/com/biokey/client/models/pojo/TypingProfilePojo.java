package com.biokey.client.models.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
public class TypingProfilePojo implements Serializable {

    private static final long serialVersionUID = 280;

    @NonNull private final String id;
    @NonNull private final String machineId;
    @NonNull private final String userId;
    @NonNull private EngineModelPojo model;
    @NonNull private String[] acceptedChallengeStrategies;
    @NonNull private final String sqsEndpoint;

}
