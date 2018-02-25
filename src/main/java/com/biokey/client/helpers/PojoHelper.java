package com.biokey.client.helpers;

import com.biokey.client.constants.AuthConstants;
import com.biokey.client.constants.SecurityConstants;
import com.biokey.client.controllers.challenges.IChallengeStrategy;
import com.biokey.client.models.pojo.ClientStatusPojo;
import com.biokey.client.models.pojo.TypingProfilePojo;
import com.biokey.client.models.response.TypingProfileContainerResponse;
import com.biokey.client.models.response.TypingProfileResponse;
import lombok.NonNull;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helps generate Pojos and convert String representations to Pojos.
 */
public class PojoHelper {

    /**
     * Cast the response from the server to a new client status.
     *
     * @param responseContainer response from the server
     * @param token the access token used to call the server
     * @return new client status based on response
     */
    public static ClientStatusPojo castToClientStatus(@NonNull TypingProfileContainerResponse responseContainer,
                                                      @NonNull String token) {
        TypingProfileResponse response = responseContainer.getTypingProfile();
        if (response == null) return null;

        return new ClientStatusPojo(
                new TypingProfilePojo(response.get_id(), response.getMachine(), response.getUser(),
                        response.getTensorFlowModel(),
                        response.getThreshold(),
                        response.getChallengeStrategies(),
                        response.getEndpoint()),
                AuthConstants.AUTHENTICATED,
                castToSecurityConstant(response.isLocked()),
                token,
                (responseContainer.getPhoneNumber() == null) ? "" : responseContainer.getPhoneNumber(),
                (responseContainer.getGoogleAuthKey() == null) ? "" : responseContainer.getGoogleAuthKey(),
                System.currentTimeMillis());
    }

    /**
     * Get computer's MAC address as a string representation.
     *
     * @return string representation of MAC
     */
    public static String getMAC() {
        try {
            byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
            return new String(mac);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Casts the string representations of challenge strategies from the server to the correct IChallengeStrategy impl.
     *
     * @param challengeStrategies array of string representations of challenge strategies from the server
     * @return array of accepted IChallengeStrategy impl
     */
    public static IChallengeStrategy[] castToChallengeStrategy(@NonNull Map<String, IChallengeStrategy> strategies, @NonNull String[] challengeStrategies) {
        if (challengeStrategies.length == 0) return null;

        List<IChallengeStrategy> acceptedStrategies = new ArrayList<>();
        for (String strategy : challengeStrategies) {
            // Check if strategy is in the master list and not yet in the accepted list.
            if (strategies.containsKey(strategy) && !acceptedStrategies.contains(strategies.get(strategy))) {
                acceptedStrategies.add(strategies.get(strategy));
            }
        }
        return acceptedStrategies.toArray(new IChallengeStrategy[acceptedStrategies.size()]);
    }

    /**
     * Casts the string representations of challenge strategies from the server to the correct IChallengeStrategy impl.
     *
     * @param challengeStrategies comma delimited string representation of challenge strategies from the server
     * @return array of accepted IChallengeStrategy impl
     */
    public static IChallengeStrategy[] castToChallengeStrategy(@NonNull Map<String, IChallengeStrategy> strategies, @NonNull String challengeStrategies) {
        if (challengeStrategies.length() == 0) return new IChallengeStrategy[0];
        return castToChallengeStrategy(strategies, challengeStrategies.split("\\s*,\\s*"));
    }

    /**
     * Casts the string representations of challenge strategies from the server to the array string representation.
     *
     * @param challengeStrategies comma delimited string representation of challenge strategies from the server
     * @return array of string representations of challenge strategies
     */
    public static String[] castToChallengeStrategyArray(@NonNull String challengeStrategies) {
        if (challengeStrategies.length() == 0) return new String[0];
        return challengeStrategies.split("\\s*,\\s*");
    }

    /**
     * Casts the string representations of thresholds from the server to the correct float array.
     *
     * @param threshold comma delimited string representations of thresholds strategies from the server
     * @return array of thresholds
     */
    public static float[] castToThreshold(@NonNull String threshold) {
        if (threshold.length() == 0) return new float[0];

        String[] thresholdStringArr = threshold.split("\\s*,\\s*");
        float[] thresholdArr = new float[thresholdStringArr.length];
        for (int i = 0; i < thresholdStringArr.length; i++) {
            thresholdArr[i] = Float.parseFloat(thresholdStringArr[i]);
        }
        return thresholdArr;
    }

    /**
     * Cast the boolean representation of security constant to the correct enum object.
     *
     * @param isLocked boolean representation of security constant.
     * @return the correct enum object
     */
    public static SecurityConstants castToSecurityConstant(boolean isLocked) {
        return (isLocked) ? SecurityConstants.LOCKED : SecurityConstants.UNLOCKED;
    }
}
