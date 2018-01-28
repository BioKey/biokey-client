package com.biokey.client.models.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import lombok.Data;

@Data
@JsonAppend(attrs = {
        @JsonAppend.Attr(value = "typingProfile")
})
public class KeyStrokePojo {

    @JsonProperty("character")
    private final char key;
    @JsonProperty("keyDown")
    private final boolean keyDown;
    @JsonProperty("timestamp")
    private final long timeStamp;

}
