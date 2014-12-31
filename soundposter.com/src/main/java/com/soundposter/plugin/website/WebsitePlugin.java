package com.soundposter.plugin.website;

import com.soundposter.plugin.service.SoundposterService;
import com.soundposter.plugin.website.model.*;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.*;
import de.deepamehta.core.model.*;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.ResultList;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
import de.deepamehta.plugins.accesscontrol.model.ACLEntry;
import de.deepamehta.plugins.accesscontrol.model.AccessControlList;
import de.deepamehta.plugins.accesscontrol.model.Operation;
import de.deepamehta.plugins.accesscontrol.model.UserRole;
import de.deepamehta.plugins.topicmaps.model.TopicmapViewmodel;
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
 * @version 1.2-SNAPSHOT
 * @author Copyright 2012-2015, Malte Reißig <malte@mikromedia.de>
 *
 * Last modified: Dec 30, 2014
 */

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.TEXT_HTML)
public class WebsitePlugin extends WebActivatorPlugin {

    private Logger log = Logger.getLogger(getClass().getName());

    @Inject
    private AccessControlService acService;
    @Inject
    private TopicmapsService tmService;
    @Inject
    private SoundposterService spService;

    // --- DeepaMehta 4 URIs ---

    private static final String CHILD_URI = "dm4.core.child";
    private static final String PARENT_URI = "dm4.core.parent";
    private static final String DEFAULT_URI = "dm4.core.default";
    private static final String DM4_TIME_MODIFIED = "dm4.time.modified";
    private static final String TOPICMAP_TYPE_URI = "dm4.topicmaps.topicmap";
    private static final String SOUNDPOSTER_NAME = "dm4.topicmaps.name";

    private static final String PERSON_TYPE_URI = "dm4.contacts.person";
    private static final String MAILBOX_TYPE_URI = "dm4.contacts.email_address";

    // --- Soundposter 1 URIs

    private static final String SETLIST_LABEL_URI = "com.soundposter.setlist_label";

    private static final String SET_URI = "com.soundposter.set";
    private static final String SET_NAME_URI = "com.soundposter.set_name";
    private static final String SET_DESCRIPTION_URI = "com.soundposter.set_description";

    private static final String SOUND_URI = "com.soundposter.sound";
    private static final String SOUND_STREAM_URI = "dm4.webbrowser.url";
    private static final String SOUND_NAME_URI = "com.soundposter.sound_name";
    private static final String SOUND_ORDINAL_URI = "com.soundposter.ordinal_number"; // fixme: belongs into poster-context
    private static final String SOUND_ARTIST_URI = "com.soundposter.artist_name";
    private static final String SOUND_ALBUM_URI = "com.soundposter.album_name";
    private static final String SOUND_SOURCE_URI = "com.soundposter.source_page";
    private static final String SOUND_PUBLISHER_INFO_URI = "com.soundposter.publisher_info";
    private static final String SOUND_PUBLISHER_NAME_URI = "com.soundposter.publisher_name";
    private static final String SOUND_LICENSE_URI = "com.soundposter.license_info";
    private static final String SOUND_DESCRIPTION_URI = "com.soundposter.sound_description";
    private static final String SOUND_ARTWORK_URI = "com.soundposter.sound_artwork_url";
    
    private static final String POSTER_WEB_ALIAS = "com.soundposter.web_alias";
    private static final String POSTER_SUBTITLE = "com.soundposter.poster_subtitle";
    private static final String POSTER_DESCRIPTION = "com.soundposter.poster_description";
    private static final String POSTER_HASHTAG = "com.soundposter.poster_hashtag";
    private static final String POSTER_LICENSE_INFO = "com.soundposter.license_info";
    private static final String POSTER_PUBLISHED = "com.soundposter.published";
    private static final String POSTER_CUSTOM_STYLE_HREF = "com.soundposter.custom_style";
    private static final String POSTER_BUY_LINK_HREF = "com.soundposter.buy_link_href";
    private static final String POSTER_BUY_LINK_LABEL = "com.soundposter.buy_link_label";
    private static final String POSTER_FEATURED = "com.soundposter.featured";
    private static final String POSTER_BUY_EDGE = "com.soundposter.buy_edge";
    private static final String POSTER_PREVIEW_GRAPHIC_EDGE = "com.soundposter.preview_graphic_edge";    

    private static final String SP_ACCOUNT = "com.soundposter.account";
    private static final String SP_ACCOUNT_ALIAS = "com.soundposter.account_alias";
    private static final String SP_AUTHOR_EDGE = "com.soundposter.author_edge";
    
    private static final String SOUNDCLOUD_CLIENT_ID = "xgQpdzwTRicVIalDvCMTqQ";
    private static final String SOUNDCLOUD_TRACK_ID_PREFIX = "com.soundcloud.track.";
    private static final String SOUNDCLOUD_SET_ID_PREFIX = "com.soundcloud.set.";

    private static final String PATH_TO_STYLES = "/filerepo/stylesheets/";

    // --- Page IDs ###

