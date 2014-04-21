package com.soundposter.plugin.service;

import de.deepamehta.core.service.PluginService;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public interface SoundposterService extends PluginService {

    /* Topic getSoundposter(String profileAlias, String posterAlias, ClientState clientState);

    Topic getRandomPublishedSoundposter(ClientState clientState);

    String getPublishedSoundposterUrl(String posterId, ClientState clientState);

    String createSignupInformation(String signup, String name, ClientState clientState);

    ResultSet<RelatedTopic> getAllPublishedSoundposter(ClientState clientState);

    ResultSet<RelatedTopic> getAllFeaturedSoundposter(ClientState clientState);

    InputStream getSoundposterView(String profileAlias, String posterAlias, ClientState clientState);

    InputStream getWebsiteView(String pathInfo, ClientState clientState);

    // InputStream getRootView(ClientState clientState);

    InputStream getSiteIcon(ClientState clientState); **/

    // JSONArray getSoundCloudTracksBySearch(String searchTerm);

    JSONArray findBandcampBands(String artistName);

    JSONArray findBandcampAlbums(String bandId);

    JSONObject getBandcampAlbum(String albumId);

}