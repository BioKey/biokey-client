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
    public Set<ClientStateModel.IClientStatusListener> statusListeners() {
        Set<ClientStateModel.IClientStatusListener> serviceStatusListeners = new HashSet<>();
        serviceStatusListeners.add(clientInitService());
        serviceStatusListeners.add(analysisEngineService());
        serviceStatusListeners.add(keyloggerDaemonService());
        serviceStatusListeners.add(lockerService());
        serviceStatusListeners.add(serverListenerService());

        return serviceStatusListeners;
    }

    @Bean
    public Set<ClientStateModel.IClientAnalysisListener> analysisQueueListeners() {
        Set<ClientStateModel.IClientAnalysisListener> analysisQueueListeners = new HashSet<>();
        analysisQueueListeners.add(lockerService());
        analysisQueueListeners.add(clientInitService());

        return analysisQueueListeners;
    }

    @Bean
    public Set<ClientStateModel.IClientKeyListener> keyQueueListeners() {
        Set<ClientStateModel.IClientKeyListener> keyQueueListeners = new HashSet<>();
        keyQueueListeners.add(analysisEngineService());
        keyQueueListeners.add(clientInitService());

        return keyQueueListeners;
    }
}
