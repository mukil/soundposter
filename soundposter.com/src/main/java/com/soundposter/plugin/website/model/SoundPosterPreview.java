package com.soundposter.plugin.website.model;

import com.soundposter.plugin.website.WebsitePlugin;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.DeepaMehtaService;
import java.util.ArrayList;

/**
 *
 * @author <malte@mikromedia.de>
 */
public class SoundPosterPreview {

    // ArrayList<Topic> streams;
    Topic soundposter;
    // UserProfile account;
    
    DeepaMehtaService dms;
    WebsitePlugin website;
    
    private static final String DM4_TIME_MODIFIED = "dm4.time.modified";
    
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
    
    public SoundPosterPreview(long topicId, DeepaMehtaService dms, WebsitePlugin plugin) {
        this.dms = dms;
        this.website = plugin;
        this.soundposter = dms.getTopic(topicId);
    }
    
    public String getTitle() {
        return soundposter.getSimpleValue().toString();
    }
    
    public String getDescription() {
        return soundposter.getChildTopics().getString(POSTER_DESCRIPTION);
    }
    
    public String getUrl() {
        return "/" + getUsername() + "/" + getWeb_alias();
    }
    
    public String getSubtitle() {
        return soundposter.getChildTopics().getString(POSTER_SUBTITLE);
    }
    
    public String getUsername() {
        return website.getProfileAliasForPoster(soundposter);
    }
    
    public String getWeb_alias() {
        return soundposter.getChildTopics().getString(POSTER_WEB_ALIAS);
    }
    
    public String getGraphic_url() {
        String filePath = website.getPreviewGraphicURL(soundposter);
        if (filePath == null) filePath = website.getPosterGraphicURL(soundposter);
        return filePath;
    }

    public String getBackground_css() {
        return "background: url(" + getGraphic_url() + ") 0px 0px no-repeat;";
    }
    
    public String getOnclick () {
        return "javascript:window.location.href=\""+getUrl()+"\"";
    }
    
    public long getLast_modified() {
        Object timevalue = soundposter.getProperty(DM4_TIME_MODIFIED);
        return Long.parseLong(timevalue.toString());
    }
    
    /** public JSONObject toJSON() {
        try {
            JSONObject object = new JSONObject();
            object.put("poster_title", getTitle());
            object.put("poster_subtitle", getSubtitle());
            object.put("poster_description", this.description);
            object.put("poster_web_alias", this.web_alias);
            object.put("poster_last_modified", this.last_modified);
            object.put("poster_css_path", this.background_css);
            object.put("poster_onclick", this.onclick);
            object.put("poster_url", this.url);
            object.put("poster_creator", this.username);
            return object;
        } catch (JSONException ex) {
            Logger.getLogger(BrowsePage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    } **/

}
