package com.biokey.client.services;

import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.constants.AuthConstants;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.TypingProfilePojo;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ClientInitServiceTest {

    @Mock
    private ClientStateModel state;

    @InjectMocks
    private ClientInitService underTest = new ClientInitService();

    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String TYPING_PROFILE_ID = "TYPING_PROFILE_ID";
    private static final ClientStatusPojo CLIENT_STATUS_POJO =
            new ClientStatusPojo(
                    new TypingProfilePojo(TYPING_PROFILE_ID, "","","",new float[] {}, (String challenge) -> false),
                    AuthConstants.AUTHENTICATED, SecurityConstants.UNLOCKED,
                    ACCESS_TOKEN,
                    0
            );

    @BeforeClass
    public void loadData() {
    }

    @Test
    public void ClientInitService_should_save_and_read_ClientStateModel(){
    }
}
