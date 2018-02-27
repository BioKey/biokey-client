package com.biokey.client.providers;

import com.biokey.client.views.frames.GoogleAuthQRFrameView;
import com.biokey.client.views.frames.LockFrameView;
import com.biokey.client.views.frames.TrayFrameView;
import com.biokey.client.views.panels.AnalysisResultTrayPanelView;
import com.biokey.client.views.panels.LockedPanelView;
import com.biokey.client.views.panels.challenges.ChallengeOptionPanelView;
import com.biokey.client.views.panels.challenges.ChallengePanelView;
import com.biokey.client.views.panels.LoginPanelView;
import com.biokey.client.views.panels.challenges.GoogleAuthChallengePanelView;
import com.biokey.client.views.panels.challenges.TextMessageChallengePanelView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that provides the singleton instances of the views of the BioKey client .
 */
@Configuration
public class ViewProvider {
    @Bean
    public LockFrameView lockFrameView() {
        return new LockFrameView();
    }

    @Bean
    public GoogleAuthQRFrameView googleAuthFrameView() {
        return new GoogleAuthQRFrameView();
    }

    @Bean
    public TrayFrameView trayMenu() {
        return new TrayFrameView();
    }

    @Bean
    public LoginPanelView loginPanelView() {
        return new LoginPanelView();
    }

    @Bean
    public ChallengePanelView googleAuthPanelView() {
        return new GoogleAuthChallengePanelView();
    }

    @Bean
    public ChallengePanelView textMessagePanelView() {
        return new TextMessageChallengePanelView();
    }

    @Bean
    public AnalysisResultTrayPanelView analysisResultTrayPanelView() {
        return new AnalysisResultTrayPanelView();
    }

    @Bean
    public ChallengeOptionPanelView challengeOptionPanelView() {
        return new ChallengeOptionPanelView();
    }

    @Bean
    public LockedPanelView lockedPanelView() {
        return new LockedPanelView();
    }
}
