package com.biokey.client.services;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.constants.AuthConstants;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.TypingProfilePojo;

import com.biokey.client.providers.AppProvider;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientInitService.class)
public class ClientInitServiceIntegrationTest {

    private static final ClientStateModel.IClientStatusListener STATUS_LISTENER = (ClientStatusPojo oldStatus, ClientStatusPojo newStatus) -> {};
    private static final Set<ClientStateModel.IClientStatusListener> STATUS_LISTENER_SET = new HashSet<>();
    private static final ClientStateModel.IClientKeyListener KEY_LISTENER = (KeyStrokePojo newKey) -> {};
    private static final Set<ClientStateModel.IClientKeyListener> KEY_LISTENER_SET = new HashSet<>();
    private static final ClientStateModel.IClientAnalysisListener ANALYSIS_LISTENER = (AnalysisResultPojo newResult) -> {};
    private static final Set<ClientStateModel.IClientAnalysisListener> ANALYSIS_LISTENER_SET = new HashSet<>();
    private static final ClientStatusPojo CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo("", "","","",new float[] {}, (String challenge) -> false),
                    AuthConstants.AUTHENTICATED, SecurityConstants.UNLOCKED,
                    "",
                    777
            );

    private static Logger log = Logger.getLogger(ClientInitService.class);

    private static ClientStateModel state;
    private static ClientInitService underTest;

    @BeforeClass
    public static void initSpring() {
        ApplicationContext springContext = new AnnotationConfigApplicationContext(AppProvider.class);

        state = springContext.getBean(ClientStateModel.class);
        STATUS_LISTENER_SET.add(STATUS_LISTENER);
        KEY_LISTENER_SET.add(KEY_LISTENER);
        ANALYSIS_LISTENER_SET.add(ANALYSIS_LISTENER);
        state.setStatusListeners(STATUS_LISTENER_SET);
        state.setKeyQueueListeners(KEY_LISTENER_SET);
        state.setAnalysisResultQueueListeners(ANALYSIS_LISTENER_SET);
        state.obtainAccessToModel();
        state.enqueueStatus(CLIENT_STATUS_POJO);
        state.releaseAccessToModel();

        underTest = springContext.getBean(ClientInitService.class);
    }

    @Test
    public void GIVEN_validState_WHEN_saveClientState_retrieveClientState_THEN_success() throws Exception {
        ClientInitService underTestPartialMock = spy(underTest);
        try {
            underTestPartialMock.saveClientState();
            underTestPartialMock.saveClientState();
            underTestPartialMock.retrieveClientState();

            state.obtainAccessToModel();
            assertTrue("Retrieved state should be equivalent to saved state",
                    state.getCurrentStatus().getTimeStamp() == CLIENT_STATUS_POJO.getTimeStamp());
            verifyPrivate(underTestPartialMock).invoke("login", anyString());
        }
        finally {
            state.releaseAccessToModel();
        }
    }

    @Test
    public void GIVEN_noState_WHEN_retrieveClientState_THEN_login() throws Exception {
        ClientInitService underTestPartialMock = spy(underTest);
        Preferences prefs = (Preferences) Whitebox.getInternalState(underTest, "prefs");
        prefs.putByteArray(AppConstants.CLIENT_STATE_PREFERENCES_ID, new byte[0]);
        underTestPartialMock.retrieveClientState();
        verifyPrivate(underTestPartialMock).invoke("login");
    }

    @Test
    public void GIVEN_invalidState_WHEN_retrieveClientState_THEN_no_login() throws Exception {
        ClientInitService underTestPartialMock = spy(underTest);
        Preferences prefs = (Preferences) Whitebox.getInternalState(underTest, "prefs");
        prefs.putByteArray(AppConstants.CLIENT_STATE_PREFERENCES_ID, new byte[]{(byte)0xaa});
        underTestPartialMock.retrieveClientState();
        verifyPrivate(underTestPartialMock).invoke("login");
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }
}
