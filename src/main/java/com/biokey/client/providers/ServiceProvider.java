package com.biokey.client.providers;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.services.*;
import com.biokey.client.views.frames.LockFrameView;
import com.biokey.client.views.panels.LoginPanelView;
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
@Import({ClientStateProvider.class, ViewProvider.class})
public class ServiceProvider {

    @Bean
    @Autowired
    public ClientInitService clientInitService(ClientStateController clientStateController, ClientStateModel clientStateModel,
                                               LockFrameView lockFrameView, LoginPanelView loginPanelView) {
        return new ClientInitService(clientStateController, clientStateModel, lockFrameView, loginPanelView);
    }

    @Bean
    @Autowired
    public AnalysisEngineService analysisEngineService(ClientStateController clientStateController, ClientStateModel clientStateModel) {
        return new AnalysisEngineService(clientStateController, clientStateModel);
    }

    @Bean
    @Autowired
    public KeyloggerDaemonService keyloggerDaemonService(ClientStateController clientStateController, ClientStateModel clientStateModel) {
        return new KeyloggerDaemonService(clientStateController, clientStateModel);
    }

    @Bean
    @Autowired
    public ChallengeService lockerService(ClientStateController clientStateController, ClientStateModel clientStateModel) {
        return new ChallengeService(clientStateController, clientStateModel);
    }

    @Bean
    @Autowired
    public ServerListenerService serverListenerService(ClientStateController clientStateController, ClientStateModel clientStateModel) {
        return new ServerListenerService(clientStateController, clientStateModel);
    }

    @Bean
    @Autowired
    public Set<ClientStateModel.IClientStatusListener> statusListeners(ClientStateController clientStateController, ClientStateModel clientStateModel,
                                                                       LockFrameView lockFrameView, LoginPanelView loginPanelView) {
        Set<ClientStateModel.IClientStatusListener> serviceStatusListeners = new HashSet<>();
        serviceStatusListeners.add(clientInitService(clientStateController, clientStateModel, lockFrameView, loginPanelView));
        serviceStatusListeners.add(analysisEngineService(clientStateController, clientStateModel));
        serviceStatusListeners.add(keyloggerDaemonService(clientStateController, clientStateModel));
        serviceStatusListeners.add(lockerService(clientStateController, clientStateModel));
        serviceStatusListeners.add(serverListenerService(clientStateController, clientStateModel));
        serviceStatusListeners.add(clientStateController);

        return serviceStatusListeners;
    }

    @Bean
    @Autowired
    public Set<ClientStateModel.IClientAnalysisListener> analysisQueueListeners(ClientStateController clientStateController, ClientStateModel clientStateModel,
                                                                                LockFrameView lockFrameView, LoginPanelView loginPanelView) {
        Set<ClientStateModel.IClientAnalysisListener> analysisQueueListeners = new HashSet<>();
        analysisQueueListeners.add(lockerService(clientStateController, clientStateModel));
        analysisQueueListeners.add(clientInitService(clientStateController, clientStateModel, lockFrameView, loginPanelView));
        analysisQueueListeners.add(clientStateController);

        return analysisQueueListeners;
    }

    @Bean
    @Autowired
    public Set<ClientStateModel.IClientKeyListener> keyQueueListeners(ClientStateController clientStateController, ClientStateModel clientStateModel,
                                                                      LockFrameView lockFrameView, LoginPanelView loginPanelView) {
        Set<ClientStateModel.IClientKeyListener> keyQueueListeners = new HashSet<>();
        keyQueueListeners.add(analysisEngineService(clientStateController, clientStateModel));
        keyQueueListeners.add(clientInitService(clientStateController, clientStateModel, lockFrameView, loginPanelView));
        keyQueueListeners.add(clientStateController);

        return keyQueueListeners;
    }
}
