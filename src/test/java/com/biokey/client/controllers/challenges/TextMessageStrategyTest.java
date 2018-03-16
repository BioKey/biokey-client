package com.biokey.client.controllers.challenges;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.EngineModelPojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TextMessageStrategyTest {

    private final ClientStatusPojo CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo("", "", "", new EngineModelPojo(), new String[] {}, ""),
                    AuthConstants.AUTHENTICATED, SecurityConstants.UNLOCKED,
                    "", "", "", 0);

    @Mock
    private ClientStateModel state;

    private TextMessageStrategy underTest;

    @Before
    public void init() {
        underTest = new TextMessageStrategy(state);
        underTest.init();
        doNothing().when(state).obtainAccessToStatus();
        doNothing().when(state).releaseAccessToStatus();
    }

    @Test
    public void GIVEN_na_WHEN_init_THEN_initialized() {
        // Init already called in @Before.
        assertTrue("strategy should be initialized", underTest.isInitialized());
    }

    @Test
    public void GIVEN_validPhoneNumber_WHEN_issueChallenge_THEN_success() {
        when(state.getCurrentStatus()).thenReturn(CLIENT_STATUS_POJO);
        assertTrue("challenge should be issued successfully", underTest.issueChallenge());
    }

    @Test
    public void GIVEN_noStatus_WHEN_issueChallenge_THEN_challengeNotIssued() {
        when(state.getCurrentStatus()).thenReturn(null);
        assertTrue("challenge should not be issued", !underTest.issueChallenge());
        assertTrue("challenge should not be null", underTest.getChallenge() != null);
    }

    @Test
    public void GIVEN_challengeIssued_WHEN_checkChallenge_THEN_success() {
        when(state.getCurrentStatus()).thenReturn(CLIENT_STATUS_POJO);
        underTest.issueChallenge();
        String issuedChallenge = underTest.getChallenge();
        assertTrue("challenge should not be null", issuedChallenge != null);
        assertTrue("challenge as an attempt should match", underTest.checkChallenge(issuedChallenge));
        assertTrue("challenge after check should be null", underTest.getChallenge() == null);
    }
}
