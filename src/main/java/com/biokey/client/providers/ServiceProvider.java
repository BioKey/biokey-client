package com.biokey.client.providers;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.controllers.challenges.IChallengeStrategy;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.services.*;
import com.biokey.client.views.frames.LockFrameView;
import com.biokey.client.views.frames.TrayFrameView;
import com.biokey.client.views.panels.AnalysisResultTrayPanelView;
import com.biokey.client.views.panels.LockedPanelView;
import com.biokey.client.views.panels.challenges.ChallengeOptionPanelView;
import com.biokey.client.views.panels.challenges.ChallengePanelView;
import com.biokey.client.views.panels.LoginPanelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration class that provides the singleton instances of the services that are BioKey client's backbone.
 */
@Configuration
@Import({ClientStateProvider.class, ViewProvider.class, ChallengeProvider.class})
public class ServiceProvider {

    @Resource(name="allSupportedStrategies")
    private Map<String, IChallengeStrategy> allSupportedStrategies;
    @Resource(name="strategyViewPairs")
    private Map<IChallengeStrategy, ChallengePanelView> strategyViewPairs;

    @Bean
    @Autowired
    public ClientInitService clientInitService(
            ClientStateController clientStateController, ClientStateModel clientStateModel,
            LockFrameView lockFrameView, LoginPanelView loginPanelView, TrayFrameView trayFrameView) {

        return new ClientInitService(clientStateController, clientStateModel, lockFrameView, loginPanelView, trayFrameView);
    }

    @Bean
    @Autowired
    public AnalysisEngineService analysisEngineService(
            ClientStateController clientStateController, TrayFrameView trayFrameView,
            AnalysisResultTrayPanelView analysisResultTrayPanelView) {

        return new AnalysisEngineService(clientStateController, trayFrameView, analysisResultTrayPanelView);
    }

    @Bean
    @Autowired
    public KeyloggerDaemonService keyloggerDaemonService(ClientStateController clientStateController) {
        return new KeyloggerDaemonService(clientStateController);
    }

    @Bean
    @Autowired
    public ChallengeService challengeService(
            ClientStateController clientStateController, ClientStateModel clientStateModel,
            LockFrameView lockFrameView, ChallengeOptionPanelView challengeOptionPanelView, LockedPanelView lockedPanelView) {

        return new ChallengeService(clientStateController, clientStateModel,
                lockFrameView, challengeOptionPanelView, lockedPanelView, allSupportedStrategies, strategyViewPairs);
    }

    @Bean
    @Autowired
    public ServerListenerService serverListenerService(ClientStateController clientStateController, ClientStateModel clientStateModel) {
        return new ServerListenerService(clientStateController, clientStateModel);
    }

    @Bean
    @Autowired
    public Set<ClientStateModel.IClientStatusListener> statusListeners(
            ClientInitService clientInitService, AnalysisEngineService analysisEngineService,
            KeyloggerDaemonService keyloggerDaemonService, ChallengeService challengeService,
            ServerListenerService serverListenerService) {

        Set<ClientStateModel.IClientStatusListener> serviceStatusListeners = new HashSet<>();
        serviceStatusListeners.add(clientInitService);
        serviceStatusListeners.add(analysisEngineService);
        serviceStatusListeners.add(keyloggerDaemonService);
        serviceStatusListeners.add(challengeService);
        serviceStatusListeners.add(serverListenerService);

        return serviceStatusListeners;
    }

    @Bean
    @Autowired
    public Set<ClientStateModel.IClientAnalysisListener> analysisQueueListeners(ClientInitService clientInitService, ChallengeService challengeService) {
        Set<ClientStateModel.IClientAnalysisListener> analysisQueueListeners = new HashSet<>();
        analysisQueueListeners.add(challengeService);
        analysisQueueListeners.add(clientInitService);

        return analysisQueueListeners;
    }

    @Bean
    @Autowired
    public Set<ClientStateModel.IClientKeyListener> keyQueueListeners(ClientInitService clientInitService, AnalysisEngineService analysisEngineService) {
        Set<ClientStateModel.IClientKeyListener> keyQueueListeners = new HashSet<>();
        keyQueueListeners.add(analysisEngineService);
        keyQueueListeners.add(clientInitService);

        return keyQueueListeners;
    }
}
