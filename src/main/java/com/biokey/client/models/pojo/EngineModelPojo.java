package com.biokey.client.models.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(value = { "model", "weights" })
public class EngineModelPojo implements Serializable {
    @JsonProperty("gaussianProfile")
    private HashMap<String, GaussianFeaturePojo> gaussianProfile = new HashMap<>();
}
