package com.soundposter.plugin;

import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.PluginService;

import com.soundposter.plugin.service.SoundposterService;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.ResultSet;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.service.event.PluginServiceArrivedListener;
import de.deepamehta.core.service.event.PluginServiceGoneListener;

import java.io.InputStream;
import java.util.logging.Logger;
import javax.ws.rs.*;

/**
 *
 * Soundposter Webservice
 * 
 * @author Malte Reißig <malte@mikromedia.de>
 * 
 */

@Path("/")
@Consumes("application/json")
@Produces("application/json")
public class SoundposterPlugin extends PluginActivator implements SoundposterService, PluginServiceArrivedListener, 
                                                                                PluginServiceGoneListener {
    private Logger log = Logger.getLogger(getClass().getName());


    @GET
    @Path("/{profile}/{poster}")
    @Produces("text/html")
    @Override
    public InputStream getSoundposterView(@PathParam("profile") String profileAlias, 
        @PathParam("poster") String posterAlias, @HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter-view for profile: " + profileAlias + " and posterAlias" + posterAlias);
        return invokeSoundposterView();
    }
    
    @GET
    @Path("/poster/{profile}/{poster}")
    @Override
    public Topic getSoundposter(@PathParam("profile") String profileAlias, @PathParam("poster") String posterAlias, 
        @HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter-data for posterAlias" + posterAlias + " and profile " + profileAlias);
        Topic soundposter_alias = dms.getTopic("com.soundposter.web_alias", 
                new SimpleValue(posterAlias), true, clientState);
        if (soundposter_alias == null) {
            throw new WebApplicationException(new Throwable("Soundposter with web alias not found"), 404);
        }
        
        /** check if soundposter is published, otherwise return 401 */
        Topic soundposter = soundposter_alias.getRelatedTopic("dm4.core.composition", "dm4.core.part", "dm4.core.whole",
                "dm4.topicmaps.topicmap", true, false, null);
        if (soundposter == null) {
            log.warning("  soundposter \""+ posterAlias +"\" found but data could not be loaded! "
                    + "(composite whole missing)\r\n");
            throw new WebApplicationException(new Throwable("Soundposter data could not be loaded"), 500);
        }
        boolean published = soundposter.getCompositeValue().getBoolean("com.soundposter.published");
        if (!published) {
            log.warning("  soundposter \""+ posterAlias +"\" found but it is not yet published! "
                    + "(Access Denied)\r\n");
            throw new WebApplicationException(new Throwable("Soundposter is currently not published."), 401);
        }
        
        /** the following is yet dummy code */
        ResultSet<RelatedTopic> songs = soundposter.getRelatedTopics("dm4.topicmaps.topic_mapcontext", "dm4.core.part",
                "dm4.core.whole", "com.soundposter.sound", true, false, 0, null);
        if (songs != null) log.fine("  "+ songs.getSize() +" songs in soundposter");
        Topic profile = dms.getTopic("com.soundposter.account_alias", 
                new SimpleValue(profileAlias), true, clientState);
        if (profile == null) log.fine("  profile \""+ profileAlias +"\" not found \r\n");

        return soundposter;
    }
    
    // ------------------------------------------------------------------------------------------------ Private Methods

    private InputStream invokeSoundposterView() {
        try {
            return dms.getPlugin("com.soundposter.webapp").getResourceAsStream("web/poster/index.html");
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
    
    /** --- Implementing PluginService Interfaces to consume AccessControlService --- */

    // @Override
    public void pluginServiceArrived(PluginService service) {
        /** if (service instanceof AccessControlService) {
            acl = (AccessControlService) service;
        } else if (service instanceof WorkspacesService) {
            ws = (WorkspacesService) service;
        } **/
    }

    // @Override
    public void pluginServiceGone(PluginService service) {
        /** if (service == acl) {
            acl = null;
        } else if (service == ws) {
            ws = null;
        } **/
    }
    
}
