package com.biokey.client.models.pojo;

import com.biokey.client.controllers.challenges.IChallengeStrategy;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
public class TypingProfilePojo implements Serializable {

    private static final long serialVersionUID = 600;

    @NonNull private final String id;
    @NonNull private final String machineId;
    @NonNull private final String userId;
    @NonNull private final String model;
    @NonNull private final float[] threshold;
    @NonNull private IChallengeStrategy[] acceptedChallengeStrategies;
    @NonNull private final String sqsEndpoint;

}
