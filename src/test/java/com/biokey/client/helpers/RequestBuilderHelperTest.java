package com.biokey.client.helpers;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.constants.AuthConstants;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.KeyStrokesPojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
public class RequestBuilderHelperTest {

    @InjectMocks
    private RequestBuilderHelper underTest;

    @Mock
    private ClientStateModel state;

    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String TYPING_PROFILE_ID = "TYPING_PROFILE_ID";
    private static final ClientStatusPojo CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo(TYPING_PROFILE_ID, "","","",new float[] {}, (String challenge) -> false),
                    AuthConstants.AUTHENTICATED, SecurityConstants.UNLOCKED,
                    ACCESS_TOKEN,
                    0);

    private static final KeyStrokePojo KEY_STROKE_POJO = new KeyStrokePojo('t', true, 1);
    private static final KeyStrokePojo OTHER_KEY_STROKE_POJO = new KeyStrokePojo('b', false, 2);
    private static final KeyStrokesPojo KEY_STROKES_POJO = new KeyStrokesPojo();

    private static final String EXPECTED_KEY_STROKE_JSON = "{\"keystrokes\": [{\"character\":\"t\",\"keyDown\":true,\"timestamp\":1,\"typingProfile\":\"TYPING_PROFILE_ID\"},{\"character\":\"b\",\"keyDown\":false,\"timestamp\":2,\"typingProfile\":\"TYPING_PROFILE_ID\"}]}";

    @BeforeClass
    public static void loadData() {
        KEY_STROKES_POJO.getKeyStrokes().add(KEY_STROKE_POJO);
        KEY_STROKES_POJO.getKeyStrokes().add(OTHER_KEY_STROKE_POJO);
    }

    @Test
    public void GIVEN_accessToken_WHEN_headerMapWithToken_THEN_expectedResult() {
        Mockito.when(state.getCurrentStatus()).thenReturn(CLIENT_STATUS_POJO);
        assertTrue("header map has expected value for access token",
                underTest.headerMapWithToken().getFirst(AppConstants.SERVER_TOKEN_HEADER).equals(ACCESS_TOKEN));
    }

    @Test
    public void GIVEN_keyStrokesAndTypingProfile_WHEN_headerMapWithToken_THEN_expectedResult() throws JsonProcessingException {
        Mockito.when(state.getCurrentStatus()).thenReturn(CLIENT_STATUS_POJO);
        assertTrue("header map has expected value for access token",
                underTest.requestBodyToPostKeystrokes(KEY_STROKES_POJO).equals(EXPECTED_KEY_STROKE_JSON));
    }
}