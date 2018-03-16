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
                                                      ClientStatusPojo currentStatus,
                                                      @NonNull String token) {
        return castToClientStatus(responseContainer, currentStatus, token, AuthConstants.AUTHENTICATED);
    }

    /**
     * Cast the response from the server to a new client status.
     *
     * @param responseContainer response from the server
     * @param token the access token used to call the server
     * @param authStatus the new auth status
     * @return new client status based on response
     */
    public static ClientStatusPojo castToClientStatus(@NonNull TypingProfileContainerResponse responseContainer,
                                                      ClientStatusPojo currentStatus,
                                                      @NonNull String token, @NonNull AuthConstants authStatus) {
        TypingProfileResponse response = responseContainer.getTypingProfile();
        if (response == null) return null;
        return new ClientStatusPojo(
                new TypingProfilePojo(response.get_id(), response.getMachine(), response.getUser(),
                        (response.getTensorFlowModel() == null) ? (currentStatus == null ? null : currentStatus.getProfile().getModel()) : response.getTensorFlowModel(),
                        response.getChallengeStrategies(),
                        response.getEndpoint()),
                authStatus,
                castToSecurityConstant(response.isLocked()),
                token,
                (responseContainer.getPhoneNumber() == null) ? "" : responseContainer.getPhoneNumber(),
                (responseContainer.getGoogleAuthKey() == null) ? "" : responseContainer.getGoogleAuthKey(),
                responseContainer.getTimeStamp());
    }

    /**
     * Get computer's MAC address as a string representation.
     *
     * @return string representation of MAC
     */
    public static String getMAC(){
        try {
            String OSName = System.getProperty("os.name");
            if (OSName.contains("Windows")) {
                return (getMAC4Windows());
            } else {
                String mac = getMAC4Linux("eth0");
                if (mac == null) {
                    mac = getMAC4Linux("eth1");
                    if (mac == null) {
                        mac = getMAC4Linux("eth2");
                        if (mac == null) {
                            mac = getMAC4Linux("usb0");
                            if (mac == null) {
                                mac = getFirstMAC();
                            }
                        }
                    }
                }
                return mac;
            }
        } catch (Exception E) {
            System.err.println("System Mac Exp : " + E.getMessage());
            return null;
        }
    }

    /**
     * Method for get MAc of Linux Machine
     * @param name
     * @return
     */
    private static String getMAC4Linux(String name){
        try {
            NetworkInterface network = NetworkInterface.getByName(name);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return (sb.toString());
        } catch (Exception E) {
            System.err.println("System Linux MAC Exp : " + E.getMessage());
            return null;
        }
    }

    /**
     * Method for first MAC address
     * @return
     */
    private static String getFirstMAC(){
        try {
            NetworkInterface network = NetworkInterface.getNetworkInterfaces().nextElement();
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return (sb.toString());
        } catch (Exception E) {
            System.err.println("System Index MAC Exp : " + E.getMessage());
            return null;
        }
    }

    /**
     * Method for get Mac Address of Windows Machine
     *
     * @return
     */
    private static String getMAC4Windows() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(addr);

            byte[] mac = network.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }

            return sb.toString();
        } catch (Exception E) {
            System.err.println("System Windows MAC Exp : " + E.getMessage());
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
     * Cast the boolean representation of security constant to the correct enum object.
     *
     * @param isLocked boolean representation of security constant.
     * @return the correct enum object
     */
    public static SecurityConstants castToSecurityConstant(boolean isLocked) {
        return (isLocked) ? SecurityConstants.LOCKED : SecurityConstants.UNLOCKED;
    }

    /**
     * Returns a new status with the authStatus set to the new authStatus.
     *
     * @param currentStatus the current status
     * @param newAuth the new authStatus
     */
    public static ClientStatusPojo createStatus(@NonNull ClientStatusPojo currentStatus, @NonNull AuthConstants newAuth) {
        return new ClientStatusPojo(currentStatus.getProfile(), newAuth, currentStatus.getSecurityStatus(),
                currentStatus.getAccessToken(), currentStatus.getPhoneNumber(), currentStatus.getGoogleAuthKey(),
                System.currentTimeMillis());
    }

    /**
     * Returns a new status with the securityStatus set to the new securityStatus.
     *
     * @param currentStatus the current status
     * @param newSecurity the new securityStatus
     */
    public static ClientStatusPojo createStatus(@NonNull ClientStatusPojo currentStatus, @NonNull SecurityConstants newSecurity) {
        return new ClientStatusPojo(currentStatus.getProfile(), currentStatus.getAuthStatus(), newSecurity,
                currentStatus.getAccessToken(), currentStatus.getPhoneNumber(), currentStatus.getGoogleAuthKey(),
                System.currentTimeMillis());
    }

    /**
     * Returns a new status with new phone number and google authentication key.
     *
     * @param currentStatus the current status
     * @param phoneNumber the new phone number
     * @param googleAuthKey the new Google authentication key
     */
    public static ClientStatusPojo createStatus(@NonNull ClientStatusPojo currentStatus, String phoneNumber, String googleAuthKey) {
        String notNullPhoneNumber = (phoneNumber == null) ? "" : phoneNumber;
        String notNullAuthKey = (googleAuthKey == null) ? "" : googleAuthKey;
        return new ClientStatusPojo(currentStatus.getProfile(), currentStatus.getAuthStatus(), currentStatus.getSecurityStatus(),
                currentStatus.getAccessToken(), notNullPhoneNumber, notNullAuthKey, System.currentTimeMillis());
    }
}
