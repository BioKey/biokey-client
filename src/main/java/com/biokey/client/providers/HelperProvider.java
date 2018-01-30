package com.biokey.client.providers;

import com.biokey.client.helpers.RequestBuilderHelper;
import com.biokey.client.helpers.ServerRequestExecutorHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/**
 * Configuration class that provides the singleton instances of any helpers.
 */
@Configuration
public class HelperProvider {

    @Bean
    public ServerRequestExecutorHelper serverRequestExecutorHelper() {
        // Use a cached thread pool to save resources by reusing threads and also terminating threads if client is idle.
        return new ServerRequestExecutorHelper(Executors.newCachedThreadPool());
    }

    @Bean
    public RequestBuilderHelper requestBuilderHelper() {
        return new RequestBuilderHelper();
    }
}
