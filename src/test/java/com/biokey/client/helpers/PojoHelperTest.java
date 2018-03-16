package com.biokey.client.helpers;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.EngineModelPojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PojoHelperTest {
    // TODO: Write tests.

    private static final ClientStatusPojo CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo("", "","", new EngineModelPojo(), new String[] {},""),
                    AuthConstants.AUTHENTICATED, SecurityConstants.UNLOCKED,
                    "", "", "", 0);

    @Test
    public void GIVEN_authStatus_WHEN_createStatusWithAuth_THEN_success() {
        assertTrue("authStatus should be unauthenticated",
                PojoHelper.createStatus(CLIENT_STATUS_POJO, AuthConstants.UNAUTHENTICATED)
                        .getAuthStatus().equals(AuthConstants.UNAUTHENTICATED));
    }

    @Test
    public void GIVEN_securityStatus_WHEN_createStatusWithSecurity_THEN_success() {
        assertTrue("securityStatus should be challenge",
                PojoHelper.createStatus(CLIENT_STATUS_POJO, SecurityConstants.CHALLENGE)
                        .getSecurityStatus().equals(SecurityConstants.CHALLENGE));
    }
}
