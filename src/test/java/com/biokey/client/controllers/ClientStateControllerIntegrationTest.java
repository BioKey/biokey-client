package com.biokey.client.controllers;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.constants.SyncStatusConstants;
import com.biokey.client.helpers.PojoHelper;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.*;
import com.biokey.client.models.response.LoginResponse;
import com.biokey.client.models.response.TypingProfileContainerResponse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.biokey.client.constants.AppConstants.KEYSTROKE_WINDOW_SIZE_PER_REQUEST;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ClientStateControllerIntegrationTest {

    // Tests require an internet connection and assumes the server is up and running.
    // Tests rely on fake data to be hardcoded from the database.

    private CountDownLatch testCompleteFlag;
    private final int TEST_TIMEOUT = 2000;

    private static final ClientStateModel.IClientStatusListener STATUS_LISTENER = (ClientStatusPojo oldStatus, ClientStatusPojo newStatus) -> {};
    private static final ClientStateModel.IClientKeyListener KEY_LISTENER = (KeyStrokePojo newKey) -> {};
    private static final ClientStateModel.IClientAnalysisListener ANALYSIS_LISTENER = (AnalysisResultPojo newResult) -> {};

    // TODO: generate the access token and other valid results BeforeClass instead of hardcoding.
    private static final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1YTk1YWExYWExYzE1ZDE3YzBjZGZkOTciLCJpYXQiOjE1MTk3NTgwNjIwNDF9.iFo7oEqpbj4z_OEd8shjdl5CfaEuTe6MnNJzB3NrSYM";
    private static final String TYPING_PROFILE_ID = "5a95aa3745e7141fcc55a9a0";
    private static final String EMAIL = "a@a.com";
    private static final String PASSWORD = "a";
    private static final String MAC = "ABC";
    private static final String UNKNOWN_MAC = "!@#";
    private static final ClientStatusPojo CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo(TYPING_PROFILE_ID, MAC,"",null,new float[] {}, new String[] {},""),
                    AuthConstants.AUTHENTICATED, SecurityConstants.UNLOCKED,
                    ACCESS_TOKEN, "", "", 0);
    private static final KeyStrokePojo KEY_STROKE_POJO = new KeyStrokePojo('t', true, Integer.MAX_VALUE);
    private static final KeyStrokePojo OLD_KEY_STROKE_POJO = new KeyStrokePojo('t', true, 0);
    private static final AnalysisResultPojo ANALYSIS_RESULT_POJO = new AnalysisResultPojo(1, 0.1f);

    @Spy
    private static ClientStateModel state = new ClientStateModel(Executors.newCachedThreadPool());
    private static ClientStateModel initialState;

    @Spy
    private ServerRequestExecutorHelper serverRequestExecutorHelper = new ServerRequestExecutorHelper(Executors.newCachedThreadPool());

    private ClientStateController underTest;

    @BeforeClass
    public static void setupListeners() {
        // Set up listeners.
        Set<ClientStateModel.IClientStatusListener> statusListenerSet = new HashSet<>();
        Set<ClientStateModel.IClientKeyListener> keyListenerSet = new HashSet<>();
        Set<ClientStateModel.IClientAnalysisListener> analysisListenerSet = new HashSet<>();
        statusListenerSet.add(STATUS_LISTENER);
        keyListenerSet.add(KEY_LISTENER);
        analysisListenerSet.add(ANALYSIS_LISTENER);
        state.setStatusListeners(statusListenerSet);
        state.setKeyQueueListeners(keyListenerSet);
        state.setAnalysisResultQueueListeners(analysisListenerSet);
    }

    @Before
    public void resetState() {
        // Reset countdown.
        testCompleteFlag = new CountDownLatch(1);

        try {
            // Set up initial state.
            initialState = new ClientStateModel(Executors.newCachedThreadPool());
            initialState.obtainAccessToModel();
            initialState.enqueueStatus(CLIENT_STATUS_POJO);

            state.obtainAccessToModel();
            state.loadStateFromMemory(initialState);
            underTest = new ClientStateController(state, serverRequestExecutorHelper);
        } finally {
            initialState.releaseAccessToModel();
            state.releaseAccessToModel();
            reset(state);
        }
    }

    private void waitForCompletion() {
        try {
            if (!testCompleteFlag.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS)) {
                System.out.println("Test timed out.");
                fail();
            }
        } catch (InterruptedException e) {
            System.out.println("Unexpected interruption.");
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void GIVEN_realCallToServer_WHEN_sendKeyStrokes_THEN_success() {
        underTest.sendKeyStrokes();
        /*
        state.obtainAccessToModel();
        state.enqueueKeyStroke(KEY_STROKE_POJO);
        state.releaseAccessToModel();

        underTest.sendKeyStrokes();
        verify(serverRequestExecutorHelper).submitPostRequest(any(), any(), any(), any(), any());
        verify(state, timeout(TEST_TIMEOUT).times(2)).releaseAccessToModel();
        verify(state, timeout(TEST_TIMEOUT).times(1)).dequeueSyncedKeyStrokes();
        */
    }

    @Test
    public void GIVEN_realCallToServer_WHEN_sendLoginRequest_THEN_success() {
        // Confirmed this works but because of server side checks for integrity, the test will never pass.
        /*
        underTest.sendLoginRequest(EMAIL, PASSWORD, (ResponseEntity<LoginResponse> response) -> {
            assertTrue("should have received 200 response", response.getStatusCodeValue() == 200);
            testCompleteFlag.countDown();
        });

        waitForCompletion();
        verify(serverRequestExecutorHelper).submitPostRequest(any(), any(), any(), any(), any());
        verify(state).releaseAccessToStatus();
        */
    }

    @Test
    public void GIVEN_realCallToServer_WHEN_sendClientStatus_THEN_success() {
        state.obtainAccessToModel();
        state.enqueueStatus(CLIENT_STATUS_POJO);
        state.releaseAccessToModel();

        underTest.sendClientStatus();
        verify(serverRequestExecutorHelper).submitPutRequest(any(), any(), any(), any(), any());
        verify(state, timeout(TEST_TIMEOUT).times(2)).releaseAccessToStatus();
        verify(state, timeout(TEST_TIMEOUT).times(1)).dequeueStatus();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void GIVEN_realCallToServer_WHEN_sendAnalysisResult_THEN_success() {
        underTest.sendAnalysisResult();
        /*
        state.obtainAccessToModel();
        state.enqueueAnalysisResult(ANALYSIS_RESULT_POJO);
        state.releaseAccessToModel();

        underTest.sendAnalysisResult();
        verify(serverRequestExecutorHelper).submitPostRequest(any(), any(), any(), any(), any());
        verify(state, timeout(TEST_TIMEOUT).times(2)).releaseAccessToModel();
        verify(state, timeout(TEST_TIMEOUT).times(1)).dequeueAnalysisResult();
        */
    }

    @Test
    public void GIVEN_loggedIn_WHEN_sendHeartbeat_THEN_success () {
        underTest.sendHeartbeat(TYPING_PROFILE_ID,(ResponseEntity<String> response) -> {
            assertTrue("should have received 200 response", response.getStatusCodeValue() == 200);
            testCompleteFlag.countDown();
        });

        waitForCompletion();
        verify(serverRequestExecutorHelper).submitPostRequest(any(), any(), any(), any(), any());
        verify(state).releaseAccessToStatus();
    }

    @Test
    public void GIVEN_realCallToServer_WHEN_retrieveStatusFromServer_THEN_success() {
        underTest.retrieveStatusFromServer(MAC, ACCESS_TOKEN, (ResponseEntity<TypingProfileContainerResponse> response) -> {
            assertTrue("should have received 200 response", response.getStatusCodeValue() == 200);
            testCompleteFlag.countDown();
        });

        waitForCompletion();
        verify(serverRequestExecutorHelper).submitPostRequest(any(), any(), any(), any(), any());
        verify(state).releaseAccessToStatus();
    }

    @Test
    public void GIVEN_realCallToServerWithUnknownMAC_WHEN_retrieveStatusFromServer_THEN_success() {
        underTest.retrieveStatusFromServer(UNKNOWN_MAC, ACCESS_TOKEN, (ResponseEntity<TypingProfileContainerResponse> response) -> {
            assertTrue("should have received 200 response", response.getStatusCodeValue() == 200);
            testCompleteFlag.countDown();
        });

        waitForCompletion();
        verify(serverRequestExecutorHelper).submitPostRequest(any(), any(), any(), any(), any());
        verify(state).releaseAccessToStatus();
    }

    @Test
    public void GIVEN_realAccessToken_WHEN_confirmAccessToken_THEN_success() {
        underTest.confirmAccessToken((ResponseEntity<String> response) -> {
            assertTrue("should have received 200 response", response.getStatusCodeValue() == 200);
            testCompleteFlag.countDown();
        });

        waitForCompletion();
        verify(serverRequestExecutorHelper).submitGetRequest(any(), any(), any(), any());
        verify(state).releaseAccessToStatus();
    }

    @Test
    public void GIVEN_noStatus_WHEN_confirmAccessToken_THEN_nullResponse() {
        underTest.clearModel();
        underTest.confirmAccessToken((ResponseEntity<String> response) -> {
            assertTrue("should have received null response", response == null);
            testCompleteFlag.countDown();
        });

        waitForCompletion();
        verify(serverRequestExecutorHelper, never()).submitGetRequest(any(), any(), any(), any());
        verify(state).releaseAccessToStatus();
    }

    @Test
    public void GIVEN_manyKeystrokes_WHEN_enqueueKeyStroke_THEN_dividesCorrectly() {
        int numEnqueues = 2 * KEYSTROKE_WINDOW_SIZE_PER_REQUEST + 1;
        for (int i = 0; i < numEnqueues; i++) {
            underTest.enqueueKeyStroke(KEY_STROKE_POJO);
        }
        try {
            state.obtainAccessToKeyStrokes();
            assertTrue("newest division of keystrokes should only have one keystroke",
                    state.getNewestKeyStrokes().getKeyStrokes().size() == 1);
            verify(state, times(numEnqueues)).notifyKeyQueueChange(KEY_STROKE_POJO);
            verify(state, times(numEnqueues)).releaseAccessToKeyStrokes();
        } finally {
            state.releaseAccessToKeyStrokes();
        }
    }

    @Test
    public void GIVEN_oldKeystrokes_WHEN_enqueueKeyStroke_THEN_dividesCorrectly() {
        underTest.enqueueKeyStroke(OLD_KEY_STROKE_POJO);
        underTest.enqueueKeyStroke(KEY_STROKE_POJO);
        underTest.enqueueKeyStroke(KEY_STROKE_POJO);

        try {
            state.obtainAccessToKeyStrokes();
            assertTrue("newest division of keystrokes should only have two keystrokes",
                    state.getNewestKeyStrokes().getKeyStrokes().size() == 2);
            verify(state, times(3)).notifyKeyQueueChange(any());
            verify(state, times(3)).releaseAccessToKeyStrokes();
        } finally {
            state.releaseAccessToKeyStrokes();
        }
    }

    @Test
    public void GIVEN_newStatus_WHEN_enqueueStatus_THEN_success() {
        underTest.enqueueStatus(PojoHelper.createStatus(CLIENT_STATUS_POJO, AuthConstants.UNAUTHENTICATED));

        try {
            state.obtainAccessToStatus();
            assertTrue("current status should now be unauthenticated",
                    state.getCurrentStatus().getAuthStatus().equals(AuthConstants.UNAUTHENTICATED));
            verify(state, times(1)).notifyStatusChange(eq(CLIENT_STATUS_POJO), any());
            verify(state, times(1)).releaseAccessToStatus();
        } finally {
            state.releaseAccessToStatus();
        }
    }

    @Test
    public void GIVEN_newModel_WHEN_passStateToModel_THEN_success() {
        ClientStateModel newModel = new ClientStateModel(Executors.newCachedThreadPool());
        try {
            newModel.obtainAccessToStatus();
            newModel.getCurrentStatus();
            newModel.enqueueStatus(CLIENT_STATUS_POJO);
        } finally {
            newModel.releaseAccessToStatus();
        }
        underTest.passStateToModel(newModel);

        try {
            state.obtainAccessToStatus();
            assertTrue("current status should now be unauthenticated",
                    state.getCurrentStatus().getAuthStatus().equals(AuthConstants.UNAUTHENTICATED));
            verify(state, times(1)).notifyModelChange();
            verify(state, times(1)).releaseAccessToModel();
        } finally {
            state.releaseAccessToStatus();
        }
    }

    @Test
    public void GIVEN_na_WHEN_checkStateModel_THEN_na() {
        // Covered in ClientStateModelTest
        underTest.checkStateModel(state);
    }

    @Test
    public void GIVEN_na_WHEN_clearModel_THEN_na() {
        // Covered in ClientStateModelTest
        underTest.clearModel();
    }

    @Test
    public void GIVEN_googleAuthKey_WHEN_setGoogleAuthKey_THEN_success() {
        underTest.setGoogleAuthKey(MAC);
        try {
            state.obtainAccessToStatus();
            assertTrue("google auth key should be in model", state.getCurrentStatus().getGoogleAuthKey().equals(MAC));
        } finally {
            state.releaseAccessToStatus();
        }
    }

}
