package com.biokey.client.controllers.challenges;

import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.views.frames.GoogleAuthFrameView;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GoogleAuthStrategyIntegrationTest {
    @Spy
    private ClientStateModel state;
    private final ServerRequestExecutorHelper helper = new ServerRequestExecutorHelper(Executors.newCachedThreadPool());
    @Mock
    private GoogleAuthFrameView view;

    private GoogleAuthStrategy underTest;

    @Before
    public void init() {
        underTest = new GoogleAuthStrategy(state, helper, view);
    }

    @Test
    public void GIVEN_na_WHEN_init_THEN_initialized() {
        doNothing().when(state).setGKey(any());
        doNothing().when(view).displayImage(any(byte[].class), any());
        underTest.init();

        verify(state).setGKey(any());
        verify(view, timeout(1000).times(1)).displayImage(any(byte[].class), any());
        assertTrue("strategy should be initialized", underTest.isInitialized());
    }

    @Test
    public void GIVEN_initialized_WHEN_checkChallenge_THEN_success() {
        doNothing().when(state).setGKey(any());
        underTest.init();

        // Capture the key generated.
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(state).setGKey(captor.capture());

        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        assertTrue("challenge should match key generated attempt",
                underTest.checkChallenge(Integer.toString(gAuth.getTotpPassword(captor.getValue()))));

    }

    @Test
    public void GIVEN_alphaString_WHEN_checkChallenge_THEN_false() {
        doNothing().when(state).setGKey(any());
        underTest.init();
        assertTrue("alphabetical letters should not pass check challenge", !underTest.checkChallenge("hi"));
    }
}
