package com.biokey.client.models.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

@Data
@JsonIgnoreProperties(value = {"model", "weights", "reset"})
public class EngineModelPojo implements Serializable {

    private static final long serialVersionUID = 240;

    @JsonProperty(value = "model")
    private String model;
    @JsonProperty(value = "weights")
    private String weights;
    @JsonProperty(value = "reset")
    private String resetWeights;
    @JsonProperty(value = "gaussianProfile")
    private HashMap<String, GaussianFeaturePojo> gaussianProfile = new HashMap<>();
}
