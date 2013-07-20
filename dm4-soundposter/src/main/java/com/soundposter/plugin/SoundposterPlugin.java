package com.soundposter.plugin;

import de.deepamehta.core.service.ClientState;

import com.soundposter.plugin.service.SoundposterService;
import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.ResultSet;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.core.service.accesscontrol.ACLEntry;
import de.deepamehta.core.service.accesscontrol.AccessControlList;
import de.deepamehta.core.service.accesscontrol.Operation;
import de.deepamehta.core.service.accesscontrol.UserRole;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.core.service.annotation.ConsumesService;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
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
    private boolean isInitialized = false;

    /** Initialize the migrated soundsets ACL-Entries. */
    @Override
    public void init() {
        isInitialized = true;
        configureIfReady();
        // setupRenderContext();
    }

    private void configureIfReady() {
        if (isInitialized) {
            checkACLsOfMigration();
        }
    }

    private void checkACLsOfMigration() {
        ResultSet<RelatedTopic> sounds = dms.getTopics("com.soundposter.sound", false, 0, null);
        Iterator<RelatedTopic> soundset = sounds.getIterator();
        while (soundset.hasNext()) {
            RelatedTopic sound = soundset.next();
            if (acService.getCreator(sound.getId()) == null) {
                log.info("initial ACL update of imported soundtrack " + sound.getSimpleValue().toString());
                Topic admin = acService.getUsername("admin");
                String adminName = admin.getSimpleValue().toString();
                acService.setCreator(sound.getId(), adminName);
                acService.setOwner(sound.getId(), adminName);
                acService.createACL(sound.getId(), new AccessControlList( //
                        new ACLEntry(Operation.WRITE, UserRole.OWNER)));
            }
        }
    }

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
