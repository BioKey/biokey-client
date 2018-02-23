package com.biokey.client.providers;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration class that provides all the beans for BioKey client.
 */
@Configuration
@Import({ServiceProvider.class, ChallengeProvider.class, ClientStateProvider.class, HelperProvider.class, ViewProvider.class})
public class AppProvider {
}
