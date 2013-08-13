package com.soundposter.plugin;

import de.deepamehta.core.service.ClientState;

import com.soundposter.plugin.service.SoundposterService;
import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Association;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.ResultSet;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.model.TopicRoleModel;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.core.service.annotation.ConsumesService;
import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONTokener;

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
