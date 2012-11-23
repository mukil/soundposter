package com.soundposter.plugin.service;

import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.ResultSet;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.PluginService;
import java.io.InputStream;

public interface SoundposterService extends PluginService {
    
    Topic getSoundposter(String profileAlias, String posterAlias, ClientState clientState);
    
    Topic getRandomPublishedSoundposter(ClientState clientState);
    
    String getPublishedSoundposterUrl(String posterId, ClientState clientState);
    
    String createSignupInformation(String signup, String name, ClientState clientState);
    
    ResultSet<RelatedTopic> getAllPublishedSoundposter(ClientState clientState);

    InputStream getSoundposterView(String profileAlias, String posterAlias, ClientState clientState);
    
    InputStream getWebsiteView(String pathInfo, ClientState clientState);
    
    // InputStream getRootView(ClientState clientState);
    
    InputStream getSiteIcon(ClientState clientState);

}