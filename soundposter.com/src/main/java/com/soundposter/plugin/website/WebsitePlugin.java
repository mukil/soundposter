package com.soundposter.plugin.website;

import com.soundposter.plugin.website.model.PreviewPoster;
import com.soundposter.plugin.website.model.SearchedSet;
import com.soundposter.plugin.website.model.SearchedTrack;
import de.deepamehta.core.service.ClientState;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.*;
import de.deepamehta.core.model.*;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.core.service.annotation.ConsumesService;
import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
import de.deepamehta.plugins.accesscontrol.model.ACLEntry;
import de.deepamehta.plugins.accesscontrol.model.AccessControlList;
import de.deepamehta.plugins.accesscontrol.model.Operation;
import de.deepamehta.plugins.accesscontrol.model.UserRole;
import de.deepamehta.plugins.topicmaps.model.Topicmap;
import de.deepamehta.plugins.topicmaps.service.TopicmapsService;
import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

/**
 *
 * Soundposter Website running on <http://www.soundposter.com>
 * @version 1.0-SNAPSHOT
 * @author Copyright 2013, Malte Reißig <malte@mikromedia.de>
 *
 * Last modified: Jul 17, 2013
 */

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.TEXT_HTML)
public class WebsitePlugin extends WebActivatorPlugin {

    private Logger log = Logger.getLogger(getClass().getName());
    private AccessControlService acService;
    private TopicmapsService tmService;
    // private SoundposterService service;
    private boolean isInitialized = false;

    private static final String CHILD_TYPE_URI = "dm4.core.child";
    private static final String PARENT_TYPE_URI = "dm4.core.parent";
    private static final String DEFAULT_TYPE_URI = "dm4.core.default";

    private static String PERSON_TYPE_URI = "dm4.contacts.person";
    private static String MAILBOX_TYPE_URI = "dm4.contacts.email_address";

    private static String SETLIST_LABEL_URI = "com.soundposter.setlist_label";

    private static String SET_URI = "com.soundposter.set";
    private static String SET_NAME_URI = "com.soundposter.set_name";
    private static String SET_DESCRIPTION_URI = "com.soundposter.set_description";

    private static String SOUND_URI = "com.soundposter.sound";
    private static String SOUND_STREAM_URI = "dm4.webbrowser.url";
    private static String SOUND_NAME_URI = "com.soundposter.sound_name";
    private static String SOUND_ORDINAL_URI = "com.soundposter.ordinal_number"; // fixme: belongs into poster-context
    private static String SOUND_ARTIST_URI = "com.soundposter.artist_name";
    private static String SOUND_ALBUM_URI = "com.soundposter.album_name";
    private static String SOUND_SOURCE_URI = "com.soundposter.source_page";
    private static String SOUND_PUBLISHER_INFO_URI = "com.soundposter.publisher_info";
    private static String SOUND_PUBLISHER_NAME_URI = "com.soundposter.publisher_name";
    private static String SOUND_LICENSE_URI = "com.soundposter.license_info";
    private static String SOUND_DESCRIPTION_URI = "com.soundposter.sound_description";
    private static String SOUND_ARTWORK_URI = "com.soundposter.sound_artwork_url";

    private static final String SOUNDCLOUD_CLIENT_ID = "xgQpdzwTRicVIalDvCMTqQ";
    private static final String SOUNDCLOUD_TRACK_ID_PREFIX = "com.soundcloud.track.";
    private static final String SOUNDCLOUD_SET_ID_PREFIX = "com.soundcloud.set.";

