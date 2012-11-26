package com.soundposter.plugin;

import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.ClientState;

import com.soundposter.plugin.service.SoundposterService;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.ResultSet;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicModel;
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
 * Soundposter Webservice
 * 
 * @author Malte Reißig <malte@mikromedia.de>
 * 
 */

@Path("/")
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
    }
    
    
    private void configureIfReady() {
        if (isInitialized) {
            checkACLsOfMigration();
        }
    }
    
    @GET
    @Path("/{profile}/{poster}")
    @Produces("text/html")
    @Override
    public InputStream getSoundposterView(@PathParam("profile") String profileAlias, 
        @PathParam("poster") String posterAlias, @HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter-view for profile: " + profileAlias + " and posterAlias" + posterAlias);
        if (profileAlias.equals("website")) return getWebsiteView(posterAlias, clientState);
        return invokeSoundposterView();
    }
    
    @GET
    @Path("/{pathInfo}")
    @Produces("text/html")
    @Override
    public InputStream getWebsiteView(@PathParam("pathInfo") String pathInfo,
        @HeaderParam("Cookie") ClientState clientState) {
        // log.info("Requesting website-view for pathInfo: " + pathInfo);
        if (pathInfo.equals("tsfestival")) return invokeSoundposterTorStreetView();
        if (pathInfo.equals("c3s")) return invokeC3SView();
        if (pathInfo.equals("favicon.ico")) return getSoundposterFavIcon();
        log.info("Requesting front page with pathInfo .. ");
        return invokeFrontpageView();
    }
    
    @GET
    @Path("/")
    @Produces("*/*") // FIXME
    @Override
    public InputStream getSiteIcon(@HeaderParam("Cookie") ClientState clientState) {
        // log.info("Requesting front page without pathinfo.. ");
        return invokeFrontpageView();
    }
    
    @GET
    @Path("/poster/{profile}/{poster}")
    @Override
    public Topic getSoundposter(@PathParam("profile") String profileAlias, @PathParam("poster") String posterAlias, 
        @HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter-data for posterAlias" + posterAlias + " and profile " + profileAlias);
        Topic soundposter_alias = dms.getTopic("com.soundposter.web_alias", 
                new SimpleValue(posterAlias), true, clientState);
        /** Soundposter Alias sanity check */
        if (soundposter_alias == null) { // not found
            throw new WebApplicationException(new Throwable("Soundposter with web alias not found"), 404);
        }
        
        /** check if soundposter is published, otherwise return 401 */
        Topic soundposter = soundposter_alias.getRelatedTopic("dm4.core.composition", "dm4.core.part", "dm4.core.whole",
                "dm4.topicmaps.topicmap", true, false, null);
        if (soundposter == null) { // internal server error
            log.warning("  soundposter \""+ posterAlias +"\" found but data could not be loaded! "
                    + "(composite whole missing)\r\n");
            throw new WebApplicationException(new Throwable("Soundposter data could not be loaded"), 500);
        }
        
        /** Soundposter isPublished sanity check */
        boolean published = soundposter.getCompositeValue().getBoolean("com.soundposter.published");
        if (!published) {
            log.warning("  soundposter \""+ posterAlias +"\" found but it is not yet published! "
                    + "(Access Denied)\r\n");
            throw new WebApplicationException(new Throwable("Soundposter is currently not published."), 401);
        }
        
        /** the following is yet dummy code, trying to retrieve songs independent from the topicmaps module*/
        ResultSet<RelatedTopic> songs = soundposter.getRelatedTopics("dm4.topicmaps.topic_mapcontext",
                "dm4.topicmaps.topicmap_topic", "dm4.core.whole", "com.soundposter.sound", true, false, 0, null);
        if (songs != null) log.fine("  "+ songs.getSize() +" songs in soundposter");

        /** Check if the poster was really authored by this profile alias */
        Topic profile = dms.getTopic("com.soundposter.account_alias",  
                new SimpleValue(profileAlias), true, clientState);
        if (profile == null) { // internal server error
            log.fine("  profile \""+ profileAlias +"\" not found \r\n");
            throw new WebApplicationException(new Throwable("Soundposter data could not be loaded"), 404);
        }

        /** last sanity check, there is a profile poster "author" relationship */
        if (!hasProfilePosterRelation(profile, soundposter)) {  // not found
            throw new WebApplicationException(new Throwable("Soundposter data could not be loaded"), 404);
        }
        return soundposter;
    }
    
    @GET
    @Path("/poster/published/all")
    @Override
    public ResultSet<RelatedTopic> getAllPublishedSoundposter(@HeaderParam("Cookie") ClientState clientState) {
        ResultSet<RelatedTopic> soundposter = dms.getTopics("dm4.topicmaps.topicmap", true, 100, clientState);
        Set<RelatedTopic> resultset = new LinkedHashSet<RelatedTopic>();
        Iterator<RelatedTopic> results = soundposter.iterator();
        while (results.hasNext()) {
            RelatedTopic element = results.next();
            if (element.getCompositeValue().has("com.soundposter.published")) {
                if (element.getCompositeValue().getBoolean("com.soundposter.published")) {
                    resultset.add(element);
                }
            }
        }

        return new ResultSet<RelatedTopic>(resultset.size(), resultset);
    }

    @GET
    @Path("/poster/random")
    @Override
    public Topic getRandomPublishedSoundposter(@HeaderParam("Cookie") ClientState clientState) {
        ResultSet<RelatedTopic> soundposter = getAllPublishedSoundposter(clientState);
        Random rand = new Random();
        Object[] results = soundposter.getItems().toArray();
        Topic randomOne = null;
        if (soundposter.getSize() > 1) {
            randomOne = (Topic) results[rand.nextInt(soundposter.getSize()-1)];
        } else {
            if (soundposter.iterator().hasNext()) randomOne = soundposter.iterator().next();
        }
        return randomOne;
    }

    @GET
    @Path("/poster/url/{id}")
    @Override
    public String getPublishedSoundposterUrl(@PathParam("id") String posterId, 
        @HeaderParam("Cookie") ClientState clientState) {
        try {
            String webAddress = "";
            Topic poster = dms.getTopic(Long.parseLong(posterId), true, clientState);
            if (poster.getCompositeValue().has("com.soundposter.published")) {
                if (poster.getCompositeValue().getBoolean("com.soundposter.published")) {
                    String posterAlias = poster.getCompositeValue().getString("com.soundposter.web_alias");
                    String profileAlias = getProfileAliasForPoster(poster);
                    if (profileAlias != null) {
                        webAddress = "/" + profileAlias + "/" + posterAlias;
                        log.info("getPublishedSoundposterUrl => " + webAddress);
                        return "{ \"url\": \"" + webAddress + "\"}";
                    } else {
                        log.warning("getPublishedSoundposterUrl => could not find profile for poster " + posterId);
                    }
                }
            }
        } catch (Exception e) {
            throw new WebApplicationException(new Throwable("A published soundposter for the given id "
                    + "could not be found."), 404);
        }
        return "{}";
    }
    
    @GET
    @Path("/create/signup/{signupInfo}/{name}")
    @Override
    public String createSignupInformation(@PathParam("signupInfo") String signup, @PathParam("name") String name, 
        @HeaderParam("Cookie") ClientState clientState) {
        try {
            Topic contact = dms.createTopic(new TopicModel("dm4.contacts.email_address", 
                    new SimpleValue(signup)), clientState);
            // contact.setCompositeValue(new CompositeValue().put("dm4.contacts.first_name", name), clientState, null);
            // contact.setCompositeValue(new CompositeValue().put("dm4.contacts.last_name", "User"), clientState, null);
            // contact.setCompositeValue(new CompositeValue().put("dm4.contacts.email_address", "User"), clientState, null);
            // "caller assumes its mulit-value but its single value", what shall this mean?
        } catch (Exception e) {
            throw new WebApplicationException(e, 500);
        }
        return "{}";
    }
    
    // ------------------------------------------------------------------------------------------------ Private Methods

    private InputStream invokeSoundposterView() {
        try {
            return dms.getPlugin("com.soundposter.webapp").getResourceAsStream("web/poster/index.html");
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
    
    private InputStream invokeFrontpageView() {
        try {
            return dms.getPlugin("com.soundposter.webapp").getResourceAsStream("web/website/index.html");
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
    
    // this hardcoded view-link exists just because of backwards compatibility reasons and some romanticism
    private InputStream invokeSoundposterTorStreetView() {
        try {
            return dms.getPlugin("com.soundposter.webapp").getResourceAsStream("web/tsfestival/index_new.html");
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
    
    // this hardcoded view-link exists just because of backwards compatibility reasons and some romanticism
    private InputStream invokeC3SView() {
        try {
            return dms.getPlugin("com.soundposter.webapp").getResourceAsStream("web/c3s/index.html");
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
    
    // this hardcoded view-link exists just because of backwards compatibility reasons and some romanticism
    private InputStream getSoundposterFavIcon() {
        try {
            return dms.getPlugin("com.soundposter.webapp").getResourceAsStream("web/images/favicon.ico");
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
    
    private boolean hasProfilePosterRelation(Topic profileAlias, Topic poster) {
        // ### check if profile is active.. 
        Topic profile = profileAlias.getRelatedTopic("dm4.core.composition", "dm4.core.part", "dm4.core.whole", 
                "com.soundposter.account", true, false, null);
        ResultSet<RelatedTopic> items = profile.getRelatedTopics("com.soundposter.author_edge", "dm4.core.default", 
                "dm4.core.default", "dm4.topicmaps.topicmap", true, false, 0, null);
        if (items.getSize() > 0) {
            Iterator<RelatedTopic> soundposter = items.getIterator();
            while(soundposter.hasNext()) {
                RelatedTopic element = soundposter.next();
                if (element.getId() == poster.getId()) {
                    return true;
                } else {
                }
            }
        }
        return false;
    }
    
    private String getProfileAliasForPoster(Topic poster) {
        // ### check if profile is active.. 
        RelatedTopic author = poster.getRelatedTopic("com.soundposter.author_edge", "dm4.core.default", 
                "dm4.core.default", "com.soundposter.account", true, false, null);
        if (author.getCompositeValue().has("com.soundposter.account_alias")) {
            return author.getCompositeValue().getString("com.soundposter.account_alias");
        }
        return null;
    }
    
    private void checkACLsOfMigration() {
        ResultSet<RelatedTopic> sounds = dms.getTopics("com.soundposter.sound", false, 0, null);
        Iterator<RelatedTopic> soundset = sounds.getIterator();
        log.info("initial ACL update of all imported soundtracks");
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
        log.info("serviceArrive...");
        if (service instanceof AccessControlService) {
            log.info("AccessControlServer has ARRIVED.. Y'EAH\n\r\n\r");
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
