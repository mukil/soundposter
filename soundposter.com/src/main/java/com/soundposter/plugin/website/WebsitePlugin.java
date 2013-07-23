package com.soundposter.plugin.website;

import de.deepamehta.core.service.ClientState;

import com.sun.jersey.api.view.Viewable;
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
import de.deepamehta.plugins.topicmaps.model.Topicmap;
import de.deepamehta.plugins.topicmaps.service.TopicmapsService;
import de.deepamehta.plugins.webactivator.WebActivatorPlugin;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * Soundposter Website running on <http://www.soundposter.com>
 * @version 1.0-SNAPSHOT
 * @author Copyright 2013, Malte Reißig <malte@mikromedia.de>
 *
 * Last modified: Jul 17, 2013
 */

@Path("/")
@Consumes("application/json")
@Produces("application/json")
public class WebsitePlugin extends WebActivatorPlugin {

    private Logger log = Logger.getLogger(getClass().getName());
    private AccessControlService acService;
    private TopicmapsService tmService;
    private boolean isInitialized = false;

    private static final String CHILD_TYPE_URI = "dm4.core.part";
    private static final String PARENT_TYPE_URI = "dm4.core.whole";
    private static final String DEFAULT_TYPE_URI = "dm4.core.default";

    /** Initialize the migrated soundsets ACL-Entries. */
    @Override
    public void init() {
        isInitialized = true;
        configureIfReady();
        setupRenderContext();
    }


    private void configureIfReady() {
        if (isInitialized) {
            checkACLsOfMigration();
        }
    }

	@GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getFrontpage(@HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting front page without pathinfo.. ");
        context.setVariable("pageId", "welcome");
        context.setVariable("poster", getRandomFeaturedSoundposter(clientState).toJSON().toString());
        return view("index");
    }

