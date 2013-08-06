package com.soundposter.plugin.website;

import com.soundposter.plugin.website.model.PreviewPoster;
import com.soundposter.plugin.website.model.SearchedSet;
import com.soundposter.plugin.website.model.SearchedTrack;
import de.deepamehta.core.service.ClientState;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.ResultSet;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.CompositeValue;
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
import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
@Consumes("application/json")
@Produces("application/json")
public class WebsitePlugin extends WebActivatorPlugin {

    private Logger log = Logger.getLogger(getClass().getName());
    private AccessControlService acService;
    private TopicmapsService tmService;
    // private SoundposterService service;
    private boolean isInitialized = false;

    private static final String CHILD_TYPE_URI = "dm4.core.part";
    private static final String PARENT_TYPE_URI = "dm4.core.whole";
    private static final String DEFAULT_TYPE_URI = "dm4.core.default";

    private static String PERSON_TYPE_URI = "dm4.contacts.person";
    private static String MAILBOX_TYPE_URI = "dm4.contacts.email_address";

    private static final String VIEW_POSTER_URL_PREFIX = "/posterview";

    private static final String SOUNDCLOUD_CLIENT_ID = "xgQpdzwTRicVIalDvCMTqQ";

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
        Topic featured = getRandomFeaturedSoundposter(clientState);
        if (featured == null) return view("index");
        // prepare page, find the poster graphic
        String graphicPath = getPosterGraphicURL(featured);
        String webalias = featured.getCompositeValue().getString("com.soundposter.web_alias");
        String username = getProfileAliasForPoster(featured);
        String url = VIEW_POSTER_URL_PREFIX + "/" + username + "/" + webalias;
        log.info("Poster URL => " + url);
        context.setVariable("poster", featured.getModel().toJSON().toString());
        context.setVariable("graphic", graphicPath);
        context.setVariable("subtitle", "Be aware of the baseline.");
        context.setVariable("author", "by " + username);
        context.setVariable("link", url);
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
		context.setVariable("pageId", "sign-up");
        return view("sign-up");
    }

    @GET
    @Path("/philosophy")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPhilosophyPage(@HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter.com sign-up page .. ");
		context.setVariable("pageId", "philosophy");
        return view("philosophy");
    }

    @GET
    @Path("/legal")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getLegalPage(@HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter.com legal page .. ");
		context.setVariable("pageId", "legal");
        return view("legal");
    }


    @GET
    @Path("/about-us")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getAboutPage(@HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting soundposter.com about page .. ");
		context.setVariable("pageId", "about");
        return view("about");
    }

    @GET
    @Path("/register")
    @Produces(MediaType.TEXT_HTML)
    public Viewable registerSimpleNewsAccount(@QueryParam("name") String name, @QueryParam("mailbox") String mailbox,
            @QueryParam("message") String message) {
        try {
            log.info("setting up new newsaccount ");
			CompositeValue personData =  new CompositeValue()
					.add("dm4.contacts.email_address", new TopicModel("dm4.contacts.email_address", new SimpleValue(mailbox)))
                    .put("dm4.contacts.person_name", new TopicModel("dm4.contacts.person_name",
                        new CompositeValue(new JSONObject().put("dm4.contacts.first_name", name))))
                    .put("dm4.contacts.notes", "<p>" + message + "</p>");
			TopicModel userModel = new TopicModel(PERSON_TYPE_URI, personData);
			dms.createTopic(userModel, null);
            log.info("created new newsletter recipient (person)");
            context.setVariable("pageId", "thanks");
			return view("thanks");
        } catch (Exception e) {
            log.warning(e.getMessage());
            throw new WebApplicationException(e.getCause());
        }
    }

    @GET
    @Path("/soundcloud/get/tracks/{query}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSoundCloudTracksView(@PathParam("query") String query) {
        JSONArray tracks = getSoundCloudTracksBySearchTerm(query);
        ArrayList<SearchedTrack> results = new ArrayList<SearchedTrack>();
        for (int i = 0; i < tracks.length(); i++) {
            try {
                //
                JSONObject item = tracks.getJSONObject(i);
                SearchedTrack result = createSoundCloudSearchedTrack(item);
                results.add(result);
            } catch (JSONException ex) {
                Logger.getLogger(WebsitePlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        context.setVariable("search_type", "tracks");
        context.setVariable("provider_name", "SoundCloud");
        context.setVariable("pageId", "search-results");
        context.setVariable("results", results);
        return view("track-results");
    }

    @GET
    @Path("/soundcloud/tracks/{query}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getSoundCloudTracksBySearchTerm(@PathParam("query") String term) {
        try {
            JSONArray results = new JSONArray();
            String endpoint = "http://api.soundcloud.com/tracks.json?client_id=" + SOUNDCLOUD_CLIENT_ID
                    + "&q=" + term + "&limit=25";
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

    @GET
    @Path("/soundcloud/get/sets/{query}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSoundCloudSetsView(@PathParam("query") String query) {
        JSONArray sets = getSoundCloudSetsBySearchTerm(query);
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
                        items.add(createSoundCloudSearchedTrack(track));
                    } catch (JSONException ex) {
                        log.fine("SoundCloud Track is missing from set (?)");
                    }
                }
                SearchedSet result = new SearchedSet(item.getString("title"),
                        item.getJSONObject("user").getString("username"), item.getString("permalink_url"),
                        false, item.getString("description"), items);
                results.add(result);
            } catch (JSONException ex) {
                log.severe(ex.getMessage());
                throw new WebApplicationException(ex.getCause());
            }
        }
        context.setVariable("search_type", "sets");
        context.setVariable("provider_name", "SoundCloud");
        context.setVariable("pageId", "search-results");
        context.setVariable("results", results);
        return view("set-results");
    }

    @GET
    @Path("/soundcloud/sets/{query}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getSoundCloudSetsBySearchTerm(@PathParam("query") String term) {
        try {
            JSONArray results = new JSONArray();
            String endpoint = "http://api.soundcloud.com/playlists.json?client_id=" + SOUNDCLOUD_CLIENT_ID
                + "&q=" + term + "&limit=7";
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

	@GET
    @Path("/browse/{pageNr}")
	@Produces(MediaType.TEXT_HTML)
    public Viewable getBrowsePage(@PathParam("pageNr") int page_nr, @HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting page " +page_nr+ " to browse all published soundposter.. ");
		context.setVariable("pageId", "browse");
        ArrayList<PreviewPoster> results = new ArrayList<PreviewPoster>();
        ResultSet<RelatedTopic> all = getAllPublishedSoundposter(clientState);
        // build up sortable collection of all result-items (warning: in-memory copy of _all_ published soundposter)
        ArrayList<RelatedTopic> in_memory = getResultSetSortedByTitle(all, clientState);
        int max_count = 6, modulo_half = 1, count = 0, start_count = page_nr * max_count;
        // throw error if page is unexpected high or NaN
        for (RelatedTopic item : in_memory) {
            // start of preparing page results
            if (count == start_count + max_count) { // reached n-soundposter, beginning at m + items-on-page
                // returning results since result-set is full
                context.setVariable("total_count", in_memory.size());
                context.setVariable("page", page_nr);
                int overall_pages = (all.getTotalCount() % max_count > modulo_half) ? (all.getTotalCount() / max_count) : all.getTotalCount() / max_count + 1;
                // int overall_pages = all.getTotalCount() / max_count;
                //
                int next_page = (page_nr == overall_pages) ? page_nr : page_nr + 1;
                int previous_page = (page_nr == 0) ? 0 : (page_nr - 1);
                boolean not_on_first_page = (page_nr > 0) ? true : false;
                boolean not_on_last_page = (page_nr == overall_pages) ? false : true;
                context.setVariable("not_on_first_page", not_on_first_page);
                context.setVariable("not_on_last_page", not_on_last_page);
                context.setVariable("previous_page_url", (page_nr > 0) ? "/browse/" + previous_page : "/browse/" + overall_pages);
                context.setVariable("next_page_url", "/browse/" + next_page);
                context.setVariable("pages", overall_pages);
                context.setVariable("from", start_count);
                context.setVariable("to", start_count + (start_count - results.size()));
                context.setVariable("set", results);
                return view("browse");
            }
            if (count >= start_count) {
                // arrived at the interesting parts of result-set
                String title = item.getModel().getSimpleValue().toString();
                String username = getProfileAliasForPoster(item);
                String web_alias = item.getCompositeValue().getString("com.soundposter.web_alias");
                String url = VIEW_POSTER_URL_PREFIX + "/" + username + "/" + web_alias;
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
        context.setVariable("total_count", in_memory.size());
        context.setVariable("page", page_nr);
        int overall_pages = (all.getTotalCount() % max_count > modulo_half) ? (all.getTotalCount() / max_count) : all.getTotalCount() / max_count + 1;
        // int overall_pages = all.getTotalCount() / max_count;
        //
        int next_page = (page_nr == overall_pages) ? 0 : page_nr + 1;
        int previous_page = (page_nr == 0) ? 0 : (page_nr - 1);
        context.setVariable("not_on_first_page", (page_nr > 0) ? true : false);
        context.setVariable("not_on_last_page", (page_nr == overall_pages) ? false : true);
        context.setVariable("previous_page_url", (page_nr > 0) ? "/browse/" + previous_page : "/browse");
        context.setVariable("next_page_url", "/browse/" + next_page);
        context.setVariable("pages", overall_pages);
        context.setVariable("from", start_count);
        context.setVariable("to", start_count + (start_count - results.size()));
        context.setVariable("set", results);
        return view("browse");
    }

    @GET
    @Path("/browse")
	@Produces(MediaType.TEXT_HTML)
    public Viewable getBrowsePage(@HeaderParam("Cookie") ClientState clientState) {
        log.info("Requesting page nr. 0 to browse all published soundposter.. ");
		context.setVariable("pageId", "browse");
        return getBrowsePage(0, clientState);
    }

	@GET
    @Path(VIEW_POSTER_URL_PREFIX + "/{authorAlias}/{posterAlias}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPosterView (@PathParam("authorAlias") String author, @PathParam("posterAlias") String poster) {
        log.info("requesting posterview \""+ poster +"\" by author \"" + author + "\"");
		return getPosterViewWithTrack(author, poster, 0);
    }

    @GET
    @Path(VIEW_POSTER_URL_PREFIX + "/{authorAlias}/{posterAlias}/{trackId}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPosterViewWithTrack (@PathParam("authorAlias") String author, @PathParam("posterAlias") String poster,
            @PathParam("trackId") long trackId) {
        log.info("requesting posterview \""+ poster +"\" by author \"" + author + "\" and track \""+trackId+"\"");
		try {
			Topic soundposter = getSoundposter(author, poster, null); // sanity checks already built-in
			String posterDescription = "", posterName ="", graphicUrl = "", buylink = "{}";
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
            graphicUrl = getPosterGraphicURL(soundposter);
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
            context.setVariable("graphic", graphicUrl);
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



    private SearchedTrack createSoundCloudSearchedTrack(JSONObject item) throws JSONException {
        // todo: at artwork_url, sound-page permalink_url, user_link
        return new SearchedTrack(item.getString("title"), item.getString("stream_url"),
            item.getJSONObject("user").getString("username"), item.getString("permalink_url"),
            item.getBoolean("streamable"), item.getString("description"));
    }

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

    private String getPosterGraphicURL (Topic poster) {
        TopicModel graphic = poster.getCompositeValue().getTopic("dm4.files.file");
        return "/filerepo" + graphic.getCompositeValue().getString("dm4.files.path");
    }

    private String getPreviewGraphicURL (Topic poster) {
        Topic previewGraphic = poster.getRelatedTopic("com.soundposter.preview_graphic_edge",
                DEFAULT_TYPE_URI, DEFAULT_TYPE_URI, "dm4.files.file", true, false, null);
        if (previewGraphic != null) {
            return "/filerepo" + previewGraphic.getCompositeValue().getString("dm4.files.path");
        }
        return null;
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
