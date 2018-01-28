package com.biokey.client.models;

import com.biokey.client.constants.StatusConstants;
import com.biokey.client.constants.SyncStatusConstants;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import com.biokey.client.services.IClientStateListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientStateModel.class)
public class ClientStateModelTest {

    private IClientStateListener LISTENER = (ClientStatusPojo status) -> {};

    private final ClientStatusPojo CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo("","","",new float[] {}, (String challenge) -> false),
                    StatusConstants.UNLOCKED,
                    "",
                    SyncStatusConstants.UNSYNCED);
    private final ClientStatusPojo SYNCED_CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo("","","",new float[] {}, (String challenge) -> false),
                    StatusConstants.UNLOCKED,
                    "",
                    SyncStatusConstants.INSYNC);

    private final AnalysisResultPojo ANALYSIS_RESULT_POJO = new AnalysisResultPojo(1, 0);
    private final AnalysisResultPojo SYNCED_ANALYSIS_RESULT_POJO = new AnalysisResultPojo(1, 0);

    private final KeyStrokePojo KEY_STROKE_POJO = new KeyStrokePojo('t', false, 1);
    private final KeyStrokePojo SYNCED_KEY_STROKE_POJO = new KeyStrokePojo('t', false, 1);

    private ClientStateModel underTest;

    @Before
    public void initUnderTest() {
        underTest = new ClientStateModel();
        SYNCED_ANALYSIS_RESULT_POJO.setSyncedWithServer(SyncStatusConstants.INSYNC);
        SYNCED_KEY_STROKE_POJO.setSyncedWithServer(SyncStatusConstants.INSYNC);
    }

    @Test
    public void GIVEN_newListener_WHEN_addListener_THEN_success() {
        underTest.addListener(LISTENER);
        assertTrue("Should have found added listener", underTest.getStateListeners().contains(LISTENER));
    }

    @Test(expected = NullPointerException.class)
    public void GIVEN_nullListener_WHEN_addListener_THEN_throwException() {
        underTest.addListener(null);
    }

    @Test
    public void GIVEN_removeAddedListener_WHEN_removeListener_THEN_success() {
        underTest.addListener(LISTENER);
        assertTrue("Should have found and removed added listener", underTest.removeListener(LISTENER));
    }

    @Test
    public void GIVEN_removeNotAddedListener_WHEN_removeListener_THEN_nothingRemoved() {
        IClientStateListener otherListener = (ClientStatusPojo status) -> {};
        underTest.addListener(LISTENER);
        assertTrue("Should not have found other listener", !underTest.removeListener(otherListener));
        assertTrue("Should have found added listener", underTest.getStateListeners().contains(LISTENER));
    }

    @Test(expected = NullPointerException.class)
    public void GIVEN_removeNullListener_WHEN_removeListener_THEN_throwException() {
        underTest.removeListener(null);
    }

    @Test
    public void GIVEN_newKeyStroke_WHEN_addKeyStroke_THEN_success() {
        underTest.addKeyStroke(KEY_STROKE_POJO);
        assertTrue("Should have found added keystroke", underTest.getKeyStrokes().contains(KEY_STROKE_POJO));
    }

    @Test(expected = NullPointerException.class)
    public void GIVEN_nullKeyStroke_WHEN_addKeyStroke_THEN_throwException() {
        underTest.addKeyStroke(null);
    }

    @Test
    public void GIVEN_firstKeyStrokeUnsynced_WHEN_clearKeyStrokes_THEN_clearNoKeyStrokes() {
        underTest.addKeyStroke(KEY_STROKE_POJO);
        underTest.addKeyStroke(SYNCED_KEY_STROKE_POJO);
        underTest.clearKeyStrokes();
        assertTrue("Should have cleared no keystrokes", underTest.getKeyStrokes().size() == 2);
    }

    @Test
    public void GIVEN_lastKeyStrokeUnsynced_WHEN_clearKeyStrokes_THEN_clearAllButLastKeyStroke() {
        underTest.addKeyStroke(SYNCED_KEY_STROKE_POJO);
        underTest.addKeyStroke(KEY_STROKE_POJO);
        underTest.clearKeyStrokes();
        assertTrue("Should have cleared all but last keystroke", underTest.getKeyStrokes().size() == 1);
    }

    @Test
    public void GIVEN_allKeyStrokeSynced_WHEN_clearKeyStrokes_THEN_clearAllKeyStrokes() {
        underTest.addKeyStroke(SYNCED_KEY_STROKE_POJO);
        underTest.addKeyStroke(SYNCED_KEY_STROKE_POJO);
        underTest.clearKeyStrokes();
        assertTrue("Should have cleared all keystrokes", underTest.getKeyStrokes().size() == 0);
    }

    @Test
    public void GIVEN_newAnalysisResult_WHEN_addAnalysisResult_THEN_success() {
        underTest.addAnalysisResult(ANALYSIS_RESULT_POJO);
        assertTrue("Should have found added analysis result", underTest.getAnalysisResults().contains(ANALYSIS_RESULT_POJO));
    }

    @Test(expected = NullPointerException.class)
    public void GIVEN_nullAnalysisResult_WHEN_addAnalysisResult_THEN_throwException() {
        underTest.addAnalysisResult(null);
    }

    @Test
    public void GIVEN_firstAnalysisResultUnsynced_WHEN_clearAnalysisResults_THEN_clearNoResults() {
        underTest.addAnalysisResult(ANALYSIS_RESULT_POJO);
        underTest.addAnalysisResult(SYNCED_ANALYSIS_RESULT_POJO);
        underTest.clearAnalysisResults();
        assertTrue("Should have cleared no analysis results", underTest.getAnalysisResults().size() == 2);
    }

    @Test
    public void GIVEN_lastAnalysisResultUnsynced_WHEN_clearAnalysisResults_THEN_clearAllButLastResult() {
        underTest.addAnalysisResult(SYNCED_ANALYSIS_RESULT_POJO);
        underTest.addAnalysisResult(ANALYSIS_RESULT_POJO);
        underTest.clearAnalysisResults();
        assertTrue("Should have cleared all but last analysis result", underTest.getAnalysisResults().size() == 1);
    }

    @Test
    public void GIVEN_allAnalysisResultSynced_WHEN_clearAnalysisResults_THEN_clearAllResults() {
        underTest.addAnalysisResult(SYNCED_ANALYSIS_RESULT_POJO);
        underTest.addAnalysisResult(SYNCED_ANALYSIS_RESULT_POJO);
        underTest.clearAnalysisResults();
        assertTrue("Should have cleared all analysis results", underTest.getAnalysisResults().size() == 0);
    }

    @Test
    public void GIVEN_newStatus_WHEN_enqueueStatus_THEN_success() throws Exception {
        ClientStateModel underTestPartialMock = spy(underTest);
        underTestPartialMock.enqueueStatus(CLIENT_STATUS_POJO);
        assertTrue("Should have found added analysis result", underTestPartialMock.getUnsyncedStatuses().contains(CLIENT_STATUS_POJO));
        verifyPrivate(underTestPartialMock).invoke("notifyChange", any());
    }

    @Test(expected = NullPointerException.class)
    public void GIVEN_nullStatus_WHEN_enqueueStatus_THEN_throwException() {
        underTest.enqueueStatus(null);
    }

    @Test
    public void GIVEN_noStatuses_WHEN_dequeueStatus_THEN_clearNoStatuses() {
        underTest.dequeueStatus();
        assertTrue("Should have cleared no statuses", underTest.getUnsyncedStatuses().isEmpty());
    }

    @Test
    public void GIVEN_unsyncedStatuses_WHEN_dequeueStatus_THEN_clearNoStatuses() {
        underTest.enqueueStatus(CLIENT_STATUS_POJO);
        underTest.dequeueStatus();
        assertTrue("Should have cleared no statuses", underTest.getUnsyncedStatuses().contains(CLIENT_STATUS_POJO));
    }

    @Test
    public void GIVEN_syncedStatuses_WHEN_dequeueStatus_THEN_clearSyncedStatus() {
        underTest.enqueueStatus(SYNCED_CLIENT_STATUS_POJO);
        underTest.dequeueStatus();
        assertTrue("Should have cleared the synced status", underTest.getUnsyncedStatuses().isEmpty());
    }
}
