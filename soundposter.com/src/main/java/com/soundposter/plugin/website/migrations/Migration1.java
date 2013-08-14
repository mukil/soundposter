package com.soundposter.plugin.website.migrations;

import de.deepamehta.core.Association;
import de.deepamehta.core.CompositeValue;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.*;
import de.deepamehta.core.service.Migration;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;


public class Migration1 extends Migration {

    private Logger log = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods

    public void run() {

        // Get JSON-Tracks Result
        String allTracksUrl = "http://www.soundposter.com/core/topic/by_type/com.soundposter.sound?fetchComposite=true";
        String detailTrackUrl = "http://www.soundposter.com/core/topic/";
        String jsonResponse = loadOldTracksData(allTracksUrl, null);
        // Iterate over Resultset
        JSONTokener jsonTracks = new JSONTokener(jsonResponse);
        try {
            JSONObject allObjectTracks = new JSONObject(jsonTracks);
            log.info(allObjectTracks.getJSONArray("items").length() + " tracks loaded");
            JSONArray allTracks = allObjectTracks.getJSONArray("items");
            log.info(allTracks.length() + "tracks loaded");
            for (int i = 0; i < allTracks.length(); i++) {
                Object item = allTracks.get(i);
                if (item != null) {
                    if (item instanceof JSONObject) {
                        // log.info("item is JSONObject " + item.toString());
                        JSONObject track = (JSONObject) item;
                        int trackId = track.getInt("id");
                        String trackResponse = loadTrackData(detailTrackUrl + "" + trackId +"");
                        JSONTokener jsonTrack = new JSONTokener(trackResponse);
                        JSONObject song = new JSONObject(jsonTrack);
                        /** todo: include source_info, author and license_info **/
                        JSONObject composite = song.getJSONObject("composite");
                        int ordinalNr = 0;
                        String stream = "", album = "", artist = "", name = "", source_info = "",
                                license_info = "", author = "";
                        if (composite.has("com.soundposter.sound_name")) {
                            name = composite.getJSONObject("com.soundposter.sound_name").getString("value");
                        }
                        // must have, otherwise we should skip this entry
                        if (composite.has("dm4.webbrowser.url")) {
                            stream = composite.getJSONObject("dm4.webbrowser.url").getString("value");
                        }
                        if (composite.has("com.soundposter.album_name")) {
                            album = composite.getJSONArray("com.soundposter.album_name")
                                    .getJSONObject(0).getString("value"); // could be many
                        }
                        if (composite.has("com.soundposter.artist_name")) {
                            artist = composite.getJSONArray("com.soundposter.artist_name")
                                    .getJSONObject(0).getString("value"); // could be many
                        }
                        if (composite.has("com.soundposter.source_page")) {
                            source_info = composite.getJSONObject("com.soundposter.source_page").getString("value");
                        }
                        if (composite.has("com.soundposter.license_info")) {
                            license_info = composite.getJSONObject("com.soundposter.license_info").getString("value");
                        }
                        if (composite.has("com.soundposter.publisher_info")) {
                            author = composite.getJSONObject("com.soundposter.publisher_info").getString("value");
                        }
                        if (composite.has("com.soundposter.ordinal_number")) {
                            ordinalNr = composite.getJSONObject("com.soundposter.ordinal_number").getInt("value");
                        }
                        // get and clean some hickups from old site
                        String streamValue =  stream.replaceAll("\"", "");
                        createSoundTopic(ordinalNr, name, artist, album, streamValue, author, license_info, source_info);
                    }
                }
            }
        } catch (Exception et) {
            throw new RuntimeException(et);
        }
    }

