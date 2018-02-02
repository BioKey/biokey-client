package com.biokey.client.controllers;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.helpers.RequestBuilderHelper;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ClientStateControllerIntegrationTest {

    // Tests require an internet connection and assumes the server is up and running.
    // Tests rely on fake data to be hardcoded from the database.

    private static final ClientStateModel.IClientStatusListener STATUS_LISTENER = (ClientStatusPojo oldStatus, ClientStatusPojo newStatus) -> {};
    private static final Set<ClientStateModel.IClientStatusListener> STATUS_LISTENER_SET = new HashSet<>();
    private static final ClientStateModel.IClientKeyListener KEY_LISTENER = (KeyStrokePojo newKey) -> {};
    private static final Set<ClientStateModel.IClientKeyListener> KEY_LISTENER_SET = new HashSet<>();
    private static final ClientStateModel.IClientAnalysisListener ANALYSIS_LISTENER = (AnalysisResultPojo newResult) -> {};
    private static final Set<ClientStateModel.IClientAnalysisListener> ANALYSIS_LISTENER_SET = new HashSet<>();

   //private static final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1YTZmZWE3NTA3Y2EzYzNmNzQ3NzNmMjciLCJpYXQiOjE1MTcyOTkxNDMwODJ9.58EymCmHmHYRKDtb0sokO_OL4OaZfJVfwaETjiP7JgA";
    private static final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1YTcyODgzMzVlZjI3ZTcyNTQ0YmI0ZDQiLCJpYXQiOjE1MTc1MDI2MDM4MzN9.LNP-izZT0bc-KI6jtIpbfrYAsahkFP-61t1OO_G_WfY";
    private static final String TYPING_PROFILE_ID = "5a6fea7607ca3c3f74773f2b";
    private static final ClientStatusPojo CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo(TYPING_PROFILE_ID, "","","",new float[] {}, (String challenge) -> false,""),
                    AuthConstants.AUTHENTICATED, SecurityConstants.UNLOCKED,
                    ACCESS_TOKEN,
                    0,"");

    @Spy
    private static ClientStateModel state = new ClientStateModel();

    @Spy @InjectMocks
    private RequestBuilderHelper requestBuilderHelper = new RequestBuilderHelper();

    @Spy
    private ServerRequestExecutorHelper serverRequestExecutorHelper = new ServerRequestExecutorHelper(Executors.newCachedThreadPool());

    @InjectMocks
    private ClientStateController underTest = new ClientStateController();

    @BeforeClass
    public static void initListeners() {
        STATUS_LISTENER_SET.add(STATUS_LISTENER);
        KEY_LISTENER_SET.add(KEY_LISTENER);
        ANALYSIS_LISTENER_SET.add(ANALYSIS_LISTENER);
        state.setStatusListeners(STATUS_LISTENER_SET);
        state.setKeyQueueListeners(KEY_LISTENER_SET);
        state.setAnalysisResultQueueListeners(ANALYSIS_LISTENER_SET);
        state.obtainAccessToModel();
        state.enqueueKeyStroke(new KeyStrokePojo('t', true, 100));
        state.enqueueStatus(CLIENT_STATUS_POJO);
        state.releaseAccessToModel();
    }

    @Test
    public void GIVEN_realCallToServer_WHEN_sendKeyStrokes_THEN_success() throws Exception {
        underTest.sendKeyStrokes();
        verify(state, timeout(500).times(2)).releaseAccessToModel();
        verify(state, timeout(500).times(1)).dequeueSyncedKeyStrokes();
    }

    //TODO: fix test. Couldn't figure out how to write it now that confirmAccessToken takes a handler
   /*@Test
    public void GIVEN_realAccessToken_WHEN_confirmAccessToken_THEN_success() {
        underTest.confirmAccessToken();

        verify(state, timeout(500).times(2)).releaseAccessToStatus();
        verify(state, never()).enqueueStatus(any());
    }*/


}
