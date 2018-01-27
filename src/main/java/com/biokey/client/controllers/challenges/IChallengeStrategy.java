package com.biokey.client.controllers.challenges;

/**
 * Interface describing the actions that all challenge strategies must provide.
 */
public interface IChallengeStrategy {

    /**
     * Ask the challenge strategy to issue the challenge and return whether the user was successful.
     *
     * @return whether the challenge was successfully completed by the user
     */
    boolean performChallenges();
}
