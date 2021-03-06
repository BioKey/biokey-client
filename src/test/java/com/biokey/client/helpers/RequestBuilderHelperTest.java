package com.biokey.client.helpers;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.models.pojo.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RequestBuilderHelperTest {

    private static final ClientStatusPojo CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo("1", "2","3", new EngineModelPojo(), new String[] {},"5"),
                    AuthConstants.AUTHENTICATED, SecurityConstants.UNLOCKED,
                    "6", "7", "8", 9);

    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String TYPING_PROFILE_ID = "TYPING_PROFILE_ID";
    private static final String EMAIL = "EMAIL";
    private static final String PASSWORD = "PASSWORD";

    private static final KeyStrokePojo KEY_STROKE_POJO = new KeyStrokePojo('t', true, 1);
    private static final KeyStrokePojo OTHER_KEY_STROKE_POJO = new KeyStrokePojo('b', false, 2);
    private static final KeyStrokesPojo KEY_STROKES_POJO = new KeyStrokesPojo();
    private static final AnalysisResultPojo ANALYSIS_RESULT_POJO = new AnalysisResultPojo(1, 0.1f);
    private static final AnalysisResultPojo OTHER_ANALYSIS_RESULT_POJO = new AnalysisResultPojo(2, 0.2f);
    private static final AnalysisResultsPojo ANALYSIS_RESULTS_POJO = new AnalysisResultsPojo();

    // TODO: not urgent but will need to update these expected
    private static final String EXPECTED_KEY_STROKE_JSON = "{\"keystrokes\": [{\"character\":116,\"keyDown\":true,\"timestamp\":1,\"typingProfile\":\"TYPING_PROFILE_ID\"},{\"character\":98,\"keyDown\":false,\"timestamp\":2,\"typingProfile\":\"TYPING_PROFILE_ID\"}]}";
    private static final String EXPECTED_LOGIN_JSON = "{\"email\": \"EMAIL\", \"password\": \"PASSWORD\"}";
    private static final String EXPECTED_CLIENT_STATUS_JSON = "{\"typingProfile\":{\"_id\":\"1\",\"user\":\"3\",\"machine\":\"2\",\"isLocked\":false,\"tensorFlowModel\":{\"gaussianProfile\":{}},\"endpoint\":\"5\",\"challengeStrategies\":[],\"locked\":false},\"phoneNumber\":\"7\",\"googleAuthKey\":\"8\",\"timeStamp\":9}";
    private static final String EXPECTED_ANALYSIS_RESULTS_JSON = "{\"analysisResults\": [{\"timestamp\":1,\"probability\":0.1,\"typingProfile\":\"TYPING_PROFILE_ID\"},{\"timestamp\":2,\"probability\":0.2,\"typingProfile\":\"TYPING_PROFILE_ID\"}]}";

    @BeforeClass
    public static void loadData() {
        KEY_STROKES_POJO.getKeyStrokes().add(KEY_STROKE_POJO);
        KEY_STROKES_POJO.getKeyStrokes().add(OTHER_KEY_STROKE_POJO);
        ANALYSIS_RESULTS_POJO.getAnalysisResults().add(ANALYSIS_RESULT_POJO);
        ANALYSIS_RESULTS_POJO.getAnalysisResults().add(OTHER_ANALYSIS_RESULT_POJO);
    }

    @Test
    public void GIVEN_accessToken_WHEN_headerMapWithToken_THEN_expectedResult() {
        assertTrue("header map does not have expected value for access token",
                RequestBuilderHelper.headerMapWithToken(ACCESS_TOKEN).getFirst(AppConstants.SERVER_TOKEN_HEADER).equals(ACCESS_TOKEN));
    }

    @Test
    public void GIVEN_accessToken_WHEN_emptyHeaderMap_THEN_expectedResult() {
        assertTrue("header map is not empty other than charset", RequestBuilderHelper.emptyHeaderMap().size() == 1);
    }

    @Test
    public void GIVEN_input_WHEN_requestBodyToPostKeystrokes_THEN_expectedResult() throws JsonProcessingException {
        System.out.println(RequestBuilderHelper.requestBodyToPostKeystrokes(KEY_STROKES_POJO, TYPING_PROFILE_ID));
        assertTrue("generated JSON does not match expected JSON",
                RequestBuilderHelper.requestBodyToPostKeystrokes(KEY_STROKES_POJO, TYPING_PROFILE_ID).equals(EXPECTED_KEY_STROKE_JSON));
    }

    @Test
    public void GIVEN_input_WHEN_requestBodyToPostLogin_THEN_expectedResult() {
        assertTrue("generated JSON does not match expected JSON",
                RequestBuilderHelper.requestBodyToPostLogin(EMAIL, PASSWORD).equals(EXPECTED_LOGIN_JSON));
    }

    @Test
    public void GIVEN_input_WHEN_requestBodyToPostClientStatus_THEN_expectedResult() throws JsonProcessingException {
        System.out.println(RequestBuilderHelper.requestBodyToPostClientStatus(CLIENT_STATUS_POJO));
        assertTrue("generated JSON does not match expected JSON",
                RequestBuilderHelper.requestBodyToPostClientStatus(CLIENT_STATUS_POJO).equals(EXPECTED_CLIENT_STATUS_JSON));
    }

    @Test
    public void GIVEN_input_WHEN_requestBodyToPostAnalysisResult_THEN_expectedResult() throws JsonProcessingException {
        System.out.println(RequestBuilderHelper.requestBodyToPostAnalysisResults(ANALYSIS_RESULTS_POJO, TYPING_PROFILE_ID));
        assertTrue("generated JSON does not match expected JSON",
                RequestBuilderHelper.requestBodyToPostAnalysisResults(ANALYSIS_RESULTS_POJO, TYPING_PROFILE_ID).equals(EXPECTED_ANALYSIS_RESULTS_JSON));
    }

}
