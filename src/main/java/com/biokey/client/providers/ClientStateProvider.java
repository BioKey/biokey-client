package com.biokey.client.providers;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * Configuration class that provides the singleton instances relating to the client state.
 */
@Configuration
@Import(ServiceProvider.class)
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
