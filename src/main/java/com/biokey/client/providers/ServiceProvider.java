package com.biokey.client.providers;

import com.biokey.client.services.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration class that provides the singleton instances of the services that are BioKey client's backbone.
 */
@Configuration
@Import({ClientStateProvider.class, ChallengeProvider.class})
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
}
