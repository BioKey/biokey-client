package com.biokey.client;

import com.biokey.client.providers.AppProvider;
import com.biokey.client.services.ClientInitService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Entry point into the BioKey client.
 */
public class App
{
    public static void main( String[] args )
    {
        ApplicationContext springContext = new AnnotationConfigApplicationContext(AppProvider.class);

        ClientInitService clientInitService = springContext.getBean(ClientInitService.class);
        clientInitService.retrieveClientState();
    }
}
