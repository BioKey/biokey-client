package com.biokey.client.models.response;

import lombok.Data;

@Data
public class TypingProfileResponse {
    private final String _id;
    private final String user;
    private final String machine;
    private final String accessToken;
    private final String tensorFlowModel;
    private final String endpoint;
    private final String _v;
    private final boolean lockStatus;
    private final boolean authStatus;
}
