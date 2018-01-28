package com.biokey.client.models.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KeyStrokePojo {

    @JsonProperty("character")
    private final char key;
    @JsonProperty("keyDown")
    private final boolean keyDown;
    @JsonProperty("timestamp")
    private final long timeStamp;

}
