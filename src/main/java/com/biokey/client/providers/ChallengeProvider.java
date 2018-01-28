package com.biokey.client.providers;

import com.biokey.client.controllers.ClientStateController;
import com.biokey.client.controllers.challenges.GoogleAuthStrategy;
import com.biokey.client.controllers.challenges.TextMessageStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}