    /** Soundposter.com Website Methods */

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getFrontpage() {
        log.info("Requesting front page without pathinfo.. ");
        viewData("pageId", "welcome");
        Topic featured = getRandomFeaturedSoundposter();
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
        String webalias = featured.getChildTopics().getString(POSTER_WEB_ALIAS);
        String subtitle = featured.getChildTopics().getString(POSTER_SUBTITLE);
        String description = featured.getChildTopics().getString(POSTER_DESCRIPTION);
        String hashtag = featured.getChildTopics().getString(POSTER_HASHTAG);
        if (hashtag.isEmpty()) hashtag = "soundposter";
        String license_info = featured.getChildTopics().getString(POSTER_LICENSE_INFO);
        // fixme: username might be null, if not set up correctly
        String username = getProfileAliasForPoster(featured); //  can be null
        String url = "/" + username + "/" + webalias;
        // viewData("poster", featured.getModel().toJSON().toString());
        viewData("graphic", graphicPath);
        viewData("title", featured.getChildTopics().getString(SOUNDPOSTER_NAME));
        viewData("subtitle", subtitle);
        viewData("license", license_info);
        viewData("description", description);
        viewData("hashtag", hashtag);
        viewData("author", username);
        viewData("link", url);
        // fetch some sound
        Topic frontpage_sound = getRandomSound();
        // fetch related soundposter and the related users webalias
        while (!isValidFrontpageSound(frontpage_sound)) {
            frontpage_sound = getRandomSound();
        }
        viewData("sound_id", frontpage_sound.getId());
        viewData("sound_name", frontpage_sound.getChildTopics().getString(SOUND_NAME_URI)); // catch missing name
        String resourceUrl = frontpage_sound.getChildTopics().getString(SOUND_STREAM_URI);
        // quick code provider-brand into the sounds view-data
        String provider = "Unknown";
        if (resourceUrl.contains("popplers")) provider = "Bandcamp";
        if (resourceUrl.contains("soundcloud")) provider = "SoundCloud";
        viewData("sound_provider", provider);
        // ### viewData("sound_artist", frontpage_sound.getCompositeValue().getString(SOUND_ARTIST_URI)); // are many
        return view("index");
    }

    private boolean isValidFrontpageSound (Topic sound) {

        // 1) check sound topic (for source, resource url, ordinal number and soundposter)
        int soundNr = -1;
        sound.loadChildTopics(SOUND_ORDINAL_URI);
        if (sound.getChildTopics().has(SOUND_ORDINAL_URI)) {
            try {
                soundNr = sound.getChildTopics().getInt(SOUND_ORDINAL_URI);
            } catch (ClassCastException ce) {
                try {
                    soundNr = (int) sound.getChildTopics().getLong(SOUND_ORDINAL_URI);
                } catch (ClassCastException ces) {
                    String value = sound.getChildTopics().getString(SOUND_ORDINAL_URI);
                    if (value.isEmpty()) return false;
                    soundNr = Integer.parseInt(value);
                }
            }
        }
        if (soundNr == -1) return false; // ### maybe we dont want to do that?
        // ..
        viewData("sound_nr", soundNr);
        String resourceUrl = "";
        sound.loadChildTopics(SOUND_STREAM_URI);
        if (sound.getChildTopics().has(SOUND_STREAM_URI)) {
            resourceUrl = sound.getChildTopics().getString(SOUND_STREAM_URI);
        }
        if (resourceUrl.isEmpty()) return false;
        viewData("sound_url", resourceUrl);
        // ..
        String sourceUrl = "";
        sound.loadChildTopics(SOUND_SOURCE_URI);
        if (sound.getChildTopics().has(SOUND_SOURCE_URI)) {
            sourceUrl = sound.getChildTopics().getString(SOUND_SOURCE_URI);
        }
        if (sourceUrl.isEmpty()) return false;
        viewData("sound_source_url", sourceUrl);
        // 2) check sounds related soundposter (as our publishing container for single sounds) if valid, too.
        ResultList<RelatedTopic> soundposters = getSoundposterBySound(sound);
        if (soundposters.getSize() > 0) {
            Topic soundposterTopic = soundposters.getItems().get(0);
            //
            // Validates if related soundposter is (a) published, (b) has a web-alias and (c) a related user profile.
            String posterWebAlias = "", profileWebAlias = "";
            soundposterTopic.loadChildTopics(POSTER_PUBLISHED);
            if (soundposterTopic.getChildTopics().has(POSTER_PUBLISHED)) {
                if (!soundposterTopic.getChildTopics().getBoolean(POSTER_PUBLISHED)) {
                    return false; // related soundposter is not published
                }
            } else {
                return false; // related soundposter is not published
            }
            profileWebAlias = getProfileAliasForPoster(soundposterTopic);
            if (profileWebAlias == null) {
                // no user profile related to related soundposter
                return false;
            }
            soundposterTopic.loadChildTopics(POSTER_WEB_ALIAS);
            if (soundposterTopic.getChildTopics().has(POSTER_WEB_ALIAS)) {
                posterWebAlias = soundposterTopic.getChildTopics().getString(POSTER_WEB_ALIAS);
            } else {
                // no web alias related to related soundposter
                return false;
            }
            // after check passes, prepare our template
            viewData("poster_name", soundposterTopic.getSimpleValue()); // catch missing name
            viewData("poster_alias", posterWebAlias);
            viewData("profile_alias", profileWebAlias);
            log.info("Identified soundposter \"" + soundposterTopic.getSimpleValue() + "\", Web-Alias: "
                    + posterWebAlias + " by user \"" + profileWebAlias + "\"");
            return true;
        }
        return false;
    }
    
    @GET
    @Path("/xml/sitemap")
    @Produces(MediaType.APPLICATION_XML)
    public String getSoundposterSitemap() {
        // ### 
        return "TBD";
    }