    /** Initialize the migrated soundsets ACL-Entries. */
    @Override
    public void init() {
        isInitialized = true;
        configureIfReady();
        initTemplateEngine();
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
        viewData("pageId", "welcome");
        Topic featured = getRandomFeaturedSoundposter(clientState);
        if (featured == null) {
            viewData("subtitle", "Edit your first soundposter so it to shows up here.");
            viewData("description", "Adding a soundposter goes like this: "
                    + "Aggregate an \"Account\" with a \"Username\" and \"Activate\" it. "
                    + "Then relate the \"Account\" to a topicmap as \"Author\". "
                    + "Set your topicmap to \"Published\" and \"Featured\" and your' nearly done. "
                    + "Just aggregate a \"File\" as background graphic to the map and make sure to use proper web "
                    + "aliases on both occasions.");
            viewData("author", "root");
            viewData("hashtag", "soundposter");
            return view("index");
        }
        // prepare page, find the poster graphic
        String graphicPath = getPosterGraphicURL(featured);
        String webalias = featured.getCompositeValue().getString("com.soundposter.web_alias");
        String subtitle = featured.getCompositeValue().getString("com.soundposter.poster_subtitle");
        String description = featured.getCompositeValue().getString("com.soundposter.poster_description");
        String hashtag = featured.getCompositeValue().getString("com.soundposter.poster_hashtag");
        if (hashtag.isEmpty()) hashtag = "soundposter";
        String license_info = featured.getCompositeValue().getString("com.soundposter.license_info");
        // fixme: username might be null, if not set up correctly
        String username = getProfileAliasForPoster(featured); //  can be null
        String url = "/" + username + "/" + webalias;
        viewData("poster", featured.getModel().toJSON().toString());
        viewData("graphic", graphicPath);
        viewData("subtitle", subtitle);
        viewData("license", license_info);
        viewData("description", description);
        viewData("hashtag", hashtag);
        viewData("author", username);
        viewData("link", url);
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
    @Path("/robots.txt")
    @Produces(MediaType.TEXT_PLAIN)
    public String getRobotsTxt(@HeaderParam("Cookie") ClientState clientState) {
        return "User-agent: *\n\rDisallow: "; // Allow all
    }

    /** fixme: return robots.txt */

	@GET
    @Path("/sign-up")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSignupPage(@HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter.com sign-up page .. ");
		viewData("pageId", "sign-up");
        return view("signup");
    }

    @GET
    @Path("/philosophy")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPhilosophyPage(@HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter.com sign-up page .. ");
		viewData("pageId", "philosophy");
        return view("philosophy");
    }

    @GET
    @Path("/legal")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getLegalPage(@HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter.com legal page .. ");
		viewData("pageId", "legal");
        return view("legal");
    }


    @GET
    @Path("/about-us")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getAboutPage(@HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter.com about page .. ");
		viewData("pageId", "about");
        return view("about");
    }

    @GET
    @Path("/register")
    @Produces(MediaType.TEXT_HTML)
    public Viewable registerSimpleNewsAccount(@QueryParam("name") String name, @QueryParam("mailbox") String mailbox,
            @QueryParam("message") String message) {
        try {
            log.info("setting up new newsaccount ");
			CompositeValueModel personData =  new CompositeValueModel()
					.add("dm4.contacts.email_address", new TopicModel("dm4.contacts.email_address", new SimpleValue(mailbox)))
                    .put("dm4.contacts.person_name", new TopicModel("dm4.contacts.person_name",
                        new CompositeValueModel(new JSONObject().put("dm4.contacts.first_name", name))))
                    .put("dm4.contacts.notes", "<p>" + message + "</p>");
			TopicModel userModel = new TopicModel(PERSON_TYPE_URI, personData);
			dms.createTopic(userModel, null);
            log.info("created new newsletter recipient (person)");
            viewData("pageId", "thanks");
			return view("thanks");
        } catch (Exception e) {
            log.warning(e.getMessage());
            throw new WebApplicationException(e.getCause());
        }
    }

    @GET
    @Path("/soundcloud/add/track/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Topic addSoundCloudTrack(@PathParam("id") int trackId, @HeaderParam("Cookie") ClientState clientState) {
        // fixme: relate track via "Author" to logged in user
        checkRequestAuthentication();
        SearchedTrack track = getSoundCloudTrackById(trackId);
        // creator and owner should be set correct if its a new track (to us)
        // todo: in any case relate track to username
        Topic sound = createSoundCloudTrackTopic(track, clientState);
        if (sound == null) {
            log.warning("We already have a soundcloud topic with that ID in our DB.");
            log.warning("Doing Nothjing!");
            // todo: get it, update it
            // result = dms.get
        } else {
            // create author-edge for the logged in user:
            Topic sp_account = getProfileTopic(acService.getUsername(acService.getUsername()));
            if (sp_account == null) throw new RuntimeException("User Account has no soundposter-profile related.");
            createAuthorRelation(sound, sp_account);
        }
        return sound;
    }

    @GET
    @Path("/soundcloud/add/set/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Topic addSoundCloudSet(@PathParam("id") int trackId, @HeaderParam("Cookie") ClientState clientState) {
        // fixme: relate set via "Author" to logged in user
        checkRequestAuthentication();
        //
        SearchedSet result = getSoundCloudSetById(trackId);
        Topic set = createSoundCloudSetTopic(result, clientState);
        if(set == null) {
            log.warning("We already have a soundcloud set-topic with that ID in our DB.");
            log.warning("Doing Nothjing!");
            // todo: get it, update it
        } else {
            Topic sp_account = getProfileTopic(acService.getUsername(acService.getUsername()));
            if (sp_account == null) throw new RuntimeException("User Account has no soundposter-profile related.");
            createAuthorRelation(set, sp_account);
        }
        return set;
    }

    @GET
    @Path("/soundcloud/view/tracks/{query}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSoundCloudTracksView(@PathParam("query") String query) {
        return getSoundCloudTracksView(query, 0);
    }

    @GET
    @Path("/soundcloud/view/tracks/{query}/{pageNr}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSoundCloudTracksView(@PathParam("query") String query, @PathParam("pageNr") int pageNr) {
        //
        checkRequestAuthentication();
        //
        JSONArray tracks = getSoundCloudTracksBySearchTerm(query, pageNr);
        ArrayList<SearchedTrack> results = new ArrayList<SearchedTrack>();
        for (int i = 0; i < tracks.length(); i++) {
            try {
                //
                JSONObject item = tracks.getJSONObject(i);
                SearchedTrack searchedTrack = createSoundCloudSearchedTrack(item, 0);
                        if (searchedTrack != null) results.add(searchedTrack);
            } catch (JSONException ex) {
                Logger.getLogger(WebsitePlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        viewData("search_type", "tracks");
        viewData("provider_name", "SoundCloud");
        viewData("pageId", "search-results");
        viewData("results", results);
        return view("track-results");
    }

    @GET
    @Path("/soundcloud/view/sets/{query}/{pageNr}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSoundCloudSetsView(@PathParam("query") String query, @PathParam("pageNr") int pageNr) {
        //
        checkRequestAuthentication();
        //
        JSONArray sets = getSoundCloudSetsBySearchTerm(query, pageNr);
        ArrayList<SearchedSet> results = new ArrayList<SearchedSet>();
        for (int i = 0; i < sets.length(); i++) {
            try {
                //
                JSONObject item = sets.getJSONObject(i);
                ArrayList<SearchedTrack> items = new ArrayList<SearchedTrack>();
                JSONArray tracks = item.getJSONArray("tracks");
                for (int k = 0; k < tracks.length(); k++) {
                    try {
                        JSONObject track = tracks.getJSONObject(k);
                        SearchedTrack searchedTrack = createSoundCloudSearchedTrack(track, (k+1));
                        if (searchedTrack != null) items.add(searchedTrack);
                    } catch (JSONException ex) {
                        log.fine("SoundCloud Track is missing from set (?)");
                    }
                }
                SearchedSet result = createSoundCloudSearchedSet(item);
                results.add(result);
            } catch (JSONException ex) {
                log.severe(ex.getMessage());
                throw new WebApplicationException(ex.getCause());
            }
        }
        viewData("search_type", "sets");
        viewData("provider_name", "SoundCloud");
        viewData("pageId", "search-results");
        viewData("results", results);
        return view("set-results");
    }

    @GET
    @Path("/soundcloud/view/sets/{query}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSoundCloudSetsView(@PathParam("query") String term) {
        return getSoundCloudSetsView(term, 0);
    }

	@GET
    @Path("/browse/{pageNr}")
	@Produces(MediaType.TEXT_HTML)
    public Viewable getBrowsePage(@PathParam("pageNr") int page_nr, @HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting page " +page_nr+ " to browse all published soundposter.. ");
		viewData("pageId", "browse");
        ArrayList<PreviewPoster> results = new ArrayList<PreviewPoster>();
        ResultSet<RelatedTopic> all = getAllPublishedSoundposter();
        // build up sortable collection of all result-items (warning: in-memory copy of _all_ published soundposter)
        ArrayList<RelatedTopic> in_memory = getResultSetSortedByTitle(all, clientState);
        int max_count = 6, modulo_half = 1, count = 0, start_count = page_nr * max_count;
        // throw error if page is unexpected high or NaN
        for (RelatedTopic item : in_memory) {
            // start of preparing page results
            if (count == start_count + max_count) { // reached n-soundposter, beginning at m + items-on-page
                // returning results since result-set is full
                viewData("total_count", in_memory.size());
                viewData("page", page_nr);
                int overall_pages = (all.getTotalCount() % max_count > modulo_half) ? (all.getTotalCount() / max_count) : all.getTotalCount() / max_count + 1;
                // int overall_pages = all.getTotalCount() / max_count;
                //
                int next_page = (page_nr == overall_pages) ? page_nr : page_nr + 1;
                int previous_page = (page_nr == 0) ? 0 : (page_nr - 1);
                boolean not_on_first_page = (page_nr > 0) ? true : false;
                boolean not_on_last_page = (page_nr == overall_pages) ? false : true;
                viewData("not_on_first_page", not_on_first_page);
                viewData("not_on_last_page", not_on_last_page);
                viewData("previous_page_url", (page_nr > 0) ? "/browse/" + previous_page : "/browse/" + overall_pages);
                viewData("next_page_url", "/browse/" + next_page);
                viewData("pages", overall_pages);
                // viewData("page_array", new ArrayList(overall_pages));
                viewData("from", start_count);
                viewData("to", start_count + (start_count - results.size()));
                viewData("overall", all.getTotalCount());
                viewData("set", results);
                return view("browse");
            }
            if (count >= start_count) {
                // arrived at the interesting parts of result-set
                String title = item.getModel().getSimpleValue().toString();
                // fixme: username might be null, if not set up correctly
                String username = getProfileAliasForPoster(item);
                String web_alias = item.getCompositeValue().getString("com.soundposter.web_alias");
                String url = "/" + username + "/" + web_alias;
                String onclick = "javascript:window.location.href=\""+url+"\"";
                String graphic_url = getPreviewGraphicURL(item);
                if (graphic_url == null) graphic_url = getPosterGraphicURL(item);
                String preview_graphic_style = "background: url(" + graphic_url + ") 0px 0px no-repeat;";
                PreviewPoster poster = new PreviewPoster(title, url, username, preview_graphic_style, onclick);
                //
                results.add(poster);
            }
            count++;
            // finished preparing page results
        }
        // if there are less posters than max_count in the database, the loop runs out without the page being prepared
        viewData("total_count", in_memory.size());
        viewData("page", page_nr);
        int overall_pages = (all.getTotalCount() % max_count > modulo_half) ? (all.getTotalCount() / max_count) : all.getTotalCount() / max_count + 1;
        // int overall_pages = all.getTotalCount() / max_count;
        //
        int next_page = (page_nr == overall_pages) ? 0 : page_nr + 1;
        int previous_page = (page_nr == 0) ? 0 : (page_nr - 1);
        viewData("not_on_first_page", (page_nr > 0) ? true : false);
        viewData("not_on_last_page", (page_nr == overall_pages) ? false : true);
        viewData("previous_page_url", (page_nr > 0) ? "/browse/" + previous_page : "/browse");
        viewData("next_page_url", "/browse/" + next_page);
        viewData("pages", overall_pages);
        viewData("from", start_count);
        viewData("to", start_count + (start_count - results.size()));
        viewData("set", results);
        return view("browse");
    }

    @GET
    @Path("/browse")
	@Produces(MediaType.TEXT_HTML)
    public Viewable getBrowsePage(@HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting page nr. 0 to browse all published soundposter.. ");
		viewData("pageId", "browse");
        return getBrowsePage(0, clientState);
    }

	@GET
    @Path("/{authorAlias}/{posterAlias}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPosterView (@PathParam("authorAlias") String author, @PathParam("posterAlias") String poster) {
        log.info("requesting posterview \""+ poster +"\" by author \"" + author + "\"");
		return getPosterViewWithTrack(author, poster, 0);
    }

    @GET
    @Path("/{authorAlias}/{posterAlias}/{trackId}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPosterViewWithTrack (@PathParam("authorAlias") String author, @PathParam("posterAlias") String poster,
            @PathParam("trackId") long trackId) {
        log.info("requesting posterview \""+ poster +"\" by author \"" + author + "\" and track \""+trackId+"\"");
		try {
			Topic soundposter = getSoundposter(author, poster); // sanity checks already built-in
			String description = "", name ="", graphicUrl = "", buylink = "{}", subtitle = "", license = "",
                    hashtag = "", buylink_label = "", buylink_href = "", setlist_label = "", stylesheet = "";
			name = soundposter.getSimpleValue().toString();
            description = soundposter.getCompositeValue().getString("com.soundposter.poster_description");
            subtitle = soundposter.getCompositeValue().getString("com.soundposter.poster_subtitle");
            license = soundposter.getCompositeValue().getString("com.soundposter.license_info");
            buylink_label = soundposter.getCompositeValue().getString("com.soundposter.buy_link_label");
            buylink_href = soundposter.getCompositeValue().getString("com.soundposter.buy_link_href");
            hashtag = soundposter.getCompositeValue().getString("com.soundposter.poster_hashtag");
            setlist_label = soundposter.getCompositeValue().getString(SETLIST_LABEL_URI);
            /** stylesheet = soundposter.getCompositeValue().getTopic("com.soundposter.custom_style")
                    .getSimpleValue().toString(); **/
            // fixme: keywords, tracklist-data
			Topicmap topicmap = tmService.getTopicmap(soundposter.getId());
            JSONArray setlist = null;
            JSONArray soundlist = new JSONArray();
            try {
                setlist = topicmap.toJSON().getJSONArray("topics");
                for (int i = 0; i < setlist.length(); i++) {
                    JSONObject sound = setlist.getJSONObject(i);
                    if (sound.getString("type_uri").equals("com.soundposter.sound")) {
                        soundlist.put(dms.getTopic(sound.getLong("id"), true).toJSON());
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(WebsitePlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
            // prepare page, find the poster graphic
            graphicUrl = getPosterGraphicURL(soundposter);
            // get partner website link
            Topic map = dms.getTopic(soundposter.getId(), true);
            Topic linkOne = map.getRelatedTopic("com.soundposter.buy_edge",
                    DEFAULT_TYPE_URI, DEFAULT_TYPE_URI, "dm4.webbrowser.web_resource", true, false);
            if (linkOne != null) buylink = linkOne.toJSON().toString();
			viewData("name", name);
			viewData("username", author);
			viewData("webalias", poster);
            viewData("subtitle", subtitle);
			viewData("description", description);
			viewData("license", license);
			viewData("poster", topicmap.toJSON().toString());
			viewData("keywords", "");
			viewData("hashtag", hashtag);
            viewData("graphic", graphicUrl);
            viewData("buylink", buylink);
            viewData("buylink_label", buylink_label);
            viewData("buylink_href", buylink_href);
            viewData("setlist_label", setlist_label);
            viewData("setlist", soundlist.toString());
            // viewData("stylesheet", stylesheet);
            viewData("track", trackId);
		} catch (WebApplicationException ex) {
			log.info(ex.getMessage());
			throw new WebApplicationException(ex, ex.getResponse().getStatus());
		}
        return view("poster");
    }

    @GET
    @Path("/{pathInfo}")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getWebsiteView(@PathParam("pathInfo") String pathInfo,
        @HeaderParam("Cookie") ClientState clientState) {
        // log.info("Requesting website-view for pathInfo: " + pathInfo);
        if (pathInfo.equals("c3s")) return invokeC3SView();
        if (pathInfo.equals("favicon.ico")) return getSoundposterFavIcon();
        log.info("Requesting front page with pathInfo .. "); // redirect to new frontpage via Response-Object
        return invokeFrontpageView();
    }

    @GET
    @Path("/tsfestival")
    @Produces(MediaType.TEXT_HTML)
    public Response getTSF12View() {
        return Response.seeOther(URI.create("/walther/tsfestival")).build();
    }

    @GET
    @Path("/poster/{profile}/{poster}")
    public Topic getSoundposter(@PathParam("profile") String profileAlias, @PathParam("poster") String posterAlias)
            throws WebApplicationException {
        log.info("Requesting soundposter-data for web alias " + posterAlias + " and profile " + profileAlias);
        Topic soundposter_alias = dms.getTopic("com.soundposter.web_alias",
                new SimpleValue(posterAlias), true);
        /** Soundposter Alias sanity check */
        if (soundposter_alias == null) { // not found
            throw new WebApplicationException(new Throwable("Soundposter with web alias not found"), 404);
        }

        /** check if soundposter is published, otherwise return 401 */
        Topic soundposter = soundposter_alias.getRelatedTopic("dm4.core.composition", "dm4.core.child", "dm4.core.parent",
                "dm4.topicmaps.topicmap", true, false);
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
                "dm4.topicmaps.topicmap_topic", "dm4.core.parent", "com.soundposter.sound", true, false, 0);
        if (songs != null) log.fine("  "+ songs.getSize() +" songs in soundposter");

        /** Check if the poster could really be authored by this profile alias */
        Topic profile = dms.getTopic("com.soundposter.account_alias", new SimpleValue(profileAlias), true);
        if (profile == null) { // internal server error
            log.info("  profile \""+ profileAlias +"\" not found \r\n");
            throw new WebApplicationException(new Throwable("Soundposter data could not be loaded. "
                    + "Profile not found"), 404);
        }

        /** last sanity check, there is a profile poster "author" relationship */
        if (!hasPosterProfileRelation(profile, soundposter)) {  // not found
            throw new WebApplicationException(new Throwable("Soundposter data could not be loaded. "
                    + "Poster has no proper author relation set."), 404);
        }
        return soundposter;
    }

    @GET
    @Path("/poster/published/all")
    public ResultSet<RelatedTopic> getAllPublishedSoundposter() {
        ResultSet<RelatedTopic> soundposter = dms.getTopics("dm4.topicmaps.topicmap", true, 100);
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
    public ResultSet<RelatedTopic> getAllFeaturedSoundposter() {
        // performs no sanity check if poster is published, means a featured poster is published implicitly
        ResultSet<RelatedTopic> soundposter = dms.getTopics("dm4.topicmaps.topicmap", true, 100);
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
        ResultSet<RelatedTopic> soundposter = getAllPublishedSoundposter();
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
        ResultSet<RelatedTopic> soundposter = getAllFeaturedSoundposter();
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


    private void checkRequestAuthentication() throws WebApplicationException {
        if (acService.getUsername() == null) {
            throw new WebApplicationException(new Throwable("You have to be logged in."), 401);
        }
    }

    private boolean hasPosterProfileRelation(Topic profileAlias, Topic poster) {
        // ### check if profile is active..
        Topic profile = profileAlias.getRelatedTopic("dm4.core.composition", "dm4.core.child", "dm4.core.parent",
                "com.soundposter.account", true, false);
        ResultSet<RelatedTopic> items = profile.getRelatedTopics("com.soundposter.author_edge", "dm4.core.default",
                "dm4.core.default", "dm4.topicmaps.topicmap", true, false, 0);
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

    private String getPosterGraphicURL (Topic poster) {
        if (poster.getCompositeValue().has("dm4.files.file")) {
            Topic graphic = poster.getCompositeValue().getTopic("dm4.files.file");
            if (graphic.getCompositeValue().has("dm4.files.path")) {
                String path = graphic.getCompositeValue().getString("dm4.files.path");
                return "/filerepo" + path;
            }
        }
        return null;
    }

    private String getPreviewGraphicURL (Topic poster) {
        Topic previewGraphic = poster.getRelatedTopic("com.soundposter.preview_graphic_edge",
                DEFAULT_TYPE_URI, DEFAULT_TYPE_URI, "dm4.files.file", true, false);
        if (previewGraphic != null) {
            return "/filerepo" + previewGraphic.getCompositeValue().getString("dm4.files.path");
        }
        return null;
    }

    private String getProfileAliasForPoster(Topic poster) {
        // todo: check if profile is active..
        RelatedTopic author = poster.getRelatedTopic("com.soundposter.author_edge", "dm4.core.default",
                "dm4.core.default", "com.soundposter.account", true, false);
        if (author == null) return null;
        if (author.getCompositeValue().has("com.soundposter.account_alias")) {
            return author.getCompositeValue().getString("com.soundposter.account_alias");
        }
        return null;
    }

    private Topic getProfileTopic(Topic username) {
        RelatedTopic profile = username.getRelatedTopic("dm4.core.aggregation", "dm4.core.child",
                "dm4.core.parent", "com.soundposter.account", true, false);
        if (profile == null) return null;
        return profile;
    }

    private Association createAuthorRelation(Topic sound, Topic username) {
        return dms.createAssociation(new AssociationModel("com.soundposter.author_edge",
                new TopicRoleModel(sound.getId(), "dm4.core.parent"),
                new TopicRoleModel(username.getId(), "dm4.core.child")), null);
    }

    /** All this could and should move to the common audiolib project (aal-plugin). */

    private Topic createSoundCloudTrackTopic(SearchedTrack object, ClientState clientState) {
        //
        String uri = SOUNDCLOUD_TRACK_ID_PREFIX + object.trackId;
        Topic exists = dms.getTopic("uri", new SimpleValue(uri), true);
        if (exists != null) return exists;
        CompositeValueModel model = new CompositeValueModel();
        model.put(SOUND_NAME_URI, object.title);
        model.put(SOUND_SOURCE_URI, object.source_url);
        model.put(SOUND_STREAM_URI, object.streaming_url + "?consumer_key=" + SOUNDCLOUD_CLIENT_ID);
        model.put(SOUND_DESCRIPTION_URI, object.description);
        model.put(SOUND_ORDINAL_URI, object.ordinal); // 0 if not set
        // model.add(SOUND_ARTIST_URI, object.artist_name);
        // model.add(SOUND_ALBUM_URI, object.album_name);
        model.put(SOUND_LICENSE_URI, object.license_info);
        model.put(SOUND_PUBLISHER_INFO_URI, object.publisher_url);
        model.put(SOUND_PUBLISHER_NAME_URI, object.publisher_name);
        model.put(SOUND_ARTWORK_URI, object.artwork_url);
        TopicModel soundModel = new TopicModel(uri, SOUND_URI, model);
        //
        return dms.createTopic(soundModel, clientState);
    }

    private Topic createSoundCloudSetTopic(SearchedSet object, ClientState clientState) {
        //
        String uri = SOUNDCLOUD_SET_ID_PREFIX + object.setId;
        Topic exists = dms.getTopic("uri", new SimpleValue(uri), true);
        if (exists != null) return exists;
        CompositeValueModel model = new CompositeValueModel();
        model.put(SET_NAME_URI, object.title);
        model.put(SET_DESCRIPTION_URI, object.description);
        model.put(SOUND_SOURCE_URI, object.source_url);
        model.put(SOUND_PUBLISHER_INFO_URI, object.publisher_url);
        model.put(SOUND_PUBLISHER_NAME_URI, object.publisher_name);
        for (int i = 0; i < object.tracks.size(); i++) {
            SearchedTrack track = object.tracks.get(i);
            Topic topic = createSoundCloudTrackTopic(track, clientState); // gets or creates a sound-topic
            model.addRef(SOUND_URI, topic.getId());
        }
        TopicModel soundModel = new TopicModel(uri, SET_URI, model);
        //
        return dms.createTopic(soundModel, clientState);
    }

    private SearchedSet getSoundCloudSetById (int setId) {
        try {
            String endpoint = "http://api.soundcloud.com/playlists/" +setId+ ".json?client_id=" + SOUNDCLOUD_CLIENT_ID;
            URL url = new URL(endpoint);
            log.info("Sending request to: " + url.toURI().toURL().toString());
            URLConnection conn = url.openConnection();
            // BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO-8859-1"));
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            JSONTokener tokener = new JSONTokener(sb.toString());
            JSONObject result = new JSONObject(tokener);
            //
            return createSoundCloudSearchedSet(result);
        } catch (URISyntaxException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex.getCause());
        } catch (JSONException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex.getCause());
        } catch (IOException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex.getCause());
        }
    }

    private SearchedTrack getSoundCloudTrackById (int trackId) {
        try {
            String endpoint = "http://api.soundcloud.com/tracks/" +trackId+ ".json?client_id=" + SOUNDCLOUD_CLIENT_ID;
            URL url = new URL(endpoint);
            log.info("Sending request to: " + url.toURI().toURL().toString());
            URLConnection conn = url.openConnection();
            // BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO-8859-1"));
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            JSONTokener tokener = new JSONTokener(sb.toString());
            //
            return createSoundCloudSearchedTrack(new JSONObject(tokener), 0);
        } catch (URISyntaxException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex.getCause());
        } catch (JSONException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex.getCause());
        } catch (IOException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex.getCause());
        }
    }

    private JSONArray getSoundCloudTracksBySearchTerm(String term, int pageNr) {
        int limit = 25;
        int offset = 0;
        if (pageNr != 0) offset = limit * pageNr;
        try {
            JSONArray results = new JSONArray();
            String endpoint = "http://api.soundcloud.com/tracks.json?client_id=" + SOUNDCLOUD_CLIENT_ID
                    + "&q=" + term + "&limit=" + limit + "&offset=" + offset;
            URL url = new URL(endpoint);
            log.info("Migration1 sending request to: " + url.toURI().toURL().toString());
            URLConnection conn = url.openConnection();
            // BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO-8859-1"));
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            JSONTokener tokener = new JSONTokener(sb.toString());
            results = new JSONArray(tokener);
            //
            return results;
        } catch (URISyntaxException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex.getCause());
        } catch (JSONException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex.getCause());
        } catch (IOException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex.getCause());
        }
    }

    private JSONArray getSoundCloudSetsBySearchTerm(String term, int pageNr) {
        int limit = 7;
        int offset = 0;
        if (pageNr != 0) offset = limit * pageNr;
        try {
            JSONArray results = new JSONArray();
            String endpoint = "http://api.soundcloud.com/playlists.json?client_id=" + SOUNDCLOUD_CLIENT_ID
                + "&q=" + term + "&limit=" + limit + "&offset=" + offset;
            URL url = new URL(endpoint);
            log.info("Migration1 sending request to: " + url.toURI().toURL().toString());
            URLConnection conn = url.openConnection();
            // BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO-8859-1"));
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            JSONTokener tokener = new JSONTokener(sb.toString());
            results = new JSONArray(tokener);
            //
            return results;
        } catch (URISyntaxException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex.getCause());
        } catch (JSONException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex.getCause());
        } catch (IOException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex.getCause());
        }
    }

    private SearchedTrack createSoundCloudSearchedTrack(JSONObject item, int ordinal) throws JSONException {
        // todo: at artwork_url, sound-page permalink_url, user_link
        String description = (item.getString("description") == null) ? "" : item.getString("description");
        String artwork_url = item.getString("artwork_url");
        if (!item.has("stream_url")) return null; // sanity check
        return new SearchedTrack(item.getString("title"), item.getString("stream_url"),
            item.getJSONObject("user").getString("permalink_url"),item.getJSONObject("user").getString("username"),
            item.getString("permalink_url"), item.getBoolean("streamable"), description, item.getInt("id"), ordinal,
            item.getString("license"), artwork_url.replace("-large", "-original"));
    }

    private SearchedSet createSoundCloudSearchedSet(JSONObject set) throws JSONException {
            //
        ArrayList<SearchedTrack> items = new ArrayList<SearchedTrack>();
        JSONArray tracks = set.getJSONArray("tracks");
        for (int k = 0; k < tracks.length(); k++) {
            try {
                JSONObject track = tracks.getJSONObject(k);
                SearchedTrack searchedTrack = createSoundCloudSearchedTrack(track, (k+1));
                if (searchedTrack != null) items.add(searchedTrack);
            } catch (JSONException ex) {
                log.fine("SoundCloud Track is missing from set (?)");
            }
        }
        SearchedSet result = new SearchedSet(set.getInt("id"), set.getString("title"),
            set.getJSONObject("user").getString("permalink_url"), set.getJSONObject("user").getString("username"),
            set.getString("permalink_url"), set.getString("description"), items);
        return result;
    }

    /** Legacy routes to an old soundposter.com */

    private InputStream invokeFrontpageView() {
        try {
            return dms.getPlugin("com.soundposter.webapp").getResourceAsStream("web/website/index.html");
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private InputStream invokeC3SView() {
        try {
            return dms.getPlugin("com.soundposter.webapp").getResourceAsStream("web/c3s/index.html");
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private InputStream getSoundposterFavIcon() {
        try {
            return dms.getPlugin("com.soundposter.webapp").getResourceAsStream("web/images/favicon.ico");
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    /** Code running once, after plugin initialization. */

    private void checkACLsOfMigration() {
        // todo: initiate "admin" soundposter account topic to have old, migrated items authored semantically consistent
        ResultSet<RelatedTopic> sounds = dms.getTopics("com.soundposter.sound", false, 0);
        Iterator<RelatedTopic> soundset = sounds.getIterator();
        while (soundset.hasNext()) {
            RelatedTopic sound = soundset.next();
            DeepaMehtaTransaction dmx = dms.beginTx();
            try {
                if (acService.getCreator(sound) == null) {
                    log.info("initial ACL update of imported soundtrack " + sound.getSimpleValue().toString());
                    Topic admin = acService.getUsername("admin");
                    String adminName = admin.getSimpleValue().toString();
                    acService.setCreator(sound, adminName);
                    acService.setOwner(sound, adminName);
                    acService.setACL(sound, new AccessControlList( //
                            new ACLEntry(Operation.WRITE, UserRole.OWNER)));
                    // adding soundposter specific author semantics
                    createAuthorRelation(sound, admin);
                }
                dmx.success();
           } catch (Exception ex) {
                dmx.failure();
                log.info(ex.getMessage());
                throw new RuntimeException(ex);
            } finally {
                dmx.finish();
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
		}/**  else if(service instanceof SoundposterService) {
            spService = (SoundposterService) service;
            "com.soundposter.plugin.service.SoundposterService"
        } **/
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
		}/**  else if (service == spService) {
            service = null;
        } **/
    }

    private ArrayList<RelatedTopic> getResultSetSortedByTitle (ResultSet<RelatedTopic> all, ClientState clientState) {
        // build up sortable collection of all result-items
        ArrayList<RelatedTopic> in_memory = new ArrayList<RelatedTopic>();
        for (RelatedTopic obj : all) {
            in_memory.add(obj);
        }
        // sort all result-items
        Collections.sort(in_memory, new Comparator<RelatedTopic>() {
            public int compare(RelatedTopic t1, RelatedTopic t2) {
                return t1.getSimpleValue().toString().toLowerCase()
                        .compareTo(t2.getSimpleValue().toString().toLowerCase());
            }
        });
        return in_memory;
    }

}
