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

        // Add service 'status' listeners to model
        @SuppressWarnings("unchecked")
        Set<ClientStateModel.IClientStateListener> serviceStatusListeners =
                (Set<ClientStateModel.IClientStateListener>) springContext.getBean("serviceStatusListeners");
        springContext.getBean(ClientStateModel.class).setStateListeners(serviceStatusListeners);

        // Add service 'status queue' listeners to model
        @SuppressWarnings("unchecked")
        Set<ClientStateModel.IClientStatusQueueListener> serviceStatusQueueListeners =
                (Set<ClientStateModel.IClientStatusQueueListener>) springContext.getBean("serviceStatusQueueListeners");
        springContext.getBean(ClientStateModel.class).setStatusQueueListeners(serviceStatusQueueListeners);

        // Add service 'key queue' listeners to model
        @SuppressWarnings("unchecked")
        Set<ClientStateModel.IClientKeyListener> keyQueueListeners =
                (Set<ClientStateModel.IClientKeyListener>) springContext.getBean("keyQueueListeners");
        springContext.getBean(ClientStateModel.class).setKeyQueueListeners(keyQueueListeners);

        // Add service 'analysis results queue' listeners to model
        @SuppressWarnings("unchecked")
        Set<ClientStateModel.IClientAnalysisListener> analysisQueueListeners =
                (Set<ClientStateModel.IClientAnalysisListener>) springContext.getBean("analysisQueueListeners");
        springContext.getBean(ClientStateModel.class).setAnalysisResultQueueListeners(analysisQueueListeners);

        // Retrieve client state and load into program to get all services running.
        ClientInitService clientInitService = springContext.getBean(ClientInitService.class);
        clientInitService.retrieveClientState();
    }
}
