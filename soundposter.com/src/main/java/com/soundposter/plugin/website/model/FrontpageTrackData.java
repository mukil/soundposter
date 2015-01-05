/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soundposter.plugin.website.model;

import com.soundposter.plugin.website.WebsitePlugin;
import de.deepamehta.core.JSONEnabled;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.DeepaMehtaService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author malt
 */
public class FrontpageTrackData implements JSONEnabled {

    public String sound_name = "", author = "", poster_title = "", poster_alias ="", profile_alias = "", poster_link = "",
            subtitle = "", description = "", license = "", hashtag = "", link = "", sound_provider = "", graphic = "";
    public long sound_id = -1;
    
    //    
    private DeepaMehtaService dms = null;
    private WebsitePlugin website = null;
    private Topic sound = null;
    private Topic soundposter = null;
    
    private String sound_url = "";
    private String sound_source_url = "";
    
    public FrontpageTrackData(long id, String name, String resource, String author, String postername, String subtitle,
            String description, String license, String hashtag, String graphicPath, String posterlink, String provider) {
        this.sound_id = id;
        this.sound_name = name;
        this.sound_url = resource;
        this.poster_title = postername;
        this.poster_link = posterlink;
        this.author = author;
        // this.profile_alias = profile_alias;
        this.description = description;
        this.subtitle = subtitle;
        this.graphic = graphicPath;
        // 
        this.license = license;
        this.hashtag = hashtag;
        this.sound_provider = provider;
        // 
        this.poster_alias = "";
    }
    
    /** public FrontpageTrackData(Topic sound, Topic soundposter, DeepaMehtaService dms, WebsitePlugin plugin) {
        this.dms = dms;
        this.website = plugin;
        this.sound = sound;
        this.soundposter = soundposter;
        
        // --- Sound Access ---
        String resourceUrl = "";
        sound.loadChildTopics(SOUND_STREAM_URI);
        if (sound.getChildTopics().has(SOUND_STREAM_URI)) {
            resourceUrl = sound.getChildTopics().getString(SOUND_STREAM_URI);
        }
        this.sound_url = resourceUrl;
        // ...
        String sourceUrl = "";
        sound.loadChildTopics(SOUND_SOURCE_URI);
        if (sound.getChildTopics().has(SOUND_SOURCE_URI)) {
            sourceUrl = sound.getChildTopics().getString(SOUND_SOURCE_URI);
        }
        this.sound_source_url = sourceUrl;
        
        // --- Soundposter Access ---
        // Validates if related soundposter is (a) published, (b) has a web-alias and (c) a related user profile.
        String posterWebAlias = "", profileWebAlias = "";
        soundposter.loadChildTopics(POSTER_PUBLISHED);
        if (soundposter.getChildTopics().has(POSTER_PUBLISHED)) {
            if (!soundposter.getChildTopics().getBoolean(POSTER_PUBLISHED)) {
            }
        } else {
        }
        profileWebAlias = getProfileAliasForPoster(soundposter);
        if (profileWebAlias == null) {
            // no user profile related to related soundposter
        }
        soundposter.loadChildTopics(POSTER_WEB_ALIAS);
        if (soundposter.getChildTopics().has(POSTER_WEB_ALIAS)) {
            posterWebAlias = soundposter.getChildTopics().getString(POSTER_WEB_ALIAS);
        } else {
            // no web alias related to related soundposter
        }
        // after check passes, prepare our template
        this.poster_name = soundposter.getSimpleValue()); // catch missing name
        this.poster_alias =  posterWebAlias;
        this.profile_alias = profileWebAlias;
            
            
        String username = website.getProfileAliasForPoster(sound); //  can be null
        String graphicPath = website.getPreviewGraphicURL(soundposter);
        String webalias = soundposter.getChildTopics().getString(POSTER_WEB_ALIAS);
        String subtitle = soundposter.getChildTopics().getString(POSTER_SUBTITLE);
        String description = soundposter.getChildTopics().getString(POSTER_DESCRIPTION);
        String hashtag = soundposter.getChildTopics().getString(POSTER_HASHTAG);
        if (hashtag.isEmpty()) hashtag = "soundposter";
        String license_info = soundposter.getChildTopics().getString(POSTER_LICENSE_INFO);
        // fixme: username might be null, if not set up correctly
        String url = "/" + username + "/" + webalias;
    } **/
    
    public JSONObject toJSON() {
        try {
            JSONObject data = new JSONObject();
            data.put("sound_id", sound_id);
            data.put("sound_name", sound_name);
            data.put("sound_url", sound_url);
            data.put("sound_provider", sound_provider);
            data.put("author", author);
            data.put("poster_name", poster_title);
            data.put("poster_alias", poster_alias);
            data.put("poster_link", poster_link);
            // data.put("profile_alias", profile_alias);
            data.put("subtitle", subtitle);
            data.put("description", description);
            data.put("license", license);
            data.put("hashtag", hashtag);
            data.put("graphic", graphic);
            return data;
        } catch (JSONException ex) {
            Logger.getLogger(FrontpageTrackData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
