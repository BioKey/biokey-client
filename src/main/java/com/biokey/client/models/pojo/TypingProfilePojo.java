package com.biokey.client.models.pojo;

import com.biokey.client.controllers.challenges.IChallengeStrategy;
import lombok.Data;
import lombok.NonNull;

@Data
public class TypingProfilePojo {

    @NonNull private final String machineId;
    @NonNull private final String userId;
    @NonNull private final String model;
    @NonNull private final float[] threshold;
    @NonNull private IChallengeStrategy acceptedChallengeStrategies;

}
