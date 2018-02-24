package com.biokey.client.providers;

import com.biokey.client.views.frames.GoogleAuthFrameView;
import com.biokey.client.views.frames.LockFrameView;
import com.biokey.client.views.frames.TrayFrameView;
import com.biokey.client.views.panels.AnalysisResultTrayPanelView;
import com.biokey.client.views.panels.GoogleAuthPanelView;
import com.biokey.client.views.panels.LoginPanelView;
import com.biokey.client.views.panels.TextMessagePanelView;
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
    public GoogleAuthFrameView googleAuthFrameView() {
        return new GoogleAuthFrameView();
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
    public GoogleAuthPanelView googleAuthPanelView() {
        return new GoogleAuthPanelView();
    }

    @Bean
    public TextMessagePanelView textMessagePanelView() {
        return new TextMessagePanelView();
    }

    @Bean
    public AnalysisResultTrayPanelView analysisResultTrayPanelView() {
        return new AnalysisResultTrayPanelView();
    }
}
