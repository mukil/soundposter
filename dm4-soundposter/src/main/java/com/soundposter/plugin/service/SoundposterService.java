package com.soundposter.plugin.service;

import com.soundposter.plugin.model.SearchedSet;
import com.soundposter.plugin.model.SearchedTrack;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.PluginService;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public interface SoundposterService extends PluginService {
    
    void setSoundCloudClientID(String key);
    
    void setBandcampAPIKey(String key);

    SearchedSet getSoundCloudSetById(int setId);
    
    SearchedTrack getSoundCloudTrackById(int trackId);
    
    SearchedSet createSoundCloudSearchedSet(JSONObject set) throws JSONException;
    
    Topic createSoundCloudSetTopic(SearchedSet set);
    
    SearchedTrack createSoundCloudSearchedTrack(JSONObject item, int ordinalNr) throws JSONException;
    
    Topic createSoundCloudTrackTopic(SearchedTrack track);

    JSONArray getSoundCloudTracksBySearchTerm(String searchTerm, int pageNr);
    
    JSONArray getSoundCloudSetsBySearchTerm(String searchTerm, int pageNr);

    JSONArray findBandcampBands(String artistName);

    JSONArray findBandcampAlbums(String bandId);

    JSONObject getBandcampAlbum(String albumId);
    
}