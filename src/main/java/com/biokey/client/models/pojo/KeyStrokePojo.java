package com.biokey.client.models.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonAppend(attrs = {
        @JsonAppend.Attr(value = "typingProfile")
})
public class KeyStrokePojo implements Serializable {

    private static final long serialVersionUID = 260;

    @JsonProperty("character")
    private final int key;
    @JsonProperty("keyDown")
    private final boolean keyDown;
    @JsonProperty("timestamp")
    private final long timeStamp;

}
