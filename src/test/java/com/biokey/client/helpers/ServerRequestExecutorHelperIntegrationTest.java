package com.biokey.client.helpers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ServerRequestExecutorHelperIntegrationTest {

    // Tests require an internet connection and assumes some common websites are not down.
    private final String GET_REQUEST_URL = "https://google.com";
    private final String BAD_GET_REQUEST_URL = "https://hoshweinstein.com";

    // To test async code, we will use a flag to determine when code is complete as well as a timeout.
    private CountDownLatch testCompleteFlag;
    private final int TEST_TIMEOUT = 1000;

    private final ServerRequestExecutorHelper underTest = new ServerRequestExecutorHelper(Executors.newCachedThreadPool());

    @Before
    public void resetFlag() {
        testCompleteFlag = new CountDownLatch(1);
    }

    public void waitForCompletion() {
        try {
            if (!testCompleteFlag.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS)) {
                System.out.println("Test timed out.");
                fail();
            }
        } catch (InterruptedException e) {
            System.out.println("Unexpected interruption.");
        }
    }

    @Test
    public void GIVEN_realUrl_WHEN_submitGetRequest_THEN_success() {
        underTest.submitGetRequest(GET_REQUEST_URL, new HttpHeaders(), String.class,
                (ResponseEntity<String> response) -> {
                    assertTrue("Response should be 200 OK", response.getStatusCodeValue() == 200);
                    assertTrue("Response body should not be null", response.getBody() != null);
                    assertTrue("Response body should have some characters", response.getBody().length() > 0);
                    testCompleteFlag.countDown();
                });
        waitForCompletion();
    }

    @Test
    public void GIVEN_badUrl_WHEN_submitGetRequest_THEN_success() {
        underTest.submitGetRequest(BAD_GET_REQUEST_URL, new HttpHeaders(), String.class,
                (ResponseEntity<String> response) -> {
                    assertTrue("Response should be 400 BAD REQUEST", response.getStatusCodeValue() == 400);
                    assertTrue("Response body should be null", response.getBody() == null);
                    testCompleteFlag.countDown();
                });
        waitForCompletion();
    }
}