    private void createSoundTopic(long ordinalNr, String soundName, String artistName,
            String albumName, String streamingUrl, String authorInfo, String licenseInfo, String sourceInfo) {
        // fixme: while re-using album-, artist-names!
        TopicModel soundModel = new TopicModel("com.soundposter.sound", new CompositeValueModel()
                .put("dm4.webbrowser.url", new TopicModel("dm4.webbrowser.url", new SimpleValue(streamingUrl))));
        soundModel.getCompositeValueModel().put("com.soundposter.ordinal_number", ordinalNr);
        try {
            Topic newSound = dms.createTopic(soundModel, null);
            if (!soundName.equals("")) {
                newSound.setCompositeValue(new CompositeValueModel()
                    .put("com.soundposter.sound_name", new TopicModel("com.soundposter.sound_name",
                        new SimpleValue(soundName))), null, null);
            }
            if (!artistName.equals("")) {
                Topic artist = getArtistNameTopic(artistName);
                if (artist != null) {
                    newSound.setCompositeValue(new CompositeValueModel()
                        .addRef("com.soundposter.artist_name", artist.getId()), null, null);
                } else {
                    //
                    newSound.setCompositeValue(new CompositeValueModel()
                        .add("com.soundposter.artist_name", new TopicModel("com.soundposter.artist_name",
                            new SimpleValue(artistName))), null, null);
                }
            }
            if (!albumName.equals("")) {
                Topic album = getAlbumNameTopic(albumName);
                if (album != null) {
                    newSound.setCompositeValue(new CompositeValueModel()
                        .addRef("com.soundposter.album_name", album.getId()), null, null);
                } else {
                    //
                    newSound.setCompositeValue(new CompositeValueModel()
                        .add("com.soundposter.album_name", new TopicModel("com.soundposter.album_name",
                            new SimpleValue(albumName))), null, null);
                }
            }
            if (!licenseInfo.equals("")) {
                newSound.setCompositeValue(new CompositeValueModel()
                    .put("com.soundposter.license_info", new TopicModel("com.soundposter.license_info",
                        new SimpleValue(licenseInfo))), null, null);
            }
            if (!authorInfo.equals("")) {
                newSound.setCompositeValue(new CompositeValueModel()
                    .put("com.soundposter.publisher_info", new TopicModel("com.soundposter.publisher_info",
                        new SimpleValue(licenseInfo))), null, null);
            }
            if (!sourceInfo.equals("")) {
                newSound.setCompositeValue(new CompositeValueModel()
                    .put("com.soundposter.source_page", new TopicModel("com.soundposter.source_page",
                        new SimpleValue(licenseInfo))), null, null);
            }
            // make sounds editable in the future
            dms.createAssociation(new AssociationModel("dm4.core.aggregation",
                    new TopicRoleModel(newSound.getId(), "dm4.core.parent"),
                    new TopicRoleModel("de.workspaces.deepamehta", "dm4.core.child")), null);
        } catch (Exception ce) {
            throw new RuntimeException(ce);
        }
    }

    private Topic getArtistNameTopic (String name) {
        // for index-mode fulltext_key, we dropped index_mode key which forces us to search via a phrase
        Set<Topic> artists = dms.searchTopics("\""+name+"\"", "com.soundposter.artist_name");
        for (Topic artist : artists) {
            if (artist.getSimpleValue().toString().equals(name)) return artist;
        }
        return null; // dms.getTopic("com.soundposter.artist_name", new SimpleValue(name), false);
    }

    private Topic getAlbumNameTopic (String name) {
        // for index-mode fulltext_key, we dropped index_mode key which forces us to search via a phrase
        Set<Topic> albums = dms.searchTopics("\""+name+"\"", "com.soundposter.album_name");
        for (Topic album : albums) {
            if (album.getSimpleValue().toString().equals(name)) return album;
        }
        return null; // dms.getTopic("com.soundposter.artist_name", new SimpleValue(name), false);
    }

    private String loadOldTracksData(String endpoint, String requestParameters) {
        String result = null;
        if (endpoint.startsWith("http://")) {
            try {
                // Send data
                String urlStr = endpoint;
                if (requestParameters != null && requestParameters.length() > 0) {
                    urlStr += "?" + requestParameters;
                }
                URL url = new URL(urlStr);
                log.info("Migration1 sending request to: " + url.toURI().toURL().toString());
                URLConnection conn = url.openConnection();
                // Get the response
                // BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO-8859-1"));
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                result = sb.toString();
                log.info("Migration1 finished loading data from " + url);
                /** DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(new InputSource(new StringReader(result)));**/
            } catch(UnknownHostException uke) {
                log.warning("*** Migration1 could not load the xml data to import from " + endpoint
                    + " message is: " + uke.getMessage());
                return null;
            } catch (Exception ex) {
                log.warning("*** Migration1 encountered problem: " + ex.getMessage());
                return null;
            }
        }
        return result;
    }

    private String loadTrackData(String endpoint) {
        String result = null;
        if (endpoint.startsWith("http://")) {
            try {
                // Send data
                String urlStr = endpoint;
                URL url = new URL(urlStr);
                log.info("Migration1 sending request to: " + url.toURI().toURL().toString());
                URLConnection conn = url.openConnection();
                // Get the response
                // BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO-8859-1"));
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                result = sb.toString();
                log.info("Migration1 finished loading data from " + url);
            } catch(UnknownHostException uke) {
                log.warning("*** Migraiton4 could not load the xml data to import from " + endpoint
                    + " message is: " + uke.getMessage());
                return null;
            } catch (Exception ex) {
                log.warning("*** Migration1 encountered problem: " + ex.getMessage());
                return null;
            }
        }
        return result;
    }

}
