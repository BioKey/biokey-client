package com.biokey.client.controllers.challenges;

import java.io.Serializable;

/**
 * Interface describing the actions that all challenge strategies must provide.
 */
public interface IChallengeStrategy extends Serializable {

    class ChallengeException extends Exception {
        ChallengeException(Exception e) {
            super(e);
        }
    }

    /**
     * Initialize this challenge strategy.
     */
    void init();

    /**
     * Get whether this challenge strategy has been initialized.
     *
     * @return true if the challenge strategy is initialized
     */
    boolean isInitialized();

    /**
     * Ask the challenge strategy to issue the challenge.
     *
     * @return true if the challenge was successfully issued
     */
    boolean issueChallenge();

    /**
     * Ask the challenge strategy to check whether the challenge passed.
     *
     * @return true if the attempt matched the challenge
     */
    boolean checkChallenge(String attempt);

    /**
     * Ask the challenge strategy to check whether the challenge is valid.
     *
     * @return true if the attempt is valid
     */
    boolean validateChallenge(String attempt);

    /**
     * @return the string representation that is consistent with the server.
     */
    String getServerRepresentation();

    /**
     * @return the string representation of any custom information the challenge should expose to the user.
     */
    String getCustomInformationText();
}
