package com.biokey.client.providers;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that provides the singleton instances relating to the client state.
 */
@Configuration
public class ClientStateProvider {

    @Bean
    public ClientStateController clientStateController() {
        return new ClientStateController();
    }

    @Bean
    public ClientStateModel clientStateModel() {
        return new ClientStateModel();
    }
}
