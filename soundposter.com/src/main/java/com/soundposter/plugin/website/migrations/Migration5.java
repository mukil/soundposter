package com.soundposter.plugin.website.migrations;

import de.deepamehta.core.Topic;
import de.deepamehta.core.model.*;
import de.deepamehta.core.service.Migration;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;


public class Migration5 extends Migration {

    private Logger log = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {

        // Get JSON-Tracks Result
        String allTracksUrl = "http://www.soundposter.com:8080/core/topic/by_type/dm4.soundposter.track";
        String detailTrackUrl = "http://www.soundposter.com:8080/core/topic/";
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
                        String soundName = song.getString("value");
                        JSONObject composite = song.getJSONObject("composite");
                        JSONObject artist = composite.getJSONObject("dm4.soundposter.artist");
                        JSONObject stream = composite.getJSONObject("dm4.webbrowser.url");
                        // get and clean some hickups from old site
                        String artistValue =  artist.getString("value").replaceAll("\"", "");
                        artistValue = artistValue.replaceAll("- ", "");
                        String streamValue =  stream.getString("value").replaceAll("\"", "");
                        createSoundTopic(soundName, artistValue, streamValue);
                    }
                }
            }
        } catch (Exception et) {
            throw new RuntimeException(et);
        }
    }

    private void createSoundTopic(String soundName, String artistName, String streamingUrl) {
        TopicModel soundModel = new TopicModel("com.soundposter.sound", new CompositeValue()
                .put("dm4.webbrowser.url", new TopicModel("dm4.webbrowser.url", new SimpleValue(streamingUrl))));
        try {
            Topic newSound = dms.createTopic(soundModel, null);
            if (!soundName.equals("")) {
                newSound.setCompositeValue(new CompositeValue()
                    .put("com.soundposter.sound_name", new TopicModel("com.soundposter.sound_name",
                        new SimpleValue(soundName))), null, null);
            }
            if (!artistName.equals("")) {
                newSound.setCompositeValue(new CompositeValue()
                    .add("com.soundposter.artist_name", new TopicModel("com.soundposter.artist_name",
                        new SimpleValue(artistName))), null, null);
            }
            // make sounds editable in the future
            dms.createAssociation(new AssociationModel("dm4.core.aggregation",
                    new TopicRoleModel(newSound.getId(), "dm4.core.whole"),
                    new TopicRoleModel("de.workspaces.deepamehta", "dm4.core.part")), null);
        } catch (Exception ce) {
            throw new RuntimeException(ce);
        }
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
                log.info("Migration4 sending request to: " + url.toURI().toURL().toString());
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
                log.info("Migration4 finished loading data from " + url);
                /** DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(new InputSource(new StringReader(result)));**/
            } catch(UnknownHostException uke) {
                log.warning("*** Migraiton4 could not load the xml data to import from " + endpoint
                    + " message is: " + uke.getMessage());
                return null;
            } catch (Exception ex) {
                log.warning("*** Migration4 encountered problem: " + ex.getMessage());
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
                log.info("Migration4 sending request to: " + url.toURI().toURL().toString());
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
                log.info("Migration4 finished loading data from " + url);
            } catch(UnknownHostException uke) {
                log.warning("*** Migraiton4 could not load the xml data to import from " + endpoint
                    + " message is: " + uke.getMessage());
                return null;
            } catch (Exception ex) {
                log.warning("*** Migration4 encountered problem: " + ex.getMessage());
                return null;
            }
        }
        return result;
    }

}
