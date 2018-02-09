package com.biokey.client.controllers.challenges;

import java.io.Serializable;

/**
 * Interface describing the actions that all challenge strategies must provide.
 */
public interface IChallengeStrategy extends Serializable {

    /**
     * Ask the challenge strategy to issue the challenge and return whether the user was successful.
     *
     * @return whether the challenge was successfully completed by the user
     */
    boolean performChallenges(String challenge);

    /**
     * Return the string representation that is consistent with the server.
     */
    String getServerRepresentation();
}
