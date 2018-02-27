package com.biokey.client.providers;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.controllers.challenges.GoogleAuthStrategy;
import com.biokey.client.controllers.challenges.IChallengeStrategy;
import com.biokey.client.controllers.challenges.TextMessageStrategy;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.views.frames.GoogleAuthQRFrameView;
import com.biokey.client.views.panels.challenges.ChallengePanelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class that provides the singleton instances of the challenge strategies.
 */
@Configuration
@Import({ClientStateProvider.class, HelperProvider.class, ViewProvider.class})
public class ChallengeProvider {
    @Bean
    @Autowired
    public IChallengeStrategy googleAuthStrategy(
            ClientStateModel clientStateModel, ClientStateController clientStateController,
            ServerRequestExecutorHelper serverRequestExecutorHelper, GoogleAuthQRFrameView googleAuthQRFrameView) {

        return new GoogleAuthStrategy(clientStateModel, clientStateController, serverRequestExecutorHelper, googleAuthQRFrameView);
    }

    @Bean
    @Autowired
    public IChallengeStrategy textMessageStrategy(ClientStateModel clientStateModel) {
        return new TextMessageStrategy(clientStateModel);
    }

    @Bean(name="allSupportedStrategies")
    @Autowired
    public Map<String, IChallengeStrategy> allSupportedStrategies(IChallengeStrategy googleAuthStrategy,
                                                                  IChallengeStrategy textMessageStrategy) {
        Map<String, IChallengeStrategy> strategies = new HashMap<>();
        strategies.put(googleAuthStrategy.getServerRepresentation(), googleAuthStrategy);
        strategies.put(textMessageStrategy.getServerRepresentation(), textMessageStrategy);

        return strategies;
    }

    @Bean(name="strategyViewPairs")
    @Autowired
    public Map<IChallengeStrategy, ChallengePanelView> strategyViewPairs(
            IChallengeStrategy googleAuthStrategy, IChallengeStrategy textMessageStrategy,
            ChallengePanelView googleAuthPanelView, ChallengePanelView textMessagePanelView) {

        Map<IChallengeStrategy, ChallengePanelView> pairs = new HashMap<>();
        pairs.put(googleAuthStrategy, googleAuthPanelView);
        pairs.put(textMessageStrategy, textMessagePanelView);

        return pairs;
    }

}
