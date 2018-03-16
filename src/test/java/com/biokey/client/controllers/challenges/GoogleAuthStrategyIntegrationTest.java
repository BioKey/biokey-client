package com.biokey.client.controllers.challenges;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.EngineModelPojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import com.biokey.client.views.frames.GoogleAuthQRFrameView;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GoogleAuthStrategyIntegrationTest {

    private final ClientStatusPojo CLIENT_STATUS_POJO_NO_KEY =
            new ClientStatusPojo(
                    new TypingProfilePojo("", "", "", new EngineModelPojo(), new String[] {}, ""),
                    AuthConstants.AUTHENTICATED, SecurityConstants.UNLOCKED,
                    "", "", "", 0);
    private final ClientStatusPojo CLIENT_STATUS_POJO_WITH_KEY =
            new ClientStatusPojo(
                    new TypingProfilePojo("", "", "", new EngineModelPojo(), new String[] {}, ""),
                    AuthConstants.AUTHENTICATED, SecurityConstants.UNLOCKED,
                    "", "", "hello", 0);
    private final int TIMEOUT = 2000;

    private ClientStateModel state;
    private ClientStateController controller;

    @Spy
    private final ServerRequestExecutorHelper helper = new ServerRequestExecutorHelper(Executors.newCachedThreadPool());
    @Mock
    private GoogleAuthQRFrameView view;

    private GoogleAuthStrategy underTest;

    @Before
    public void init() {
        state = new ClientStateModel(Executors.newCachedThreadPool());
        controller = spy(new ClientStateController(state, helper));
        underTest = new GoogleAuthStrategy(state, controller, helper, view);
        doNothing().when(view).displayImage(any(byte[].class), any());
    }

    @Test
    public void GIVEN_withKey_WHEN_init_THEN_notInitialized() {
        state.obtainAccessToModel();
        state.enqueueStatus(CLIENT_STATUS_POJO_WITH_KEY);

        underTest.init();

        verify(controller, never()).setGoogleAuthKey(any());
        verify(helper, never()).submitGetRequest(any(), any(), any(), any());
        assertTrue("strategy should not be initialized", !underTest.isInitialized());
    }

    @Test
    public void GIVEN_noStatus_WHEN_init_THEN_notInitialized() {
        state.obtainAccessToModel();
        state.clear();

        underTest.init();

        verify(controller, never()).setGoogleAuthKey(any());
        verify(helper, never()).submitGetRequest(any(), any(), any(), any());
        assertTrue("strategy should not be initialized", !underTest.isInitialized());
    }

    @Test
    public void GIVEN_noKey_WHEN_init_THEN_initialized() {
        state.obtainAccessToModel();
        state.enqueueStatus(CLIENT_STATUS_POJO_NO_KEY);

        underTest.init();

        verify(controller, timeout(TIMEOUT).times(1)).setGoogleAuthKey(any());
        verify(view, timeout(TIMEOUT).times(1)).displayImage(any(byte[].class), any());
        assertTrue("strategy should be initialized", underTest.isInitialized());
    }

    @Test
    public void GIVEN_notInitialized_WHEN_checkChallenge_THEN_false() {
        state.obtainAccessToModel();
        state.enqueueStatus(CLIENT_STATUS_POJO_WITH_KEY);

        assertTrue("not initialized should not pass check challenge", !underTest.checkChallenge("1"));
    }

    @Test
    public void GIVEN_noStatus_WHEN_checkChallenge_THEN_false() {
        GIVEN_noKey_WHEN_init_THEN_initialized();

        state.obtainAccessToModel();
        state.clear();
        assertTrue("no status should not pass check challenge", !underTest.checkChallenge("1"));
    }

    @Test
    public void GIVEN_noKey_WHEN_checkChallenge_THEN_false() {
        GIVEN_noKey_WHEN_init_THEN_initialized();

        state.obtainAccessToModel();
        state.enqueueStatus(CLIENT_STATUS_POJO_NO_KEY);
        assertTrue("no key should not pass check challenge", !underTest.checkChallenge("1"));
    }

    @Test
    public void GIVEN_initialized_WHEN_checkChallenge_THEN_success() {
        state.obtainAccessToModel();
        state.enqueueStatus(CLIENT_STATUS_POJO_NO_KEY);

        underTest.init();

        // Capture the key generated.
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(controller, timeout(TIMEOUT).times(1)).setGoogleAuthKey(captor.capture());

        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        assertTrue("challenge should match key generated attempt",
                underTest.checkChallenge(Integer.toString(gAuth.getTotpPassword(captor.getValue()))));

    }

    @Test
    public void GIVEN_alphaString_WHEN_checkChallenge_THEN_false() {
        GIVEN_noKey_WHEN_init_THEN_initialized();

        assertTrue("alphabetical letters should not pass check challenge", !underTest.checkChallenge("hi"));
    }
}
