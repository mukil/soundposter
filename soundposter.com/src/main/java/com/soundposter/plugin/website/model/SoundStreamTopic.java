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
public class SoundStreamTopic implements JSONEnabled {
    
    Topic stream;
    
    ArrayList<Topic> soundposters;
    ArrayList<UserProfile> accounts;
    
    DeepaMehtaService dms;
    WebsitePlugin website;
    
    private int soundOrdinalNumber = 0;
    private String streamResourceURL = "";
    private String streamSourceURL = "";
    
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
    
    public SoundStreamTopic(long id, long posterId, DeepaMehtaService dms, WebsitePlugin plugin) {
        this.dms = dms;
        this.website = plugin;
        this.stream = dms.getTopic(id).loadChildTopics();
        // this.soundposters = dms.getTopic(posterId).loadChildTopics();
        // this.accounts = dms.getTopic(posterId).loadChildTopics();
    }
    
    public SoundStreamTopic(Topic sound, DeepaMehtaService dms, WebsitePlugin plugin) {
        this.dms = dms;
        this.website = plugin;
        this.stream = sound.loadChildTopics();
        // this.soundposters = dms.getTopic(posterId).loadChildTopics();
        // this.accounts = dms.getTopic(posterId).loadChildTopics();
    }
    
    public JSONObject toJSON() {
        return stream.loadChildTopics().toJSON();
    }
    
    public int getOrdinalNumber() {
        stream.loadChildTopics(SOUND_ORDINAL_URI);
        if (stream.getChildTopics().has(SOUND_ORDINAL_URI)) {
            try {
                soundOrdinalNumber = stream.getChildTopics().getInt(SOUND_ORDINAL_URI);
            } catch (ClassCastException ce) {
                try {
                    soundOrdinalNumber = (int) stream.getChildTopics().getLong(SOUND_ORDINAL_URI);
                } catch (ClassCastException ces) {
                    String value = stream.getChildTopics().getString(SOUND_ORDINAL_URI);
                    if (!value.isEmpty()) soundOrdinalNumber = Integer.parseInt(value);
                }
            }
        }
        return soundOrdinalNumber;
    }
    
    public String getStreamResourceURL () {
        stream.loadChildTopics(SOUND_STREAM_URI);
        if (stream.getChildTopics().has(SOUND_STREAM_URI)) {
            streamResourceURL = stream.getChildTopics().getString(SOUND_STREAM_URI);
        }
        return streamResourceURL;
    }
    
    public String getStreamSourceURL() {
        stream.loadChildTopics(SOUND_SOURCE_URI);
        if (stream.getChildTopics().has(SOUND_SOURCE_URI)) {
            streamSourceURL = stream.getChildTopics().getString(SOUND_SOURCE_URI);
        }
        return streamSourceURL;
    }

    public String getTitle() {
        return stream.getSimpleValue().toString();
    }

    public Topic getItem() {
        return stream;
    }

}
