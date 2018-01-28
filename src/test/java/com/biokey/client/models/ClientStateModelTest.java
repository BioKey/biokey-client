package com.biokey.client.models;

import com.biokey.client.constants.StatusConstants;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientStateModel.class)
public class ClientStateModelTest {

    private ClientStateModel.IClientStateListener LISTENER = () -> {};
    private Set<ClientStateModel.IClientStateListener> LISTENER_SET = new HashSet<>();

    private final ClientStatusPojo CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo("","","",new float[] {}, (String challenge) -> false),
                    StatusConstants.UNLOCKED,
                    "",
                    0);
    private ClientStateModel.IStatusChangeController CONTROLLER =
            (ClientStatusPojo currentStatus) -> CLIENT_STATUS_POJO;
    private final ClientStatusPojo OTHER_CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo("","","",new float[] {}, (String challenge) -> false),
                    StatusConstants.UNLOCKED,
                    "",
                    0);
    private ClientStateModel.IStatusChangeController OTHER_CONTROLLER =
            (ClientStatusPojo currentStatus) -> OTHER_CLIENT_STATUS_POJO;

    private final AnalysisResultPojo ANALYSIS_RESULT_POJO = new AnalysisResultPojo(1, 0);
    private final AnalysisResultPojo OTHER_ANALYSIS_RESULT_POJO = new AnalysisResultPojo(1, 0);

    private final KeyStrokePojo KEY_STROKE_POJO = new KeyStrokePojo('t', false, 1);
    private final KeyStrokePojo OTHER_KEY_STROKE_POJO = new KeyStrokePojo('t', false, 1);

    private ClientStateModel underTest;

    @Before
    public void initUnderTest() {
        LISTENER_SET.add(LISTENER);
        underTest = new ClientStateModel();
        underTest.setStateListeners(LISTENER_SET);
    }

    @Test
    public void GIVEN_newKeyStroke_WHEN_enqueueKeyStroke_THEN_success() {
        underTest.enqueueKeyStroke(KEY_STROKE_POJO);
        assertTrue("Should have found added key stroke in all key strokes queue",
                underTest.getKeyStrokes().contains(KEY_STROKE_POJO));
        assertTrue("Should have found added key stroke in unsynced key strokes queue",
                underTest.getOldestKeyStrokes().getKeyStrokes().contains(KEY_STROKE_POJO));
    }

    @Test(expected = NullPointerException.class)
    public void GIVEN_nullKeyStroke_WHEN_enqueueKeyStroke_THEN_throwException() {
        underTest.enqueueKeyStroke(null);
    }

    @Test
    public void GIVEN_dequeueKeyStrokeFromEmptyQueue_WHEN_dequeueSyncedKeyStrokes_THEN_returnsFalse() {
        assertTrue("Should have cleared no key strokes from unsynced queue",
                !underTest.dequeueSyncedKeyStrokes());
    }

    @Test
    public void GIVEN_dequeueKeyStrokeFromEmptyQueue_WHEN_dequeueAllKeyStrokes_THEN_returnsFalse() {
        assertTrue("Should have cleared no key strokes from all key strokes queue",
                !underTest.dequeueAllKeyStrokes());
    }

    @Test
    public void GIVEN_dequeueKeyStrokeFromUnsynced_WHEN_dequeueSyncedKeyStrokes_THEN_rightKeyStrokesDequeued() {
        underTest.enqueueKeyStroke(KEY_STROKE_POJO);
        underTest.enqueueKeyStroke(KEY_STROKE_POJO);
        underTest.divideKeyStrokes();
        underTest.enqueueKeyStroke(KEY_STROKE_POJO);
        underTest.dequeueSyncedKeyStrokes();
        assertTrue("Should have cleared oldest division of key strokes from unsynced queue",
                underTest.getOldestKeyStrokes().getKeyStrokes().size() == 1);
        assertTrue("Should have cleared no key strokes from all key strokes queue",
                underTest.getOldestKeyStrokes().getKeyStrokes().size() == 1);
    }

    @Test
    public void GIVEN_dequeueKeyStrokeFromAll_WHEN_dequeueAllKeyStrokes_THEN_rightKeyStrokesDequeued() {
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
    }

    @Test
    public void GIVEN_newAnalysisResult_WHEN_enqueueAnalysisResult_THEN_success() {
        underTest.enqueueAnalysisResult(ANALYSIS_RESULT_POJO);
        assertTrue("Should have found added analysis result",
                underTest.getOldestAnalysisResult() == ANALYSIS_RESULT_POJO);
    }

    @Test(expected = NullPointerException.class)
    public void GIVEN_nullAnalysisResult_WHEN_enqueueAnalysisResult_THEN_throwException() {
        underTest.enqueueAnalysisResult(null);
    }

    @Test
    public void GIVEN_dequeueAnalysisResultFromEmptyQueue_WHEN_dequeueAnalysisResult_THEN_returnsFalse() {
        assertTrue("Should have cleared no results from queue", !underTest.dequeueAnalysisResult());
    }

    @Test
    public void GIVEN_dequeueAnalysisResult_WHEN_dequeueAnalysisResult_THEN_rightResultDequeued() {
        underTest.enqueueAnalysisResult(ANALYSIS_RESULT_POJO);
        underTest.enqueueAnalysisResult(OTHER_ANALYSIS_RESULT_POJO);
        underTest.dequeueAnalysisResult();
        assertTrue("Should have cleared oldest result from queue",
                underTest.getOldestAnalysisResult() == OTHER_ANALYSIS_RESULT_POJO);
    }

    @Test
    public void GIVEN_newStatus_WHEN_enqueueStatus_THEN_success() throws Exception {
        ClientStateModel underTestPartialMock = spy(underTest);
        underTestPartialMock.enqueueStatus(CONTROLLER);
        assertTrue("Should have found added analysis result",
                underTestPartialMock.getOldestStatus() == CLIENT_STATUS_POJO);
        verifyPrivate(underTestPartialMock).invoke("notifyChange");
    }

    @Test(expected = NullPointerException.class)
    public void GIVEN_nullStatus_WHEN_enqueueStatus_THEN_throwException() {
        underTest.enqueueStatus(null);
    }

    @Test
    public void GIVEN_noStatuses_WHEN_dequeueStatus_THEN_clearNoStatuses() {
        assertTrue("Should have cleared no statuses from queue", !underTest.dequeueStatus());
    }

    @Test
    public void GIVEN_dequeueStatus_WHEN_dequeueStatus_THEN_rightStatusDequeued() {
        underTest.enqueueStatus(CONTROLLER);
        underTest.enqueueStatus(OTHER_CONTROLLER);
        underTest.dequeueStatus();
        assertTrue("Should have cleared oldest status from queue",
                underTest.getOldestStatus() == OTHER_CLIENT_STATUS_POJO);
        underTest.dequeueStatus();
        assertTrue("Current status should remain defined",
                underTest.getCurrentStatus() == OTHER_CLIENT_STATUS_POJO);
    }
}
