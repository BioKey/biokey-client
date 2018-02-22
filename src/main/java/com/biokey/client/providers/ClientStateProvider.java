package com.biokey.client.providers;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.helpers.RequestBuilderHelper;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * Configuration class that provides the singleton instances relating to the client state.
 */
@Configuration
@Import({HelperProvider.class})
public class ClientStateProvider {

    @Bean
    @Autowired
    public ClientStateController clientStateController(ServerRequestExecutorHelper serverRequestExecutorHelper) {
        return new ClientStateController(clientStateModel(), serverRequestExecutorHelper);
    }

    @Bean
    public ClientStateModel clientStateModel() {
        return new ClientStateModel();
    }
}
