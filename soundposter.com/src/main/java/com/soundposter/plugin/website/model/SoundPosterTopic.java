package com.soundposter.plugin.website.model;

import com.soundposter.plugin.website.WebsitePlugin;
import de.deepamehta.core.JSONEnabled;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.DeepaMehtaService;
import java.util.ArrayList;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author <malte@mikromedia.de>
 */
public class SoundPosterTopic implements JSONEnabled {
     
    ArrayList<Topic> streams;
    Topic soundposter;
    UserProfile account;
    
    DeepaMehtaService dms;
    WebsitePlugin website;
    
    private static final String POSTER_WEB_ALIAS = "com.soundposter.web_alias";
    private static final String POSTER_PUBLISHED = "com.soundposter.published";
    private static final String POSTER_SUBTITLE = "com.soundposter.poster_subtitle";
    private static final String POSTER_DESCRIPTION = "com.soundposter.poster_description";
    private static final String POSTER_HASHTAG = "com.soundposter.poster_hashtag";
    private static final String POSTER_LICENSE_INFO = "com.soundposter.license_info";
    private static final String POSTER_CUSTOM_STYLE_HREF = "com.soundposter.custom_style";
    private static final String POSTER_BUY_LINK_HREF = "com.soundposter.buy_link_href";
    private static final String POSTER_BUY_LINK_LABEL = "com.soundposter.buy_link_label";
    private static final String POSTER_FEATURED = "com.soundposter.featured";
    private static final String POSTER_BUY_EDGE = "com.soundposter.buy_edge";
    private static final String POSTER_PREVIEW_GRAPHIC_EDGE = "com.soundposter.preview_graphic_edge"; 
    
    public SoundPosterTopic(long streamId, long posterId, DeepaMehtaService dms, WebsitePlugin plugin) {
        this.dms = dms;
        this.website = plugin;
        this.soundposter = dms.getTopic(posterId);
        // this.streams = dms.getTopic(id).loadChildTopics();
    }
    
    public SoundPosterTopic(Topic poster, DeepaMehtaService dms, WebsitePlugin plugin) {
        this.dms = dms;
        this.website = plugin;
        this.soundposter = poster;
        // this.streams = dms.getTopic(id).loadChildTopics();
    }

    public JSONObject toJSON() {
        return soundposter.loadChildTopics().toJSON();
    }
    
    /** ArrayList<Topic> getStreams() {
        return soundposter.getChildTopics("com.soundposter.sound");
    } **/
    
    public boolean isPublished() {
        soundposter.loadChildTopics(POSTER_PUBLISHED);
        if (soundposter.getChildTopics().has(POSTER_PUBLISHED)) {
            return soundposter.getChildTopics().getBoolean(POSTER_PUBLISHED); // soundposter is not published
        }
        return false; 
    }
    
    public Topic getTopic() {
        return soundposter;
    }
    
    public String getTitle () {
        return soundposter.getSimpleValue().toString();
    }
    
    public String getUsernameWebAlias() {
        return website.getProfileAliasForPoster(soundposter);
    }
    
    public String getWebAlias() {
        String value = "";
        soundposter.loadChildTopics(POSTER_WEB_ALIAS);
        if (soundposter.getChildTopics().has(POSTER_WEB_ALIAS)) {
            value = soundposter.getChildTopics().getString(POSTER_WEB_ALIAS);
        }
        return value;
    }
    
}
