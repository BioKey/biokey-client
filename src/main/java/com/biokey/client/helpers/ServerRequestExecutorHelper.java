package com.biokey.client.helpers;

import lombok.NonNull;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * Helper class that uses Spring RestTemplate to make HTTP requests to the server.
 */
public class ServerRequestExecutorHelper {

    private static Logger log = Logger.getLogger(ServerRequestExecutorHelper.class);

    public interface ServerResponseHandler<T> {
        void handleResponse(ResponseEntity<T> response);
    }

    private RestTemplate rt = new RestTemplate();

    /**
     * Executor that will use a worker or pool of worker threads to make HTTP requests asynchronously.
     */
    private ExecutorService executor;

    public ServerRequestExecutorHelper(ExecutorService executor) {
        this.executor = executor;
        this.rt.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    /**
     * Submits a HTTP GET Request to the provided url, and expect a response back matching the template.
     * Allow caller to provide a lambda to execute on the response.
     *
     * @param url url to send request to
     * @param responseTemplate the expected template that the response should be cast to
     * @param handler the lambda that will be passed the server response
     * @param <T> the type of the responseTemplate
     */
    public <T> void submitGetRequest(@NonNull String url,
                                     @NonNull HttpHeaders headers,
                                     @NonNull Class<T> responseTemplate,
                                     @NonNull ServerResponseHandler<T> handler) {
        executor.execute(() -> {
            // Add body and header to an Http Entity.
            HttpEntity<HashMap<String, String>> requestEntity = new HttpEntity<>(headers);

            // Pre-suppose that the response fails for whatever reason.
            ResponseEntity<T> response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            try {
                // Try getting a response in the form of responseTemplate from server represented by url.
                response = rt.exchange(url, HttpMethod.GET, requestEntity, responseTemplate);
            } catch (RestClientException e) {
                log.error("Exception when trying HTTP GET Request for: " + url, e);
            } finally {
                // Even if there was an exception, handle the response.
                handler.handleResponse(response);
            }
        });
    }

    /**
     * Submits a HTTP POST Request to the provided url, attaching the provided request body.
     * Allow caller to provide a lambda to execute on the response.
     *
     * @param url url to send request to
     * @param requestBody the key-value pairs to be attached to the request body
     * @param responseTemplate the expected template that the response should be cast to
     * @param handler the lambda that will be passed the server response
     * @param <T> the type of the responseTemplate
     */
    public <T> void submitPostRequest(@NonNull String url,
                                      @NonNull HttpHeaders headers,
                                      @NonNull String requestBody,
                                      @NonNull Class<T> responseTemplate,
                                      @NonNull ServerResponseHandler<T> handler) {
        executor.execute(() -> {
            // Add body and header to an Http Entity.
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            System.out.println (url);
            // Pre-suppose that the response fails for whatever reason.
            ResponseEntity<T> response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            try {
                // Try getting a response in the form of responseTemplate from server represented by url.
                response = rt.postForEntity(url, requestEntity, responseTemplate);
            } catch (RestClientException e) {
                log.error("Exception when trying HTTP POST Request for: " + url, e);
            } finally {
                // Even if there was an exception, handle the response.
                handler.handleResponse(response);
            }
        });
    }

    /**
     * Submits a HTTP PUT Request to the provided url, attaching the provided request body.
     * Allow caller to provide a lambda to execute on the response.
     *
     * @param url url to send request to
     * @param requestBody the key-value pairs to be attached to the request body
     * @param responseTemplate the expected template that the response should be cast to
     * @param handler the lambda that will be passed the server response
     * @param <T> the type of the responseTemplate
     */
    public <T> void submitPutRequest(@NonNull String url,
                                     @NonNull HttpHeaders headers,
                                     @NonNull String requestBody,
                                     @NonNull Class<T> responseTemplate,
                                     @NonNull ServerResponseHandler<T> handler) {
        executor.execute(() -> {
            // Add body and header to an Http Entity.
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            // Pre-suppose that the response fails for whatever reason.
            ResponseEntity<T> response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            try {
                // Try getting a response in the form of responseTemplate from server represented by url.
                response = rt.exchange(url, HttpMethod.PUT, requestEntity, responseTemplate);
            } catch (RestClientException e) {
                log.error("Exception when trying HTTP PUT Request for: " + url, e);
            } finally {
                // Even if there was an exception, handle the response.
                handler.handleResponse(response);
            }
        });
    }
}
