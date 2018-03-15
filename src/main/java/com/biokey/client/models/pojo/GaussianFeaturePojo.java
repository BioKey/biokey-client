package com.biokey.client.models.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GaussianFeaturePojo implements Serializable {
    @JsonProperty("mean")
    private double mean;
    @JsonProperty("stdev")
    private double stdev;
}
