package com.biokey.client.constants;

public class EngineConstants {
    public static final int SEQ_THRESHOLD = 150;

    public static final int MODEL_SERVER_PORT = 4674;
    public static final String MODEL_SERVER_URL = "http://127.0.0.1:"+MODEL_SERVER_PORT;
    public static final String INIT_ENDPOINT = MODEL_SERVER_URL + "/init";
    public static final String PREDICTION_ENDPOINT = MODEL_SERVER_URL + "/prediction";
}
