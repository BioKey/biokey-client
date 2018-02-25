package com.biokey.client.views.panels.challenges;

import javax.swing.JPanel;
import java.awt.event.ActionListener;

public interface ChallengePanelView {

    /**
     * Getter method for the panel.
     *
     * @return the panel
     */
    JPanel getChallengePanel();

    /**
     * Getter method for the string representation of the user's typed code.
     * @return the string representation of the user's typed code
     */
    String getCode();

    /**
     * Setter method to add a new action listener to the send button.
     * @param l the action listener added to the send button
     */
    void addSendAction(ActionListener l);

    /**
     * Setter method to add a new action listener to the submit button.
     * @param l the action listener added to the submit button
     */
    void addSubmitAction(ActionListener l);

    /**
     * Setter method to add a new action listener to the resend button.
     * @param l the action listener added to the resend button
     */
    void addResendAction(ActionListener l);

    /**
     * Setter method to add a new action listener to the alt strategy button.
     * @param l the action listener added to the alt strategy button
     */
    void addAltAction(ActionListener l);

    /**
     * Setter method to change the enabled status of the send button.
     * @param enable true if the button should be enabled
     */
    void setEnableSend(boolean enable);

    /**
     * Setter method to change the enabled status of the submit button.
     * @param enable true if the button should be enabled
     */
    void setEnableSubmit(boolean enable);

    /**
     * Setter method to change the enabled status of the resend button.
     * @param enable true if the button should be enabled
     */
    void setEnableResend(boolean enable);

    /**
     * Setter method to change the enabled status of the alt strategy button.
     * @param enable true if the button should be enabled
     */
    void setEnableAlt(boolean enable);

    /**
     * Setter method to display text to the user.
     * @param newInfo the new text to display
     */
    void setInformationText(String newInfo);

    /**
     * Clears the code.
     */
    void clearCode();
}
