package com.biokey.client.providers;

import com.biokey.client.views.frames.LockFrameView;
import com.biokey.client.views.panels.LoginPanelView;
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
    public LoginPanelView loginPanelView() {
        return new LoginPanelView();
    }
}
