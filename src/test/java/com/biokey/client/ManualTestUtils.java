package com.biokey.client;

import com.biokey.client.services.ClientInitService;
import org.apache.log4j.Logger;

import java.util.prefs.Preferences;

public class ManualTestUtils {

    private static Logger log = Logger.getLogger(ManualTestUtils.class);

    //TODO: Current Hack, will fix when unifying the view logic
    public static void main( String[] args ) {
        Preferences prefs = Preferences.userRoot().node(ClientInitService.class.getName());
        try {
            prefs.clear();
        } catch (Exception e) {
            log.debug("Could not clear Preferences.", e);
        }
    }
}
