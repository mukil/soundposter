package com.soundposter.plugin;


import com.soundposter.plugin.model.SearchedSet;
import com.soundposter.plugin.model.SearchedTrack;
import com.soundposter.plugin.service.SoundposterService;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.ChildTopicsModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.osgi.PluginActivator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.ws.rs.*;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

/**
 *
 * Soundposter Webservice running on <http://www.soundposter.com>
 * @version 1.1-SNAPSHOT
 * @author Copyright 2013-2014, Malte Reißig <malte@mikromedia.de>
 *
 * Last modified: Apr 21, 2014
 */

@Path("/service")
@Consumes("application/json")
@Produces("application/json")
public class SoundposterPlugin extends PluginActivator implements SoundposterService {

    private Logger log = Logger.getLogger(getClass().getName());
    
    /** @Inject
    private AccessControlService acService; **/

    private String bandcampApiKey = "";
    private String soundCloudClientId = "";
    
    // Sound Instance URIs by Provider

    private static final String SOUNDCLOUD_TRACK_ID_PREFIX = "com.soundcloud.track.";
    private static final String SOUNDCLOUD_SET_ID_PREFIX = "com.soundcloud.set.";
    
    // Soundposter Library URIs
    
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

    @Override
    public void setSoundCloudClientID(String key) {
        this.soundCloudClientId = key;
    }
    
    @Override
    public void setBandcampAPIKey(String key) {
        this.bandcampApiKey = key;
    }

    /** --- Bandcamp Service Getters ---
     * @param artistName
     * @return  JSONArray */

    @Override
    public JSONArray findBandcampBands(String artistName) {
        return getBandcampBandsByName(artistName);
    }

