package com.biokey.client.models.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GaussianFeaturePojo implements Serializable {

    private static final long serialVersionUID = 250;

    @JsonProperty("mean")
    private double mean;
    @JsonProperty("stdev")
    private double stdev;
    @JsonProperty("i")
    private int index;
}
