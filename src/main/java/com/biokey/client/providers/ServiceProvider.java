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
    public Set<ClientStateModel.IClientStateListener> serviceStatusListeners() {
        Set<ClientStateModel.IClientStateListener> serviceStatusListeners = new HashSet<>();
        serviceStatusListeners.add(clientInitService());
        serviceStatusListeners.add(analysisEngineService());
        serviceStatusListeners.add(keyloggerDaemonService());
        serviceStatusListeners.add(lockerService());
        serviceStatusListeners.add(serverListenerService());

        return serviceStatusListeners;
    }

    @Bean
    public Set<ClientStateModel.IClientStatusQueueListener> serviceStatusQueueListeners() {
        Set<ClientStateModel.IClientStatusQueueListener> serviceStatusQueueListeners = new HashSet<>();

        //Assumption: the controller will listen for pending changes in the status?
        //TODO: a 'save' service will listen for pending changes in the status

        return serviceStatusQueueListeners;
    }

    @Bean
    public Set<ClientStateModel.IClientAnalysisListener> analysisQueueListeners() {
        Set<ClientStateModel.IClientAnalysisListener> analysisQueueListeners = new HashSet<>();
        analysisQueueListeners.add(lockerService());

        //Assumption: LockerService will listen for pending analysis results
        //TODO: a 'save' service will listen for pending analysis results

        return analysisQueueListeners;
    }

    @Bean
    public Set<ClientStateModel.IClientKeyListener> keyQueueListeners() {
        Set<ClientStateModel.IClientKeyListener> keyQueueListeners = new HashSet<>();
        keyQueueListeners.add(analysisEngineService());

        //Assumption: AnalysisEngineService will listen for pending/added keys
        //TODO: a 'save' service will listen for pending/added keys

        return keyQueueListeners;
    }
}