    private JSONArray getBandcampBandsByName(String name) {
        try {
            JSONArray results = new JSONArray();
            String endpoint = "http://api.bandcamp.com/api/band/3/search?name=" + name + "&key="
                    + bandcampApiKey;
            URL url = new URL(endpoint);
            log.info("Bandcamp Search API request: " + url.toURI().toURL().toString());
            URLConnection conn = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            log.fine("Bandcamp Response: " + sb.toString());
            JSONObject response = new JSONObject(sb.toString());
            results = response.getJSONArray("results");
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

    @Override
    public JSONArray findBandcampAlbums(String bandId) {
        return getBandcampDiscographyByBandId(bandId);
    }

    private JSONArray getBandcampDiscographyByBandId(String bandId) {
        try {
            JSONArray results = new JSONArray();
            String endpoint = "http://api.bandcamp.com/api/band/3/discography?band_id=" + bandId
                    + "&key=" + bandcampApiKey;
            URL url = new URL(endpoint);
            log.info("Bandcamp Discography API request: " + url.toURI().toURL().toString());
            URLConnection conn = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            log.fine("Bandcamp Response: " + sb.toString());
            JSONObject response = new JSONObject(sb.toString());
            results = response.getJSONArray("discography");
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

    @Override
    public JSONObject getBandcampAlbum(String albumId) {
        try {
            String endpoint = "http://api.bandcamp.com/api/album/2/info?album_id=" + albumId
                    + "&key=" + bandcampApiKey;
            URL url = new URL(endpoint);
            log.info("Bandcamp Album API request: " + url.toURI().toURL().toString());
            URLConnection conn = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            log.fine("Bandcamp Response: \n" + sb.toString());
            JSONObject response = new JSONObject(sb.toString());
            return response;
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

    /**
     * Implementing a soundposter search would trigger the following getTopic-Calls() to fulltext_key indexed types:
     *  Sound name, Sound Description, Set Name, Set Description, Poster Subtitle, Poster Description, Publisher Name,
     *  Album Name, Artist Name, futurewise: Tags, too and at last probably: com.soundposter.account_name
     **/
    
     /**
     * SoundCloud Service Getters.
     * All this could and should move to the common audiolib project (aal-plugin).
     */

    @Override
    public SearchedSet getSoundCloudSetById (int setId) {
        try {
            String endpoint = "http://api.soundcloud.com/playlists/" +setId+ ".json?client_id=" + soundCloudClientId;
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

    @Override
    public SearchedTrack getSoundCloudTrackById (int trackId) {
        try {
            String endpoint = "http://api.soundcloud.com/tracks/" +trackId+ ".json?client_id=" + soundCloudClientId;
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

    @Override
    public JSONArray getSoundCloudTracksBySearchTerm(String term, int pageNr) {
        int limit = 25;
        int offset = 0;
        if (pageNr != 0) offset = limit * pageNr;
        try {
            JSONArray results = new JSONArray();
            String endpoint = "http://api.soundcloud.com/tracks.json?client_id=" + soundCloudClientId
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
            throw new WebApplicationException(ex);
        } catch (JSONException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex);
        } catch (IOException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex);
        }
    }

    @Override
    public JSONArray getSoundCloudSetsBySearchTerm(String term, int pageNr) {
        int limit = 7;
        int offset = 0;
        if (pageNr != 0) offset = limit * pageNr;
        try {
            JSONArray results = new JSONArray();
            String endpoint = "http://api.soundcloud.com/playlists.json?client_id=" + soundCloudClientId
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
            throw new WebApplicationException(ex);
        } catch (JSONException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex);
        } catch (IOException ex) {
            log.severe(ex.getMessage());
            throw new WebApplicationException(ex);
        }
    }
    
    @Override
    public SearchedTrack createSoundCloudSearchedTrack(JSONObject item, int ordinal) throws JSONException {
        // todo: at artwork_url, sound-page permalink_url, user_link
        String description = (item.getString("description") == null) ? "" : item.getString("description");
        String artwork_url = item.getString("artwork_url");
        if (!item.has("stream_url")) return null; // sanity check
        return new SearchedTrack(item.getString("title"), item.getString("stream_url"),
            item.getJSONObject("user").getString("permalink_url"),item.getJSONObject("user").getString("username"),
            item.getString("permalink_url"), item.getBoolean("streamable"), description, item.getInt("id"), ordinal,
            item.getString("license"), artwork_url.replace("-large", "-original"));
    }

    @Override
    public SearchedSet createSoundCloudSearchedSet(JSONObject set) throws JSONException {
            //
        ArrayList<SearchedTrack> items = new ArrayList<SearchedTrack>();
        JSONArray tracks = set.getJSONArray("tracks");
        for (int k = 0; k < tracks.length(); k++) {
            try {
                JSONObject track = tracks.getJSONObject(k);
                SearchedTrack searchedTrack = createSoundCloudSearchedTrack(track, (k+1));
                if (searchedTrack != null) items.add(searchedTrack);
            } catch (JSONException ex) {
                log.warning("Creating SoundCloud Set failed, while parsing tracks ... ");
                throw ex;
            }
        }
        SearchedSet result = new SearchedSet(set.getInt("id"), set.getString("title"),
            set.getJSONObject("user").getString("permalink_url"), set.getJSONObject("user").getString("username"),
            set.getString("permalink_url"), set.getString("description"), items);
        return result;
    }

    @Override
    public Topic createSoundCloudTrackTopic(SearchedTrack object) {
        //
        String uri = SOUNDCLOUD_TRACK_ID_PREFIX + object.trackId;
        Topic exists = dms.getTopic("uri", new SimpleValue(uri));
        if (exists != null) return exists;
        ChildTopicsModel model = new ChildTopicsModel();
        model.put(SOUND_NAME_URI, object.title);
        model.put(SOUND_SOURCE_URI, object.source_url);
        model.put(SOUND_STREAM_URI, object.streaming_url + "?consumer_key=" + soundCloudClientId);
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

    @Override
    public Topic createSoundCloudSetTopic(SearchedSet object) {
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


}
