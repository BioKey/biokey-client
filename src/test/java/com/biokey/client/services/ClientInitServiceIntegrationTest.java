package com.biokey.client.services;

import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.constants.AuthConstants;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokePojo;
import com.biokey.client.models.pojo.TypingProfilePojo;

import com.biokey.client.providers.AppProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

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
        underTest.saveClientState();
        underTest.saveClientState();
        underTest.retrieveClientState();

        state.obtainAccessToModel();
        assertTrue("Retrieved state should be equivalent to saved state",
                state.getCurrentStatus().getTimeStamp() == CLIENT_STATUS_POJO.getTimeStamp());
        state.releaseAccessToModel();
    }
}
