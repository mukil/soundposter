package com.soundposter.plugin;


import com.soundposter.plugin.service.SoundposterService;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import javax.ws.rs.*;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

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
    
    @Inject
    private AccessControlService acService;

    private final String BANDCAMP_API_SERVICE_KEY = "";
    // private final String SOUNDCLOUD_API_SERVICE_KEY = "";

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
                    + BANDCAMP_API_SERVICE_KEY;
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
                    + "&key=" + BANDCAMP_API_SERVICE_KEY;
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
                    + "&key=" + BANDCAMP_API_SERVICE_KEY;
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

}
