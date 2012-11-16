package com.soundposter.plugin.service;

import de.deepamehta.core.Topic;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.PluginService;
import java.io.InputStream;

public interface SoundposterService extends PluginService {
    
    Topic getSoundposter(String profileAlias, String posterAlias, ClientState clientState);

    InputStream getSoundposterView(String profileAlias, String posterAlias, ClientState clientState);

}
