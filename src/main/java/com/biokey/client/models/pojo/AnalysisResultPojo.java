package com.biokey.client.models.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonAppend(attrs = {
        @JsonAppend.Attr(value = "typingProfile")
})
@JsonIgnoreProperties(value = { "syncedWithServer" })
public class AnalysisResultPojo implements Serializable {

    private static final long serialVersionUID = 200;

    @JsonProperty("timestamp")
    private final long timeStamp;
    @JsonProperty("probability")
    private final float probability;
}
