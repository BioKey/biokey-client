package com.biokey.client;

import com.biokey.client.models.ClientStateModel;
import com.biokey.client.providers.AppProvider;
import com.biokey.client.services.ClientInitService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Set;

/**
 * Entry point into the BioKey client.
 */
public class App {

    public static void main( String[] args )
    {
        // Load Spring context.
        ApplicationContext springContext = new AnnotationConfigApplicationContext(AppProvider.class);

        ClientStateModel model = springContext.getBean(ClientStateModel.class);

        // Add service 'status' listeners to model
        @SuppressWarnings("unchecked")
        Set<ClientStateModel.IClientStatusListener> serviceStatusListeners =
                (Set<ClientStateModel.IClientStatusListener>) springContext.getBean("statusListeners");
        model.setStatusListeners(serviceStatusListeners);

        // Add service 'key queue' listeners to model
        @SuppressWarnings("unchecked")
        Set<ClientStateModel.IClientKeyListener> keyQueueListeners =
                (Set<ClientStateModel.IClientKeyListener>) springContext.getBean("keyQueueListeners");
        model.setKeyQueueListeners(keyQueueListeners);

        // Add service 'analysis results queue' listeners to model
        @SuppressWarnings("unchecked")
        Set<ClientStateModel.IClientAnalysisListener> analysisQueueListeners =
                (Set<ClientStateModel.IClientAnalysisListener>) springContext.getBean("analysisQueueListeners");
        model.setAnalysisResultQueueListeners(analysisQueueListeners);

        // Retrieve client state and load into program to get all services running.
        ClientInitService clientInitService = springContext.getBean(ClientInitService.class);
        clientInitService.retrieveClientState();
    }
}
