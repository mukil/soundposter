package com.soundposter.plugin;


import com.soundposter.plugin.service.SoundposterService;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.core.service.annotation.ConsumesService;
import java.util.logging.Logger;
import javax.ws.rs.*;

/**
 *
 * Soundposter Webservice running on <http://www.soundposter.com>
 * @version 1.0-SNAPSHOT
 * @author Copyright 2013, Malte Reißig <malte@mikromedia.de>
 *
 * Last modified: Jul 17, 2013
 */

@Path("/service")
@Consumes("application/json")
@Produces("application/json")
public class SoundposterPlugin extends PluginActivator implements SoundposterService {

    private Logger log = Logger.getLogger(getClass().getName());
    private AccessControlService acService;

    /** Implementing a soundposter search would trigger the following getTopic-Calls() to fulltext_key indexed types:
     *  Sound name, Sound Description, Set Name, Set Description, Poster Subtitle, Poster Description, Publisher Name,
     *  Album Name, Artist Name, futurewise: Tags, too and at last probably: com.soundposter.account_name
     **/

    /** --- Implementing PluginService Interfaces to consume AccessControlService --- */

    @Override
    @ConsumesService("de.deepamehta.plugins.accesscontrol.service.AccessControlService")
    public void serviceArrived(PluginService service) {
        if (service instanceof AccessControlService) {
            acService = (AccessControlService) service;
        }
    }

    @Override
    @ConsumesService("de.deepamehta.plugins.accesscontrol.service.AccessControlService")
    public void serviceGone(PluginService service) {
        if (service == acService) {
            acService = null;
        }
    }

}