    @GET
    @Path("/manifest.webapp")
    @Produces("application/x-web-app-manifest+json")
    public String getFirefoxOSWebAppManifest() {
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
    public String getRobotsTxt() {
        return "User-agent: *\n\rDisallow: "; // Allow all
    }

    @GET
    @Path("/tsfestival")
    @Produces(MediaType.TEXT_HTML)
    public Response getTSF12View() {
        return Response.seeOther(URI.create("/walther/tsfestival")).build();
    }

    @GET
    @Path("/c3s")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getCCCSView() {
        return invokeC3SView();
    }

    @GET
    @Path("/favicon.ico")
    public InputStream getFavicon() {
        return getSoundposterFavIcon();
    }

	@GET
    @Path("/contact")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSignupPage() {
        log.info("Requesting soundposter.com sign-up page .. ");
		viewData("pageId", "sign-up");
        return view("signup");
    }

    @GET
    @Path("/philosophy")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPhilosophyPage() {
        log.info("Requesting soundposter.com sign-up page .. ");
		viewData("pageId", "philosophy");
        return view("philosophy");
    }

    @GET
    @Path("/legal")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getLegalPage() {
        log.info("Requesting soundposter.com legal page .. ");
		viewData("pageId", "legal");
        return view("legal");
    }

    @GET
    @Path("/help")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getHelpPage() {
        log.info("Requesting soundposter.com help page .. ");
		viewData("pageId", "help");
        return view("help");
    }

    @GET
    @Path("/imprint")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getImprint() {
        log.info("Requesting soundposter.com imprint page .. ");
		viewData("pageId", "imprint");
        return view("imprint");
    }


    @GET
    @Path("/about-us")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getAboutPage() {
        log.info("Requesting soundposter.com about page .. ");
		viewData("pageId", "about");
        return view("about");
    }

    @GET
    @Path("/pricing")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPricingPage() {
        log.info("Requesting soundposter.com plans and pricing page .. ");
		viewData("pageId", "pricing");
        return view("pricing");
    }

    /** Legacy routes to an old soundposter.com */

    private InputStream invokeC3SView() {
        try {
            return getStaticResource("web/c3s/index.html"); // ### compare index.html-s
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private InputStream getSoundposterFavIcon() {
        try {
            return getStaticResource("web/images/mini_SP_Logo_mit_dreieck_ffffff.png");
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/register")
    @Produces(MediaType.TEXT_HTML)
    public Viewable registerSimpleNewsAccount(@QueryParam("name") String name, @QueryParam("mailbox") String mailbox,
            @QueryParam("message") String message) {
        try {
            log.info("setting up new newsaccount ");
            ChildTopicsModel personData =  new ChildTopicsModel()
                .add(MAILBOX_TYPE_URI, new TopicModel(MAILBOX_TYPE_URI, new SimpleValue(mailbox)))
                .put("dm4.contacts.person_name", new TopicModel("dm4.contacts.person_name",
                    new ChildTopicsModel(new JSONObject().put("dm4.contacts.first_name", name))))
                .put("dm4.contacts.notes", "<p>" + message + "</p>");
            TopicModel userModel = new TopicModel(PERSON_TYPE_URI, personData);
            dms.createTopic(userModel);
            log.info("created new newsletter recipient (person)");
            viewData("pageId", "thanks");
            return view("thanks");
        } catch (Exception e) {
            log.warning(e.getMessage());
            throw new WebApplicationException(e.getCause());
        }
    }

    @GET
    @Path("/browse/{pageNr}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getBrowsePage(@PathParam("pageNr") int page_nr) {
        log.info("Requesting page " +page_nr+ " to browse all published soundposter.. ### Sort for the very latest");
        viewData("pageId", "browse");
        ArrayList<PreviewPoster> results = new ArrayList<PreviewPoster>();
        ResultList<RelatedTopic> all = getAllPublishedSoundposter();
        // build up sortable collection of all result-items (warning: in-memory copy of _all_ published soundposter)
        ArrayList<RelatedTopic> in_memory = getResultSetSortedByModificationDate(all);
        int max_count = 6;
        int offset = page_nr * max_count;
        // throw error if page is unexpected high or NaN
        int count = 0;
        for (RelatedTopic item : in_memory) {
            // start of preparing page results
            if (count >= offset) {
                // arrived at the interesting parts of result-set
                String title = item.getModel().getSimpleValue().toString();
                // fixme: username might be null, if not set up correctly
                String username = getProfileAliasForPoster(item);
                String web_alias = item.getChildTopics().getString(POSTER_WEB_ALIAS);
                String url = "/" + username + "/" + web_alias;
                String onclick = "javascript:window.location.href=\""+url+"\"";
                String graphic_url = getPreviewGraphicURL(item);
                //
                String poster_description = item.getChildTopics().getString(POSTER_DESCRIPTION);
                String poster_subtitle = item.getChildTopics().getString(POSTER_SUBTITLE);
                Object timevalue = item.getProperty(DM4_TIME_MODIFIED);
                long last_modified = Long.parseLong(timevalue.toString());
                // List<Topic> tags = item.getCompositeValue().getTopics("dm4.tags.tag");
                //
                if (graphic_url == null) graphic_url = getPosterGraphicURL(item);
                String preview_graphic_style = "background: url(" + graphic_url + ") 0px 0px no-repeat;";
                PreviewPoster poster = new PreviewPoster(title, url, username, preview_graphic_style, onclick,
                poster_description, poster_subtitle, last_modified);
                //
                results.add(poster);
                if (results.size() == max_count) break;
            }
            count++;
            // finished preparing page results
        }
        boolean on_first_page = (page_nr == 0) ? true : false;
        boolean on_last_page = (offset >= (all.getTotalCount() - max_count)) ? true : false;
        viewData("on_first_page", on_first_page);
        viewData("on_last_page", on_last_page);
        // returning results since result-set is full
        viewData("total_count", in_memory.size());
        viewData("page", page_nr);
        int overall_pages = (all.getTotalCount() % max_count == 0) ? all.getTotalCount()/max_count : (all.getTotalCount() / max_count) + 1;
        //
        int next_page = (page_nr == overall_pages) ? page_nr : page_nr + 1;
        int previous_page = (page_nr == 0) ? 0 : (page_nr - 1);
        viewData("previous_page_url", (page_nr > 0) ? "/browse/" + previous_page : "#");
        viewData("next_page_url", (on_last_page) ? "#": "/browse/" + next_page);
        // general
        viewData("pages", overall_pages);
        viewData("from", offset);
        viewData("to", offset + (offset - results.size()));
        viewData("overall", all.getTotalCount());
        viewData("set", results);
        return view("browse");
    }

    @GET
    @Path("/browse")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getBrowsePage() {
        log.info("Requesting page nr. 0 to browse all published soundposter.. ");
        viewData("pageId", "browse");
        return getBrowsePage(0);
    }

    @GET
    @Path("/{authorAlias}/{posterAlias}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPosterView (@PathParam("authorAlias") String author, @PathParam("posterAlias") String poster) {
        return getPosterViewWithTrack(author, poster, 0);
    }

    @GET
    @Path("/{authorAlias}/{posterAlias}/{trackId}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPosterViewWithTrack (@PathParam("authorAlias") String author, 
            @PathParam("posterAlias") String poster, @PathParam("trackId") long trackId) {
        String authorValue = author.trim(); // better for thymeleaf io
        String posterValue = poster.trim(); // better for thymeleaf io
        log.info("requesting posterview \""+ poster +"\" by author \"" + author + "\" and track \""+trackId+"\"");
        try {
            Topic soundposter = getSoundposter(authorValue, posterValue); // sanity checks already built-in
            String poster_description = "", name ="", graphicUrl = "", buylink = "{}", subtitle = "", license = "",
            hashtag = "", buylink_label = "", buylink_href = "", setlist_label = "", stylesheet = "",
            previewGraphicUrl = "";
            name = soundposter.getSimpleValue().toString();
            poster_description = soundposter.getChildTopics().getString(POSTER_DESCRIPTION);
            subtitle = soundposter.getChildTopics().getString(POSTER_SUBTITLE);
            license = soundposter.getChildTopics().getString(POSTER_LICENSE_INFO);
            buylink_label = soundposter.getChildTopics().getString(POSTER_BUY_LINK_LABEL);
            buylink_href = soundposter.getChildTopics().getString(POSTER_BUY_LINK_HREF);
            hashtag = soundposter.getChildTopics().getString(POSTER_HASHTAG);
            setlist_label = soundposter.getChildTopics().getString(SETLIST_LABEL_URI);
            Topic css = soundposter.getChildTopics().getTopic(POSTER_CUSTOM_STYLE_HREF);
            if (css != null && !css.getSimpleValue().toString().equals("")) {
                stylesheet = PATH_TO_STYLES + css.getSimpleValue().toString();
            }
            String pageTitle = "", mediaTitle = "";
            String sound_name = "", artist_name = "", album_name = "", sound_text = "";
            if (trackId != 0) {
                Topic track = dms.getTopic(trackId).loadChildTopics();
                if (track.getChildTopics().has(SOUND_NAME_URI)) {
                    sound_name = track.getChildTopics().getString(SOUND_NAME_URI);
                }
                /** if (track.getCompositeValue().has(SOUND_ARTIST_URI)) {
                    List<TopicModel> artists = track.getModel().getCompositeValueModel().getTopics(SOUND_ARTIST_URI);
                    artist_name = artists.get(0).getSimpleValue().toString(); // fixme: list all associated artists here
                } **/
                if (track.getChildTopics().has(SOUND_ALBUM_URI)) {
                    List<TopicModel> albums = track.getModel().getChildTopicsModel().getTopics(SOUND_ALBUM_URI);
                    album_name = albums.get(0).getSimpleValue().toString();
                }
                if (track.getChildTopics().has(SOUND_DESCRIPTION_URI)) {
                    sound_text = track.getChildTopics().getString(SOUND_DESCRIPTION_URI);
                }
                // Construct poster page title for a specific sound
                if (!artist_name.equals("")) {
                    pageTitle += artist_name + " ";
                    mediaTitle += artist_name + " ";
                }
                pageTitle += sound_name;
                mediaTitle = sound_name;
                if (!album_name.equals("")) {
                    pageTitle += " - " + album_name;
                    mediaTitle += " - " + album_name;
                }
                mediaTitle += " in " +name;
                pageTitle += " in " +name+ " on soundposter.com/" +authorValue+ "/" +posterValue;
                // set Sound Description text over Poster description text
                poster_description = sound_text;
            } else {
                // Construct poster page title
                pageTitle = name + " on soundposter.com/" + authorValue + "/" + posterValue;
            }
            // fixme: keywords, tracklist-data
            TopicmapViewmodel topicmap = tmService.getTopicmap(soundposter.getId(), true);
            JSONArray setlist = null;
            JSONArray soundlist = new JSONArray();
            try {
                setlist = topicmap.toJSON().getJSONArray("topics");
                for (int i = 0; i < setlist.length(); i++) {
                    JSONObject sound = setlist.getJSONObject(i);
                    if (sound.getString("type_uri").equals(SOUND_URI)) {
                        soundlist.put(dms.getTopic(sound.getLong("id")).loadChildTopics().toJSON());
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(WebsitePlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
            // prepare page, find the poster graphic
            graphicUrl = getPosterGraphicURL(soundposter);
            //
            previewGraphicUrl = getPreviewGraphicURL(soundposter);
            if (previewGraphicUrl == null) previewGraphicUrl = "";
            // get partner website link
            Topic map = dms.getTopic(soundposter.getId());
            Topic linkOne = map.getRelatedTopic(POSTER_BUY_EDGE,
                    DEFAULT_URI, DEFAULT_URI, "dm4.webbrowser.web_resource");
            if (linkOne != null) buylink = linkOne.toJSON().toString();
            // Prepare page data
            String url = "http://new.soundposter.com/" +authorValue+ "/" +posterValue;
            if (trackId != 0) url += "/" +trackId;
            viewData("url", url);
            viewData("pageTitle", pageTitle);
            viewData("mediaTitle", mediaTitle);
            viewData("name", name);
            viewData("username", authorValue);
            viewData("webalias", posterValue);
            viewData("subtitle", subtitle);
            viewData("description", poster_description);
            viewData("license", license);
            viewData("poster", topicmap.toJSON().toString());
            viewData("keywords", "");
            viewData("hashtag", hashtag);
            viewData("graphic", graphicUrl);
            viewData("preview_graphic", previewGraphicUrl);
            viewData("buylink", buylink);
            viewData("buylink_label", buylink_label);
            viewData("buylink_href", buylink_href);
            viewData("setlist_label", setlist_label);
            viewData("setlist", soundlist.toString());
            viewData("stylesheet", stylesheet);
            viewData("track", trackId);
        } catch (WebApplicationException ex) {
                log.info(ex.getMessage());
                throw new WebApplicationException(ex, ex.getResponse().getStatus());
        }
        return view("poster");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/poster/{profile}/{poster}")
    public Topic getSoundposter(@PathParam("profile") String profileAlias, @PathParam("poster") String posterAlias)
            throws WebApplicationException {
        log.info("Requesting soundposter-data for web alias " + posterAlias + " and profile " + profileAlias);
        String posterAliasValue = posterAlias.trim();
        String profileAliasValue = profileAlias.trim();
        Topic soundposter_alias = dms.getTopic(POSTER_WEB_ALIAS, new SimpleValue(posterAliasValue));
        /** Soundposter Alias sanity check */
        if (soundposter_alias == null) { // not found
            throw new WebApplicationException(new Throwable("Soundposter with web alias not found"), 404);
        }

        /** check if soundposter is published, otherwise return 401 */
        Topic soundposter = soundposter_alias.getRelatedTopic("dm4.core.composition", CHILD_URI, PARENT_URI, 
                TOPICMAP_TYPE_URI);
        if (soundposter == null) { // internal server error
            log.warning("  soundposter \""+ posterAliasValue +"\" found but data could not be loaded! "
                    + "(composite whole missing)\r\n");
            throw new WebApplicationException(new Throwable("Soundposter data could not be loaded"), 500);
        }

        /** Soundposter isPublished sanity check */
        boolean published = soundposter.getChildTopics().getBoolean(POSTER_PUBLISHED);
        if (!published) {
            log.warning("  soundposter \""+ posterAliasValue +"\" found but it is not yet published! "
                    + "(Access Denied)\r\n");
            throw new WebApplicationException(new Throwable("Soundposter is currently not published."), 401);
        }

        /** the following is yet dummy code, trying to retrieve songs independent from the topicmaps module*/
        ResultList<RelatedTopic> songs = soundposter.getRelatedTopics("dm4.topicmaps.topic_mapcontext",
                "dm4.topicmaps.topicmap_topic", PARENT_URI, SOUND_URI,  0);
        if (songs != null) log.fine("  "+ songs.getSize() +" songs in soundposter");

        /** Check if the poster could really be authored by this profile alias */
        Topic profile = dms.getTopic(SP_ACCOUNT_ALIAS, new SimpleValue(profileAliasValue));
        if (profile == null) { // internal server error
            log.info("  profile \""+ profileAliasValue +"\" not found \r\n");
            throw new WebApplicationException(new Throwable("Soundposter data could not be loaded. "
                    + "Profile not found"), 404);
        }

        /** last sanity check, there is a profile poster "author" relationship */
        if (!hasPosterProfileRelation(profile, soundposter)) {  // not found
            throw new WebApplicationException(new Throwable("Soundposter data could not be loaded. "
                    + "Poster has no proper author relation set."), 404);
        }
        return soundposter.loadChildTopics();
    }

    @GET
    @Path("/poster/published/all")
    public ResultList<RelatedTopic> getAllPublishedSoundposter() {
        ResultList<RelatedTopic> soundposter = dms.getTopics(TOPICMAP_TYPE_URI, 100);
        List<RelatedTopic> resultset = new ArrayList<RelatedTopic>();
        Iterator<RelatedTopic> results = soundposter.iterator();
        while (results.hasNext()) {
            RelatedTopic element = results.next();
            element.loadChildTopics(POSTER_PUBLISHED);
            if (element.getChildTopics().has(POSTER_PUBLISHED)) {
                if (element.getChildTopics().getBoolean(POSTER_PUBLISHED)) {
                    resultset.add(element);
                }
            }
        }
        return new ResultList<RelatedTopic>(resultset.size(), resultset);
    }

    @GET
    @Path("/poster/featured/all")
    public ResultList<RelatedTopic> getAllFeaturedSoundposter() {
        // performs no sanity check if poster is published, means a featured poster is published implicitly
        ResultList<RelatedTopic> soundposter = dms.getTopics(TOPICMAP_TYPE_URI, 100);
        List<RelatedTopic> resultset = new ArrayList<RelatedTopic>();
        Iterator<RelatedTopic> results = soundposter.iterator();
        while (results.hasNext()) {
            RelatedTopic element = results.next();
            element.loadChildTopics(POSTER_FEATURED);
            if (element.getChildTopics().has(POSTER_FEATURED)) {
                if (element.getChildTopics().getBoolean(POSTER_FEATURED)) {
                    resultset.add(element);
                }
            }
        }

        return new ResultList<RelatedTopic>(resultset.size(), resultset);
    }

    @GET
    @Path("/poster/random/published")
    public Topic getRandomPublishedSoundposter() {
        ResultList<RelatedTopic> soundposter = getAllPublishedSoundposter();
        Random rand = new Random();
        Object[] results = soundposter.getItems().toArray();
        Topic randomOne = null;
        if (soundposter.getSize() > 1) {
            randomOne = (Topic) results[rand.nextInt(soundposter.getSize()-1)];
        } else {
            if (soundposter.iterator().hasNext()) randomOne = soundposter.iterator().next();
        }
        return (randomOne != null) ? randomOne.loadChildTopics() : randomOne;
    }

    @GET
    @Path("/poster/random/featured")
    public Topic getRandomFeaturedSoundposter() {
        ResultList<RelatedTopic> soundposter = getAllFeaturedSoundposter();
        Random rand = new Random();
        Object[] results = soundposter.getItems().toArray();
        Topic randomOne = null;
        if (soundposter.getSize() > 1) {
            randomOne = (Topic) results[rand.nextInt(soundposter.getSize()-1)];
        } else {
            if (soundposter.iterator().hasNext()) randomOne = soundposter.iterator().next();
        }
        return (randomOne != null) ? randomOne.loadChildTopics() : randomOne;
    }

    @GET
    @Path("/sound/random")
    public Topic getRandomSound() {
        ResultList<RelatedTopic> tracks = dms.getTopics(SOUND_URI, 0);
        Random rand = new Random();
        Object[] results = tracks.getItems().toArray();
        Topic random_sound = (Topic) results[rand.nextInt(tracks.getSize()-1)];
        return dms.getTopic(random_sound.getId()).loadChildTopics();
    }

    @GET
    @Path("/sound/identify/poster")
    public ResultList<RelatedTopic> getSoundposterBySound(Topic soundTopic) {
        return soundTopic.getRelatedTopics("dm4.topicmaps.topic_mapcontext", "dm4.topicmaps.topicmap_topic",
                DEFAULT_URI, TOPICMAP_TYPE_URI, 1);
    }

    @GET
    @Path("/create/signup/{signupInfo}/{name}")
    public String createSignupInformation(@PathParam("signupInfo") String signup, @PathParam("name") String name) {
        try {
            Topic contact = dms.createTopic(new TopicModel(MAILBOX_TYPE_URI, new SimpleValue(signup)));
            // contact.setCompositeValue(new CompositeValue().put("dm4.contacts.first_name", name), clientState, null);
            // contact.setCompositeValue(new CompositeValue().put("dm4.contacts.last_name", "User"), clientState, null);
            // contact.setCompositeValue(new CompositeValue().put("dm4.contacts.email_address", "User"), clientState, null);
            // "caller assumes its mulit-value but its single value", what shall this mean?
        } catch (Exception e) {
            throw new WebApplicationException(e, 500);
        }
        return "{}";
    }

    /** Soundposter Website internals */

    private void checkRequestAuthentication() throws WebApplicationException {
        if (acService.getUsername() == null) {
            throw new WebApplicationException(new Throwable("You have to be logged in."), 401);
        }
    }

    private boolean hasPosterProfileRelation(Topic profileAlias, Topic poster) {
        // ### check if profile is active..
        Topic profile = profileAlias.getRelatedTopic("dm4.core.composition", CHILD_URI, PARENT_URI, SP_ACCOUNT);
        ResultList<RelatedTopic> items = profile.getRelatedTopics(SP_AUTHOR_EDGE, DEFAULT_URI,
                DEFAULT_URI, TOPICMAP_TYPE_URI, 0);
        if (items.getSize() > 0) {
            Iterator<RelatedTopic> soundposter = items.iterator();
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
        if (poster.getChildTopics().has("dm4.files.file")) {
            Topic graphic = poster.getChildTopics().getTopic("dm4.files.file");
            if (graphic.getChildTopics().has("dm4.files.path")) {
                String path = graphic.getChildTopics().getString("dm4.files.path");
                return "/filerepo" + path;
            }
        }
        return null;
    }

    private String getPreviewGraphicURL (Topic poster) {
        Topic previewGraphic = poster.getRelatedTopic(POSTER_PREVIEW_GRAPHIC_EDGE,
                DEFAULT_URI, DEFAULT_URI, "dm4.files.file");
        if (previewGraphic != null) {
            return "/filerepo" + previewGraphic.getChildTopics().getString("dm4.files.path");
        }
        return null;
    }

    private String getProfileAliasForPoster(Topic poster) {
        // ### todo: check if profile is active..
        RelatedTopic author = poster.getRelatedTopic(SP_AUTHOR_EDGE, DEFAULT_URI, DEFAULT_URI, SP_ACCOUNT);
        if (author == null) return null;
        author.loadChildTopics(SP_ACCOUNT_ALIAS);
        if (author.getChildTopics().has(SP_ACCOUNT_ALIAS)) {
            return author.getChildTopics().getString(SP_ACCOUNT_ALIAS);
        }
        return null;
    }

    private Topic getProfileTopic(Topic username) {
        RelatedTopic profile = username.getRelatedTopic("dm4.core.aggregation", CHILD_URI, PARENT_URI, SP_ACCOUNT);
        if (profile == null) return null;
        return profile;
    }

    private Association createAuthorRelation(Topic sound, Topic username) {
        return dms.createAssociation(new AssociationModel(SP_AUTHOR_EDGE,
                new TopicRoleModel(sound.getId(), PARENT_URI),
                new TopicRoleModel(username.getId(), CHILD_URI)));
    }



    /** SoundCloud Service Handlers (Soundposter Website) */

    @GET
    @Path("/soundcloud/add/track/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Topic addSoundCloudTrack(@PathParam("id") int trackId) {
        // fixme: relate track via "Author" to logged in user
        checkRequestAuthentication();
        SearchedTrack track = getSoundCloudTrackById(trackId);
        // creator and owner should be set correct if its a new track (to us)
        // todo: in any case relate track to username
        Topic sound = createSoundCloudTrackTopic(track);
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
    public Topic addSoundCloudSet(@PathParam("id") int trackId) {
        // fixme: relate set via "Author" to logged in user
        checkRequestAuthentication();
        //
        SearchedSet result = getSoundCloudSetById(trackId);
        Topic set = createSoundCloudSetTopic(result);
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
    @Path("/bandcamp/view/bands/{name}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getBandcampBandsView(@PathParam("name") String query) {
        //
        checkRequestAuthentication();
        //
        JSONArray bands = spService.findBandcampBands(query);
        ArrayList<SearchedBandcampBand> results = new ArrayList<SearchedBandcampBand>();
        for (int i = 0; i < bands.length(); i++) {
            try {
                //
                JSONObject item = bands.getJSONObject(i);
                log.info("Found Band on Bandcamp: " + item.getString("name") + " (" + item.getLong("band_id") + ")");
                String offsite_url = (!item.has("offsite_url")) ? "" : item.getString("offsite_url");
                SearchedBandcampBand searchedBand = new SearchedBandcampBand(item.getLong("band_id"),
                        item.getString("name"), item.getString("url"), item.getString("subdomain"), offsite_url);
                if (searchedBand != null) results.add(searchedBand);
            } catch (JSONException ex) {
                log.severe("Could not parse this band-result.. " + ex.getMessage());
            }
        }
        viewData("search_type", "bands");
        viewData("provider_name", "Bandcamp");
        viewData("pageId", "search-results");
        viewData("results", results);
        return view("band-results");
    }

    @GET
    @Path("/bandcamp/view/discography/{bandName}/{bandId}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getBandcampDiscographyView(@PathParam("bandName") String bandName, @PathParam("bandId") String bandId) {
        //
        checkRequestAuthentication();
        //
        JSONArray albums = spService.findBandcampAlbums(bandId);
        ArrayList<SearchedBandcampAlbum> results = new ArrayList<SearchedBandcampAlbum>();
        for (int i = 0; i < albums.length(); i++) {
            try {
                //
                JSONObject item = albums.getJSONObject(i);
                log.info("Found Album on Bandcamp: " + item.getString("title") + " by artist " + item.getString("artist"));
                long album_id = -1, track_id = -1;
                String artistName = "";
                if (item.has("album_id")) album_id = item.getLong("album_id");
                if (item.has("track_id")) track_id = item.getLong("track_id");
                if (item.has("arist")) artistName = item.getString("artist");
                String small_url = "", large_url = "";
                if (item.has("small_art_url")) small_url = item.getString("small_art_url");
                if (item.has("large_art_url")) item.getString("large_art_url");
                SearchedBandcampAlbum searchAlbum = new SearchedBandcampAlbum(album_id, track_id,
                        item.getLong("band_id"), item.getString("title"), item.getString("url"),
                        small_url, large_url, artistName);
                if (searchAlbum != null) results.add(searchAlbum);
            } catch (JSONException ex) {
                log.severe("Could not parse this album-result .. " + ex.getMessage());
            }
        }
        viewData("artist_name", bandName);
        //
        viewData("search_type", "albums");
        viewData("provider_name", "Bandcamp");
        viewData("pageId", "search-results");
        viewData("results", results);
        return view("album-results");
    }

    @GET
    @Path("/bandcamp/view/album/{artistName}/{albumName}/{albumId}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getBandcampAlbumView(@PathParam("artistName") String artistName,
            @PathParam("albumName") String albumName, @PathParam("albumId") String albumId) {
        //
        checkRequestAuthentication();
        //
        JSONObject album = spService.getBandcampAlbum(albumId);
        ArrayList<SearchedBandcampTrack> results = new ArrayList<SearchedBandcampTrack>();
        try {
            JSONArray tracks = album.getJSONArray("tracks");
            for (int i = 0; i < tracks.length(); i++) {
                //
                JSONObject item = tracks.getJSONObject(i);
                log.info("Found track on Bandcamp: " + item.getString("title") + " .. " );
                long album_id = -1, track_id = -1, band_id = -1;
                int number = -1;
                // int release_date = -1;
                if (item.has("album_id")) album_id = item.getLong("album_id");
                if (item.has("track_id")) track_id = item.getLong("track_id");
                if (item.has("band_id")) band_id = item.getLong("band_id");
                if (item.has("number")) number = item.getInt("number");
                //
                String about = "", credits = "", artist_name = "";
                if (item.has("about")) about = item.getString("about");
                if (item.has("credits")) credits = item.getString("credits");
                if (item.has("artist")) artist_name = item.getString("artist");
                //
                String small_url = "", large_url = "";
                if (item.has("small_art_url")) small_url = item.getString("small_art_url");
                if (item.has("large_art_url")) item.getString("large_art_url");
                // if (item.has("release_date") release_date = item.getInt("release_date");
                SearchedBandcampTrack searchAlbum = new SearchedBandcampTrack(track_id, number, item.getString("title"),
                        item.getString("streaming_url"), small_url, large_url, artist_name, about, credits);
                if (searchAlbum != null) {
                    results.add(searchAlbum);
                    log.info("  Streaming-url is \"" + searchAlbum.streaming_url + "\" "
                            + "(Track: "+searchAlbum.number+")");
                }
            }
        } catch (JSONException ex) {
            log.severe("Could not parse this album-result .. " + ex.getMessage());
        }
        viewData("artist_name", artistName);
        viewData("album_name", albumName);
        //
        viewData("search_type", "album");
        viewData("provider_name", "Bandcamp");
        viewData("pageId", "search-results");
        viewData("results", results);
        return view("bandcamp-album");
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

    private Topic createSoundCloudTrackTopic(SearchedTrack object) {
        //
        String uri = SOUNDCLOUD_TRACK_ID_PREFIX + object.trackId;
        Topic exists = dms.getTopic("uri", new SimpleValue(uri));
        if (exists != null) return exists;
        ChildTopicsModel model = new ChildTopicsModel();
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
        return dms.createTopic(soundModel);
    }

    private Topic createSoundCloudSetTopic(SearchedSet object) {
        //
        String uri = SOUNDCLOUD_SET_ID_PREFIX + object.setId;
        Topic exists = dms.getTopic("uri", new SimpleValue(uri));
        if (exists != null) return exists;
        ChildTopicsModel model = new ChildTopicsModel();
        model.put(SET_NAME_URI, object.title);
        model.put(SET_DESCRIPTION_URI, object.description);
        model.put(SOUND_SOURCE_URI, object.source_url);
        model.put(SOUND_PUBLISHER_INFO_URI, object.publisher_url);
        model.put(SOUND_PUBLISHER_NAME_URI, object.publisher_name);
        for (int i = 0; i < object.tracks.size(); i++) {
            SearchedTrack track = object.tracks.get(i);
            Topic topic = createSoundCloudTrackTopic(track); // gets or creates a sound-topic
            model.addRef(SOUND_URI, topic.getId());
        }
        TopicModel soundModel = new TopicModel(uri, SET_URI, model);
        //
        return dms.createTopic(soundModel);
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



    /**
     * SoundCloud Service Getters.
     * All this could and should move to the common audiolib project (aal-plugin).
     */

    private SearchedSet getSoundCloudSetById (int setId) {
        try {
            String endpoint = "http://api.soundcloud.com/playlists/" +setId+ ".json?client_id=" + SOUNDCLOUD_CLIENT_ID;
            URL url = new URL(endpoint);
            log.info("SoundCloud request: " + url.toURI().toURL().toString());
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
            log.info("SoundCloud request: " + url.toURI().toURL().toString());
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
            log.info("SoundCloud request: " + url.toURI().toURL().toString());
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
            log.info("SoundCloud request: " + url.toURI().toURL().toString());
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

    private ArrayList<RelatedTopic> getResultSetSortedByTitle (ResultList<RelatedTopic> all) {
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


    private ArrayList<RelatedTopic> getResultSetSortedByModificationDate (ResultList<RelatedTopic> all) {
        // build up sortable collection of all result-items
        ArrayList<RelatedTopic> in_memory = new ArrayList<RelatedTopic>();
        for (RelatedTopic obj : all) {
            in_memory.add(obj);
        }
        // sort all result-items
        Collections.sort(in_memory, new Comparator<RelatedTopic>() {
            public int compare(RelatedTopic t1, RelatedTopic t2) {
                long first_date = Long.parseLong(t1.getProperty(DM4_TIME_MODIFIED).toString());
                long second_date = Long.parseLong(t2.getProperty(DM4_TIME_MODIFIED).toString());
                if (first_date > second_date) return -1;
                if (second_date > first_date) return 1;
                return 0;
            }
        });
        return in_memory;
    }



    /** Initialize the migrated soundsets ACL-Entries. */

    @Override
    public void init() {
        initTemplateEngine();
    }

    @Override
    public void postInstall() {
        checkACLsOfMigration();
    }

    /** Code running once, after first plugin initialization. */

    private void checkACLsOfMigration() {
        // todo: initiate "admin" soundposter account topic to have old, migrated items authored semantically consistent
        ResultList<RelatedTopic> sounds = dms.getTopics("com.soundposter.sound", 0);
        Iterator<RelatedTopic> soundset = sounds.iterator();
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

}
