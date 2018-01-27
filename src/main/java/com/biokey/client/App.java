package com.biokey.client;

import com.biokey.client.models.ClientStateModel;
import com.biokey.client.providers.AppProvider;
import com.biokey.client.services.ClientInitService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Entry point into the BioKey client.
 */
public class App {

    public static void main( String[] args )
    {
        // Load Spring context.
        ApplicationContext springContext = new AnnotationConfigApplicationContext(AppProvider.class);

        // Add service listeners to model
        @SuppressWarnings("unchecked")
        Set<ClientStateModel.IClientStateListener> serviceListeners =
                (Set<ClientStateModel.IClientStateListener>) springContext.getBean("serviceListeners");
        springContext.getBean(ClientStateModel.class).setStateListeners(serviceListeners);

        // Retrieve client state and load into program to get all services running.
        ClientInitService clientInitService = springContext.getBean(ClientInitService.class);
        clientInitService.retrieveClientState();
    }
}
