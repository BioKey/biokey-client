package com.biokey.client.providers;

import com.biokey.client.controllers.challenges.GoogleAuthStrategy;
import com.biokey.client.controllers.challenges.IChallengeStrategy;
import com.biokey.client.controllers.challenges.TextMessageStrategy;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import com.biokey.client.models.ClientStateModel;
import com.biokey.client.views.frames.GoogleAuthFrameView;
import com.biokey.client.views.panels.GoogleAuthPanelView;
import com.biokey.client.views.panels.TextMessagePanelView;
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
    public GoogleAuthStrategy googleAuthStrategy(ClientStateModel clientStateModel,
                                                 ServerRequestExecutorHelper serverRequestExecutorHelper,
                                                 GoogleAuthFrameView googleAuthFrameView) {
        return new GoogleAuthStrategy(clientStateModel, serverRequestExecutorHelper, googleAuthFrameView);
    }

    @Bean
    @Autowired
    public TextMessageStrategy textMessageStrategy(ClientStateModel clientStateModel) {
        return new TextMessageStrategy(clientStateModel);
    }

    @Bean
    @Autowired
    public Map<String, IChallengeStrategy> allSupportedAuthStrategies(ClientStateModel clientStateModel,
                                                                      ServerRequestExecutorHelper serverRequestExecutorHelper,
                                                                      GoogleAuthFrameView googleAuthFrameView) {
        Map<String, IChallengeStrategy> strategies = new HashMap<>();
        strategies.put(googleAuthStrategy(clientStateModel, serverRequestExecutorHelper, googleAuthFrameView).getServerRepresentation(),
                googleAuthStrategy(clientStateModel, serverRequestExecutorHelper, googleAuthFrameView));
        strategies.put(textMessageStrategy(clientStateModel).getServerRepresentation(), textMessageStrategy(clientStateModel));

        return strategies;
    }

}
