package com.biokey.client.providers;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration class that provides the singleton instances of the services that are BioKey client's backbone.
 */
@Configuration
@Import(ClientStateProvider.class)
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
    @Autowired
    public Set<ClientStateModel.IClientStatusListener> statusListeners(ClientStateController clientStateController) {
        Set<ClientStateModel.IClientStatusListener> serviceStatusListeners = new HashSet<>();
        serviceStatusListeners.add(clientInitService());
        serviceStatusListeners.add(analysisEngineService());
        serviceStatusListeners.add(keyloggerDaemonService());
        serviceStatusListeners.add(lockerService());
        serviceStatusListeners.add(serverListenerService());
        serviceStatusListeners.add(clientStateController);

        return serviceStatusListeners;
    }

    @Bean
    @Autowired
    public Set<ClientStateModel.IClientAnalysisListener> analysisQueueListeners(ClientStateController clientStateController) {
        Set<ClientStateModel.IClientAnalysisListener> analysisQueueListeners = new HashSet<>();
        analysisQueueListeners.add(lockerService());
        analysisQueueListeners.add(clientInitService());
        analysisQueueListeners.add(clientStateController);

        return analysisQueueListeners;
    }

    @Bean
    @Autowired
    public Set<ClientStateModel.IClientKeyListener> keyQueueListeners(ClientStateController clientStateController) {
        Set<ClientStateModel.IClientKeyListener> keyQueueListeners = new HashSet<>();
        keyQueueListeners.add(analysisEngineService());
        keyQueueListeners.add(clientInitService());
        keyQueueListeners.add(clientStateController);

        return keyQueueListeners;
    }
}