	@GET
    @Path("/manifest.webapp")
    @Produces("application/x-web-app-manifest+json")
    public String getFirefoxOSWebAppManifest(@HeaderParam("Cookie") ClientState clientState) {
        // todo: fix ApacheConfig with "AddType application/x-web-app-manifest+json .webapp"
        try {
            log.info("Requesting FirefoxOS Web-App Manifest");
            JSONObject manifest = new JSONObject();
            manifest.put("version", "0.1-SNAPSHOT");
            manifest.put("name", "Soundposter");
            manifest.put("description", "Soundposter - Audible websites with images");
            manifest.put("launch_path", "/");
            manifest.put("type", "web");
            manifest.put("icons", new JSONObject()
                    .put("128", "/com.soundposter.website/images/soundposter_play_button_600.png")); // todo:
            manifest.put("developer", new JSONObject()
                    .put("name", "Malte Reißig")
                    .put("url", "http://www.mikromedia.de"));
            manifest.put("install_allowed_from", new JSONArray().put("*"));
            manifest.put("locales", new JSONObject()  // todo:
                    .put("en", new JSONObject()
                        .put("description", "The Soundposter Web-App is an audio-player for streaming sounds "
                            + "with images.")
                        .put("developer", "http://www.mikromedia.de")));
            manifest.put("default_locale", "en");  // todo:
            manifest.put("permissions", new JSONObject()
                    .put("audio-channel-normal", true)
                    .put("audio-channel-content", true)); // todo: check FFOS audio?
            // we would like to have the permission for "backgroundservice", "systemXHR" to load data in "packaged" app?
            /** {
                "version": "0.1",
                "name": "Your App",
                "description": "Your new awesome Open Web App",
                "launch_path": "/index.html",
                "icons": {
                    "16": "/img/icons/mortar-16.png",
                    "48": "/img/icons/mortar-48.png",
                    "128": "/img/icons/mortar-128.png"
                },
                "developer": {
                    "name": "Your Name",
                    "url": "http://yourawesomeapp.com"
                },
                "installs_allowed_from": ["*"],
                "locales": {
                    "es": {
                        "description": "Su nueva aplicación impresionante Open Web",
                        "developer": {
                            "url": "http://yourawesomeapp.com"
                        }
                    },
                    "it": {
                        "description": "Il vostro nuovo fantastico Open Web App",
                        "developer": {
                            "url": "http://yourawesomeapp.com"
                        }
                    }
                },
                "default_locale": "en",
                "permissions": {
                    "systemXHR": {}
                }
            } **/

            return manifest.toString();
        } catch (JSONException ex) {
            Logger.getLogger(WebsitePlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Error";
    }

	@GET
    @Path("/sign-up")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSignupPage(@HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter.com sign-up page .. ");
		context.setVariable("pageId", "sign-up");
        return view("sign-up");
    }

	@GET
    @Path("/browse/{pageNr}")
	@Produces(MediaType.TEXT_HTML)
    public Viewable getPosterPage(@PathParam("pageNr") int pageNr, @HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting page " +pageNr+ " to browse all published soundposter.. ");
		context.setVariable("pageId", "browse");
        return view("browse");
    }

    @GET
    @Path("/browse")
	@Produces(MediaType.TEXT_HTML)
    public Viewable getPosterFrontPage(@PathParam("pageNr") int pageNr, @HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting page " +pageNr+ " to browse all published soundposter.. ");
		context.setVariable("pageId", "browse");
        return view("browse");
    }

	@GET
    @Path("/posterview/{authorAlias}/{posterAlias}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPosterView (@PathParam("authorAlias") String author, @PathParam("posterAlias") String poster) {
        log.info("requesting posterview \""+ poster +"\" by author \"" + author + "\"");
		return getPosterViewWithTrack(author, poster, 0);
    }

    @GET
    @Path("/posterview/{authorAlias}/{posterAlias}/{trackId}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPosterViewWithTrack (@PathParam("authorAlias") String author, @PathParam("posterAlias") String poster,
            @PathParam("trackId") long trackId) {
        log.info("requesting posterview \""+ poster +"\" by author \"" + author + "\" and track \""+trackId+"\"");
		try {
			Topic soundposter = getSoundposter(author, poster, null); // sanity checks already built-in
			String posterDescription = "", posterName ="", graphicUrl = "", buylink = "";
			posterName = soundposter.getSimpleValue().toString();
			if (soundposter.getCompositeValue().has("com.soundposter.description")) {
				posterDescription = soundposter.getCompositeValue().getString("com.soundposter.description");
			}
            // fixme: keywords, tracklist-data
			Topicmap topicmap = tmService.getTopicmap(soundposter.getId(), null);
            JSONArray setlist = null;
            JSONArray soundlist = new JSONArray();
            try {
                setlist = topicmap.toJSON().getJSONArray("topics");
                for (int i = 0; i < setlist.length(); i++) {
                    JSONObject sound = setlist.getJSONObject(i);
                    if (sound.getString("type_uri").equals("com.soundposter.sound")) {
                        soundlist.put(dms.getTopic(sound.getLong("id"), true, null).toJSON());
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(WebsitePlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
            // prepare page, find the poster graphic
            TopicModel graphic = soundposter.getCompositeValue().getTopic("dm4.files.file");
            graphicUrl = graphic.getCompositeValue().getString("dm4.files.path");
            // get partner website link
            Topic map = dms.getTopic(soundposter.getId(), true, null);
            Topic linkOne = map.getRelatedTopic("com.soundposter.buy_edge",
                    DEFAULT_TYPE_URI, DEFAULT_TYPE_URI, "dm4.webbrowser.web_resource", true, false, null);
            if (linkOne != null) buylink = linkOne.toJSON().toString();
			context.setVariable("name", posterName);
			context.setVariable("username", author);
			context.setVariable("webalias", poster);
			context.setVariable("description", posterDescription);
			context.setVariable("poster", topicmap.toJSON().toString());
			context.setVariable("keywords", "");
            context.setVariable("graphic", "/filerepo" + graphicUrl);
            context.setVariable("buylink", buylink);
            context.setVariable("setlist", soundlist.toString());
            context.setVariable("track", trackId);
		} catch (WebApplicationException ex) {
			log.info(ex.getMessage());
			throw new WebApplicationException(ex, ex.getResponse().getStatus());
		}
        return view("poster");
    }

    @GET
    @Path("/{profile}/{poster}")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getSoundposterView(@PathParam("profile") String profileAlias,
        @PathParam("poster") String posterAlias, @HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter-view for profile: " + profileAlias + " and posterAlias" + posterAlias);
        if (profileAlias.equals("website")) return getWebsiteView(posterAlias, clientState);
        return invokeSoundposterView();
    }

    @GET
    @Path("/{pathInfo}")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getWebsiteView(@PathParam("pathInfo") String pathInfo,
        @HeaderParam("Cookie") ClientState clientState) {
        // log.info("Requesting website-view for pathInfo: " + pathInfo);
        if (pathInfo.equals("tsfestival")) return invokeSoundposterTorStreetView();
        if (pathInfo.equals("c3s")) return invokeC3SView();
        if (pathInfo.equals("favicon.ico")) return getSoundposterFavIcon();
        log.info("Requesting front page with pathInfo .. "); // redirect to new frontpage via Response-Object
        return invokeFrontpageView();
    }

    @GET
    @Path("/poster/{profile}/{poster}")
    public Topic getSoundposter(@PathParam("profile") String profileAlias, @PathParam("poster") String posterAlias,
        @HeaderParam("Cookie") ClientState clientState) throws WebApplicationException {
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
    @Path("/poster/featured/all")
    public ResultSet<RelatedTopic> getAllFeaturedSoundposter(@HeaderParam("Cookie") ClientState clientState) {
        // performs no sanity check if poster is published, means a featured poster is published implicitly
        ResultSet<RelatedTopic> soundposter = dms.getTopics("dm4.topicmaps.topicmap", true, 100, clientState);
        Set<RelatedTopic> resultset = new LinkedHashSet<RelatedTopic>();
        Iterator<RelatedTopic> results = soundposter.iterator();
        while (results.hasNext()) {
            RelatedTopic element = results.next();
            if (element.getCompositeValue().has("com.soundposter.featured")) {
                if (element.getCompositeValue().getBoolean("com.soundposter.featured")) {
                    resultset.add(element);
                }
            }
        }

        return new ResultSet<RelatedTopic>(resultset.size(), resultset);
    }

    @GET
    @Path("/poster/random/published")
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
    @Path("/poster/random/featured")
    public Topic getRandomFeaturedSoundposter(@HeaderParam("Cookie") ClientState clientState) {
        ResultSet<RelatedTopic> soundposter = getAllFeaturedSoundposter(clientState);
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
    @ConsumesService({
        "de.deepamehta.plugins.topicmaps.service.TopicmapsService",
        "de.deepamehta.plugins.accesscontrol.service.AccessControlService"
    })
// @ConsumesService("de.deepamehta.plugins.accesscontrol.service.TopicmapsService")
    public void serviceArrived(PluginService service) {
        if (service instanceof AccessControlService) {
            acService = (AccessControlService) service;
        } else if (service instanceof TopicmapsService) {
			tmService = (TopicmapsService) service;
		}
    }

    @Override
    @ConsumesService({
        "de.deepamehta.plugins.topicmaps.service.TopicmapsService",
        "de.deepamehta.plugins.accesscontrol.service.AccessControlService"
    })
	// @ConsumesService("de.deepamehta.plugins.accesscontrol.service.TopicmapsService")
    public void serviceGone(PluginService service) {
        if (service == acService) {
            acService = null;
        } else if (service == tmService) {
			tmService = null;
		}
    }

}
