package com.biokey.client.providers;

import com.biokey.client.controllers.challenges.GoogleAuthStrategy;
import com.biokey.client.controllers.challenges.IChallengeStrategy;
import com.biokey.client.controllers.challenges.TextMessageStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class that provides the singleton instances of the challenge strategies.
 */
@Configuration
public class ChallengeProvider {
    @Bean
    public GoogleAuthStrategy googleAuthStrategy() {
        return new GoogleAuthStrategy();
    }

    @Bean
    public TextMessageStrategy textMessageStrategy() {
        return new TextMessageStrategy();
    }

    @Bean
    public Map<String, IChallengeStrategy> allSupportedAuthStrategies() {
        Map<String, IChallengeStrategy> strategies = new HashMap<>();
        strategies.put(googleAuthStrategy().getServerRepresentation(), googleAuthStrategy());
        strategies.put(textMessageStrategy().getServerRepresentation(), textMessageStrategy());
        return strategies;
    }

}
