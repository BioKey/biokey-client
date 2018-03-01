package com.biokey.client.helpers;

import com.biokey.client.constants.AppConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.models.pojo.AnalysisResultPojo;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.KeyStrokesPojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import com.biokey.client.models.response.TypingProfileContainerResponse;
import com.biokey.client.models.response.TypingProfileResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Queue;

public class RequestBuilderHelper {

    /**
     * Create header map with the current status' access token added.
     *
     * @param token the custom token
     * @return header map with the current status' access token as the only element
     */
    public static HttpHeaders headerMapWithToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AppConstants.SERVER_TOKEN_HEADER, token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Create empty header map specifying content type application/json.
     *
     * @return header map specifying application/json
     */
    public static HttpHeaders emptyHeaderMap(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Create request body for POST keystrokes.
     *
     * @param keysToSend the keys to serialize
     * @param typingProfileId the typing profile id to attach to each serialized key
     * @return string representing the request body for POST keystrokes
     * @throws JsonProcessingException if serialization of keystrokes to JSON has failed
     */
    public static String requestBodyToPostKeystrokes(KeyStrokesPojo keysToSend, String typingProfileId) throws JsonProcessingException {
        ObjectWriter writer = new ObjectMapper().writerFor(Queue.class).withAttribute("typingProfile", typingProfileId);
        return "{\"keystrokes\": " + writer.writeValueAsString(keysToSend.getKeyStrokes()) + "}";
    }

    /**
     * Create request body for POST login.
     *
     * @param email    the email of the user to be logged in
     * @param password the password of the user to be logged in
     * @return string representing the request body for POST keystrokes
     */
    public static String requestBodyToPostLogin(@NonNull String email, @NonNull String password) {
        return "{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}";
    }

    /**
     * Create request body for POST client status.
     *
     * @param clientStatus the client status to serialize
     * @return string representing the request body for POST status
     * @throws JsonProcessingException if serialization of status to JSON has failed
     */
    public static String requestBodyToPostClientStatus(ClientStatusPojo clientStatus) throws JsonProcessingException {
        TypingProfilePojo typingProfile = clientStatus.getProfile();
        TypingProfileContainerResponse req = new TypingProfileContainerResponse(
                new TypingProfileResponse(typingProfile.getId(), typingProfile.getUserId(), typingProfile.getMachineId(),
                        clientStatus.getSecurityStatus() != SecurityConstants.UNLOCKED,
                        typingProfile.getModel(),
                        typingProfile.getSqsEndpoint(),
                        typingProfile.getAcceptedChallengeStrategies(),
                        typingProfile.getThreshold()),
                clientStatus.getPhoneNumber(),
                clientStatus.getGoogleAuthKey(),
                clientStatus.getTimeStamp());

        return new ObjectMapper().writeValueAsString(req);
    }

    /**
     * Create request body for POST analysis result.
     *
     * @param analysisResult the analysis result to serialize
     * @param typingProfileId the typing profile id to attach to serialized result
     * @return string representing the request body for POST result
     * @throws JsonProcessingException if serialization of result to JSON has failed
     */
    public static String requestBodyToPostAnalysisResult(AnalysisResultPojo analysisResult, String typingProfileId) throws JsonProcessingException {
        ObjectWriter writer = new ObjectMapper().writerFor(Object.class).withAttribute("typingProfile", typingProfileId);
        return writer.writeValueAsString(analysisResult);
    }
}

