package com.biokey.client.models;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.controllers.challenges.IChallengeStrategy;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientStateModel.class)
public class ClientStateModelTest {

    private static final ClientStateModel.IClientStatusListener STATUS_LISTENER = mock(ClientStateModel.IClientStatusListener.class);
    private static final HashSet<ClientStateModel.IClientStatusListener> STATUS_LISTENER_SET = new HashSet<>();
    private static final ClientStateModel.IClientKeyListener KEY_LISTENER = mock(ClientStateModel.IClientKeyListener.class);
    private static final HashSet<ClientStateModel.IClientKeyListener> KEY_LISTENER_SET = new HashSet<>();
    private static final ClientStateModel.IClientAnalysisListener ANALYSIS_LISTENER = mock(ClientStateModel.IClientAnalysisListener.class);
    private static final HashSet<ClientStateModel.IClientAnalysisListener> ANALYSIS_LISTENER_SET = new HashSet<>();

    private final ClientStatusPojo CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo("", "", "", "", new float[] {}, new IChallengeStrategy[] {}, ""),
                    AuthConstants.AUTHENTICATED, SecurityConstants.UNLOCKED,
                    "", "", 0);
    private final ClientStatusPojo OTHER_CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo("", "", "", "", new float[] {}, new IChallengeStrategy[] {}, ""),
                    AuthConstants.AUTHENTICATED, SecurityConstants.UNLOCKED,
                    "", "", 0);

    private final AnalysisResultPojo ANALYSIS_RESULT_POJO = new AnalysisResultPojo(1, 0);
    private final AnalysisResultPojo OTHER_ANALYSIS_RESULT_POJO = new AnalysisResultPojo(1, 0);

    private final KeyStrokePojo KEY_STROKE_POJO = new KeyStrokePojo('t', false, 1);
    private final KeyStrokePojo OTHER_KEY_STROKE_POJO = new KeyStrokePojo('t', false, 1);

    private ClientStateModel underTest;

    @BeforeClass
    public static void initListeners() {
        STATUS_LISTENER_SET.add(STATUS_LISTENER);
        KEY_LISTENER_SET.add(KEY_LISTENER);
        ANALYSIS_LISTENER_SET.add(ANALYSIS_LISTENER);
        doNothing().when(STATUS_LISTENER).statusChanged(any(), any());
        doNothing().when(KEY_LISTENER).keystrokeQueueChanged(any());
        doNothing().when(ANALYSIS_LISTENER).analysisResultQueueChanged(any());
    }

    @Before
    public void initUnderTest() {
        underTest = new ClientStateModel();
        underTest.setStatusListeners(STATUS_LISTENER_SET);
        underTest.setKeyQueueListeners(KEY_LISTENER_SET);
        underTest.setAnalysisResultQueueListeners(ANALYSIS_LISTENER_SET);
    }

    public interface ITestRunner {
        void run();
    }

    @Test
    public void GIVEN_noAccess_WHEN_anyFunction_THEN_throwsException() {
        ArrayList<ITestRunner> badListeners = new ArrayList<>();
        badListeners.add(underTest::clear);
        badListeners.add(underTest::getCurrentStatus);
        badListeners.add(underTest::dequeueStatus);
        badListeners.add(underTest::getOldestStatus);
        badListeners.add(underTest::dequeueAnalysisResult);
        badListeners.add(underTest::getOldestAnalysisResult);
        badListeners.add(underTest::divideKeyStrokes);
        badListeners.add(underTest::dequeueSyncedKeyStrokes);
        badListeners.add(underTest::dequeueAllKeyStrokes);
        badListeners.add(underTest::getOldestKeyStrokes);
        badListeners.add(underTest::getKeyStrokes);

        // All the functions with no parameters.
        for (ITestRunner badListener : badListeners) {
            try {
                badListener.run();
                fail("AccessControlException not thrown.");
            } catch (Exception e) {
                assertTrue(e instanceof AccessControlException);
            }
        }

        // Test the functions with parameters.
        try {
            underTest.loadStateFromMemory(underTest);
            fail("AccessControlException not thrown.");
        } catch (Exception e) {
            assertTrue(e instanceof AccessControlException);
        }

        try {
            underTest.enqueueStatus(CLIENT_STATUS_POJO);
            fail("AccessControlException not thrown.");
        } catch (Exception e) {
            assertTrue(e instanceof AccessControlException);
        }

        try {
            underTest.enqueueAnalysisResult(ANALYSIS_RESULT_POJO);
            fail("AccessControlException not thrown.");
        } catch (Exception e) {
            assertTrue(e instanceof AccessControlException);
        }

        try {
            underTest.enqueueKeyStroke(KEY_STROKE_POJO);
            fail("AccessControlException not thrown.");
        } catch (Exception e) {
            assertTrue(e instanceof AccessControlException);
        }
    }

    @Test
    public void GIVEN_multipleObtainReleaseAccess_WHEN_obtainReleaseFunctions_THEN_success() {
        try {
            underTest.obtainAccessToModel();
            underTest.obtainAccessToModel();
            underTest.releaseAccessToModel();
            underTest.releaseAccessToModel();
            underTest.releaseAccessToModel();

            for (int i = 0; i < 2; i++) {
                underTest.obtainAccessToStatus();
                underTest.obtainAccessToKeyStrokes();
                underTest.obtainAccessToAnalysisResult();
            }

            for (int i = 0; i < 3; i++) {
                underTest.releaseAccessToStatus();
                underTest.releaseAccessToKeyStrokes();
                underTest.releaseAccessToAnalysisResult();
            }
        } finally {
            underTest.releaseAccessToModel();
        }
    }

    @Test
    public void GIVEN_newKeyStroke_WHEN_enqueueKeyStroke_THEN_success() {
        try {
            underTest.obtainAccessToKeyStrokes();
            ClientStateModel underTestPartialMock = spy(underTest);
            underTestPartialMock.enqueueKeyStroke(KEY_STROKE_POJO);
            assertTrue("Should have found added key stroke in all key strokes queue",
                    underTestPartialMock.getKeyStrokes().contains(KEY_STROKE_POJO));
            assertTrue("Should have found added key stroke in unsynced key strokes queue",
                    underTestPartialMock.getOldestKeyStrokes().getKeyStrokes().contains(KEY_STROKE_POJO));
            //verifyPrivate(underTestPartialMock).invoke("notifyKeyQueueChange", any());
        } finally {
            underTest.releaseAccessToKeyStrokes();
        }
    }

    @Test
    public void GIVEN_dividedKeyStrokes_WHEN_enqueueKeyStroke_THEN_orderRetrievedCorrect() {
        try {
            underTest.obtainAccessToKeyStrokes();
            underTest.enqueueKeyStroke(KEY_STROKE_POJO);
            underTest.divideKeyStrokes();
            underTest.enqueueKeyStroke(OTHER_KEY_STROKE_POJO);
            assertTrue("Should have found first key stroke in oldest keystrokes",
                    underTest.getOldestKeyStrokes().getKeyStrokes().contains(KEY_STROKE_POJO));
            assertTrue("Should have found second key stroke in newest keystrokes",
                    underTest.getNewestKeyStrokes().getKeyStrokes().contains(OTHER_KEY_STROKE_POJO));
        } finally {
            underTest.releaseAccessToKeyStrokes();
        }
    }

    @Test(expected = NullPointerException.class)
    public void GIVEN_nullKeyStroke_WHEN_enqueueKeyStroke_THEN_throwException() {
        try {
            underTest.obtainAccessToKeyStrokes();
            underTest.enqueueKeyStroke(null);
        } finally {
            underTest.releaseAccessToKeyStrokes();
        }
    }

    @Test
    public void GIVEN_dequeueKeyStrokeFromEmptyQueue_WHEN_dequeueSyncedKeyStrokes_THEN_returnsFalse() {
        try {
            underTest.obtainAccessToKeyStrokes();
            assertTrue("Should have cleared no key strokes from unsynced queue",
                    !underTest.dequeueSyncedKeyStrokes());
        } finally {
            underTest.releaseAccessToKeyStrokes();
        }
    }

    @Test
    public void GIVEN_dequeueKeyStrokeFromEmptyQueue_WHEN_dequeueAllKeyStrokes_THEN_returnsFalse() {
        try {
            underTest.obtainAccessToKeyStrokes();
            assertTrue("Should have cleared no key strokes from all key strokes queue",
                    !underTest.dequeueAllKeyStrokes());
        } finally {
            underTest.releaseAccessToKeyStrokes();
        }
    }

    @Test
    public void GIVEN_dequeueKeyStrokeFromUnsynced_WHEN_dequeueSyncedKeyStrokes_THEN_rightKeyStrokesDequeued() {
        try {
            underTest.obtainAccessToKeyStrokes();
            underTest.enqueueKeyStroke(KEY_STROKE_POJO);
            underTest.enqueueKeyStroke(KEY_STROKE_POJO);
            underTest.divideKeyStrokes();
            underTest.enqueueKeyStroke(KEY_STROKE_POJO);
            underTest.dequeueSyncedKeyStrokes();
            assertTrue("Should have cleared oldest division of key strokes from unsynced queue",
                    underTest.getOldestKeyStrokes().getKeyStrokes().size() == 1);
            assertTrue("Should have cleared no key strokes from all key strokes queue",
                    underTest.getOldestKeyStrokes().getKeyStrokes().size() == 1);
        } finally {
            underTest.releaseAccessToKeyStrokes();
        }
    }

    @Test
    public void GIVEN_dequeueKeyStrokeFromAll_WHEN_dequeueAllKeyStrokes_THEN_rightKeyStrokesDequeued() {
        try {
            underTest.obtainAccessToKeyStrokes();
            underTest.enqueueKeyStroke(KEY_STROKE_POJO);
            underTest.enqueueKeyStroke(OTHER_KEY_STROKE_POJO);
            underTest.divideKeyStrokes();
            underTest.enqueueKeyStroke(OTHER_KEY_STROKE_POJO);
            underTest.dequeueAllKeyStrokes();
            assertTrue("Should have cleared no key strokes from unsynced queue",
                    underTest.getOldestKeyStrokes().getKeyStrokes().size() == 2);
            assertTrue("Should have cleared key stroke from all key strokes queue",
                    underTest.getKeyStrokes().size() == 2);
            assertTrue("Should have cleared oldest key stroke from all key strokes queue",
                    underTest.getKeyStrokes().peek() == OTHER_KEY_STROKE_POJO);
        } finally {
            underTest.releaseAccessToKeyStrokes();
        }
    }

    @Test
    public void GIVEN_newAnalysisResult_WHEN_enqueueAnalysisResult_THEN_success() {
        try {
            underTest.obtainAccessToAnalysisResult();
            ClientStateModel underTestPartialMock = spy(underTest);
            underTestPartialMock.enqueueAnalysisResult(ANALYSIS_RESULT_POJO);
            assertTrue("Should have found added analysis result",
                    underTestPartialMock.getOldestAnalysisResult() == ANALYSIS_RESULT_POJO);
            //verifyPrivate(underTestPartialMock).invoke("notifyAnalysisResultQueueChange", any());
        } finally {
            underTest.releaseAccessToAnalysisResult();
        }
    }

    @Test(expected = NullPointerException.class)
    public void GIVEN_nullAnalysisResult_WHEN_enqueueAnalysisResult_THEN_throwException() {
        try {
            underTest.obtainAccessToAnalysisResult();
            underTest.enqueueAnalysisResult(null);
        } finally {
            underTest.releaseAccessToAnalysisResult();
        }
    }

    @Test
    public void GIVEN_dequeueAnalysisResultFromEmptyQueue_WHEN_dequeueAnalysisResult_THEN_returnsFalse() {
        try {
            underTest.obtainAccessToAnalysisResult();
            assertTrue("Should have cleared no results from queue", !underTest.dequeueAnalysisResult());
        } finally {
            underTest.releaseAccessToAnalysisResult();
        }
    }

    @Test
    public void GIVEN_dequeueAnalysisResult_WHEN_dequeueAnalysisResult_THEN_rightResultDequeued() {
        try {
            underTest.obtainAccessToAnalysisResult();
            underTest.enqueueAnalysisResult(ANALYSIS_RESULT_POJO);
            underTest.enqueueAnalysisResult(OTHER_ANALYSIS_RESULT_POJO);
            underTest.dequeueAnalysisResult();
            assertTrue("Should have cleared oldest result from queue",
                    underTest.getOldestAnalysisResult() == OTHER_ANALYSIS_RESULT_POJO);
        } finally {
            underTest.releaseAccessToAnalysisResult();
        }
    }

    @Test
    public void GIVEN_newStatus_WHEN_enqueueStatus_THEN_success() {
        try {
            underTest.obtainAccessToStatus();
            ClientStateModel underTestPartialMock = spy(underTest);

            underTestPartialMock.getCurrentStatus();
            underTestPartialMock.enqueueStatus(CLIENT_STATUS_POJO);

            assertTrue("Should have found added analysis result",
                    underTestPartialMock.getOldestStatus() == CLIENT_STATUS_POJO);
            //verifyPrivate(underTestPartialMock).invoke("notifyStatusChange", any(), any());
        } finally {
            underTest.releaseAccessToStatus();
        }
    }

    @Test(expected = AccessControlException.class)
    public void GIVEN_newStatusWithoutRetrievingOldStatus_WHEN_enqueueStatus_THEN_throwException() {
        try {
            underTest.obtainAccessToStatus();
            underTest.enqueueStatus(CLIENT_STATUS_POJO);
        } finally {
            underTest.releaseAccessToStatus();
        }
    }

    @Test
    public void GIVEN_newStatusWhileModelLocked_WHEN_enqueueStatus_THEN_success() {
        try {
            underTest.obtainAccessToModel();
            ClientStateModel underTestPartialMock = spy(underTest);

            underTestPartialMock.enqueueStatus(CLIENT_STATUS_POJO);
            assertTrue("Should have found added analysis result",
                    underTestPartialMock.getOldestStatus() == CLIENT_STATUS_POJO);
            //verifyPrivate(underTestPartialMock).invoke("notifyStatusChange", any(), any());
        } finally {
            underTest.releaseAccessToModel();
        }
    }

    @Test(expected = NullPointerException.class)
    public void GIVEN_nullStatus_WHEN_enqueueStatus_THEN_throwException() {
        try {
            underTest.obtainAccessToStatus();
            underTest.getCurrentStatus();
            underTest.enqueueStatus(null);
        } finally {
            underTest.releaseAccessToStatus();
        }
    }

    @Test
    public void GIVEN_noStatuses_WHEN_dequeueStatus_THEN_clearNoStatuses() {
        try {
            underTest.obtainAccessToStatus();
            assertTrue("Should have cleared no statuses from queue", !underTest.dequeueStatus());
        } finally {
            underTest.releaseAccessToStatus();
        }
    }

    @Test
    public void GIVEN_dequeueStatus_WHEN_dequeueStatus_THEN_rightStatusDequeued() {
        try {
            underTest.obtainAccessToStatus();
            underTest.getCurrentStatus();
            underTest.enqueueStatus(CLIENT_STATUS_POJO);
            underTest.enqueueStatus(OTHER_CLIENT_STATUS_POJO);
            underTest.dequeueStatus();
            assertTrue("Should have cleared oldest status from queue",
                    underTest.getOldestStatus() == OTHER_CLIENT_STATUS_POJO);
            underTest.dequeueStatus();
            assertTrue("Current status should remain defined",
                    underTest.getCurrentStatus() == OTHER_CLIENT_STATUS_POJO);
        } finally {
            underTest.releaseAccessToStatus();
        }
    }

    @Test
    public void GIVEN_notifyQueues_WHEN_notify_THEN_lambdasCalled() {
        underTest.notifyStatusChange(null, null);
        underTest.notifyKeyQueueChange(null);
        underTest.notifyAnalysisResultQueueChange(null);
        underTest.notifyModelChange();
        verify(STATUS_LISTENER, timeout(100).times(2)).statusChanged(any(), any());
        verify(KEY_LISTENER, timeout(100).times(2)).keystrokeQueueChanged(any());
        verify(ANALYSIS_LISTENER, timeout(100).times(2)).analysisResultQueueChanged(any());
    }

    @Test
    public void GIVEN_newModel_WHEN_loadStateFromMemory_THEN_changesReflected() {
        ClientStateModel newModel = new ClientStateModel();
        try {
            newModel.obtainAccessToStatus();
            newModel.getCurrentStatus();
            newModel.enqueueStatus(CLIENT_STATUS_POJO);
            underTest.obtainAccessToModel();
            underTest.loadStateFromMemory(newModel);

            assertTrue("changes should be reflected to model", underTest.getCurrentStatus() == CLIENT_STATUS_POJO);
        } finally {
            newModel.releaseAccessToStatus();
            underTest.releaseAccessToModel();
        }
    }

    @Test
    public void GIVEN_itself_WHEN_checkStateModel_THEN_returnInvalid() {
        assertTrue("the model should not be valid", !underTest.checkStateModel(underTest));
    }

    @Test
    public void GIVEN_itselfAfterEnqueueStatus_WHEN_checkStateModel_THEN_returnInvalid() {
        try {
            underTest.obtainAccessToStatus();
            underTest.getCurrentStatus();
            underTest.enqueueStatus(CLIENT_STATUS_POJO);
            assertTrue("the model should be valid", underTest.checkStateModel(underTest));
        } finally {
            underTest.releaseAccessToStatus();
        }

    }

    @Test
    public void GIVEN_enqueuedItems_WHEN_clear_THEN_itemsCleared() {
        try {
            underTest.obtainAccessToModel();
            underTest.enqueueStatus(CLIENT_STATUS_POJO);
            underTest.enqueueKeyStroke(KEY_STROKE_POJO);
            underTest.enqueueAnalysisResult(ANALYSIS_RESULT_POJO);
            underTest.clear();
            assertTrue("Should have found no current status",underTest.getCurrentStatus() == null);
            assertTrue("Should have found no old status",underTest.getOldestStatus() == null);
            assertTrue("Should have found no new keystrokes",underTest.getNewestKeyStrokes() == null);
            assertTrue("Should have found no old keystrokes",underTest.getOldestKeyStrokes() == null);
            assertTrue("Should have found no keystrokes",underTest.getKeyStrokes().size() == 0);
            assertTrue("Should have found no analysis results",underTest.getOldestAnalysisResult() == null);
        } finally {
            underTest.releaseAccessToModel();
        }
    }
}
