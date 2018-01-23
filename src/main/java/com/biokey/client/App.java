package com.biokey.client;

import com.biokey.client.providers.AppProvider;
import com.biokey.client.services.ClientInitService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point into the BioKey client.
 */
public class App {

    public static void main( String[] args )
    {
        // Create log file.
        setLogFile();

        // Load Spring context.
        ApplicationContext springContext = new AnnotationConfigApplicationContext(AppProvider.class);

        // Retrieve client state and load into program to get all services running.
        ClientInitService clientInitService = springContext.getBean(ClientInitService.class);
        clientInitService.retrieveClientState();
    }

    private static void setLogFile() {
        Path logFilePath = Paths.get(System.getProperty("user.dir"), "logs", "biokey-client.log");
        System.setProperty("biokey.logFilePath", logFilePath.toString());
    }
}
