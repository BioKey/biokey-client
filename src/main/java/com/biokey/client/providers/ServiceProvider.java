package com.biokey.client.providers;

import com.biokey.client.models.ClientStateModel;
import com.biokey.client.services.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration class that provides the singleton instances of the services that are BioKey client's backbone.
 */
@Configuration
public class ServiceProvider {

    @Bean
    public ClientInitService clientInitService() {
        return new ClientInitService();
    }

    @Bean
    public AnalysisEngineService analysisEngineService() {
        return new AnalysisEngineService();
    }

    @Bean
    public KeyloggerDaemonService keyloggerDaemonService() {
        return new KeyloggerDaemonService();
    }

    @Bean
    public LockerService lockerService() {
        return new LockerService();
    }

    @Bean
    public ServerListenerService serverListenerService() {
        return new ServerListenerService();
    }

    @Bean
    public Set<ClientStateModel.IClientStateListener> serviceListeners() {
        Set<ClientStateModel.IClientStateListener> serviceListeners = new HashSet<>();
        serviceListeners.add(clientInitService());
        serviceListeners.add(analysisEngineService());
        serviceListeners.add(keyloggerDaemonService());
        serviceListeners.add(lockerService());
        serviceListeners.add(serverListenerService());

        return serviceListeners;
    }
}
