package com.soundposter.plugin.migrations;

import de.deepamehta.core.AssociationType;
import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.*;
import de.deepamehta.core.service.Migration;
import java.util.logging.Logger;


public class Migration4 extends Migration {
    
    private Logger log = Logger.getLogger(getClass().getName());
    
    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {
        
        // Assign all new topicTypes to the default workspace
        TopicType text = dms.getTopicType("com.soundposter.text", null);
        assignWorkspace(text);
        TopicType event = dms.getTopicType("com.soundposter.event", null);
        assignWorkspace(event);
        TopicType account = dms.getTopicType("com.soundposter.account", null);
        assignWorkspace(account);
        TopicType sound = dms.getTopicType("com.soundposter.sound", null);
        assignWorkspace(sound);
        TopicType soundName = dms.getTopicType("com.soundposter.sound_name", null);
        assignWorkspace(soundName);
        TopicType ordinaNumber = dms.getTopicType("com.soundposter.ordinal_number", null);
        assignWorkspace(ordinaNumber);
        TopicType artistName = dms.getTopicType("com.soundposter.artist_name", null);
        assignWorkspace(ordinaNumber);
        TopicType albumName = dms.getTopicType("com.soundposter.album_name", null);
        assignWorkspace(albumName);
        TopicType tag = dms.getTopicType("com.soundposter.tag", null);
        assignWorkspace(tag);
        TopicType report = dms.getTopicType("com.soundposter.report", null);
        assignWorkspace(report);
        TopicType lastModified = dms.getTopicType("com.soundposter.last_modified", null);
        assignWorkspace(lastModified);
        TopicType dateTime = dms.getTopicType("com.soundposter.date_time", null);
        assignWorkspace(dateTime);
        TopicType eventName = dms.getTopicType("com.soundposter.event_name", null);
        assignWorkspace(eventName);
        TopicType locationName = dms.getTopicType("com.soundposter.location_name", null);
        assignWorkspace(locationName);
        TopicType locationLon = dms.getTopicType("com.soundposter.location_lon", null);
        assignWorkspace(locationLon);
        TopicType locationLat = dms.getTopicType("com.soundposter.location_lat", null);
        assignWorkspace(locationLat);
        TopicType markerTime = dms.getTopicType("com.soundposter.marker_time", null);
        assignWorkspace(markerTime);
        TopicType remark = dms.getTopicType("com.soundposter.remark", null);
        assignWorkspace(remark);
        TopicType accountExpires = dms.getTopicType("com.soundposter.account_expires", null);
        assignWorkspace(accountExpires);
        TopicType accountAlias = dms.getTopicType("com.soundposter.account_alias", null);
        assignWorkspace(accountAlias);
        TopicType accountStarted = dms.getTopicType("com.soundposter.account_started", null);
        assignWorkspace(accountStarted);
        TopicType accountActive = dms.getTopicType("com.soundposter.account_active", null);
        assignWorkspace(accountActive);
        TopicType accountType = dms.getTopicType("com.soundposter.account_type", null);
        assignWorkspace(accountType);
        TopicType thirdParty = dms.getTopicType("com.soundposter.thirdparty", null);
        assignWorkspace(thirdParty);
        TopicType thirdPartyKey = dms.getTopicType("com.soundposter.thirdparty_key", null);
        assignWorkspace(thirdPartyKey);
        TopicType thirdPartyId = dms.getTopicType("com.soundposter.thirdparty_id", null);
        assignWorkspace(thirdPartyId);
        TopicType featuredFlag = dms.getTopicType("com.soundposter.featured", null);
        assignWorkspace(featuredFlag);
        TopicType publishedFlag = dms.getTopicType("com.soundposter.published", null);
        assignWorkspace(publishedFlag);
        TopicType webAlias = dms.getTopicType("com.soundposter.web_alias", null);
        assignWorkspace(webAlias);
        TopicType sourceInfo = dms.getTopicType("com.soundposter.source_info", null);
        assignWorkspace(sourceInfo);
        TopicType authorInfo = dms.getTopicType("com.soundposter.author_info", null);
        assignWorkspace(authorInfo);
        TopicType licenseInfo = dms.getTopicType("com.soundposter.license_info", null);
        assignWorkspace(licenseInfo);
        TopicType customStyle = dms.getTopicType("com.soundposter.custom_style", null);
        assignWorkspace(customStyle);
        TopicType customScript = dms.getTopicType("com.soundposter.custom_script", null);
        assignWorkspace(customScript);
        TopicType displayOptions = dms.getTopicType("com.soundposter.display_options", null);
        assignWorkspace(displayOptions);
        
        // Assign all assocTypes to the default workspace
        AssociationType buyLink = dms.getAssociationType("com.soundposter.buy_edge", null);
        assignWorkspace(buyLink);
        AssociationType homeLink = dms.getAssociationType("com.soundposter.home_edge", null);
        assignWorkspace(homeLink);
        AssociationType moreLink = dms.getAssociationType("com.soundposter.more_edge", null);
        assignWorkspace(moreLink);
        AssociationType graphicLink = dms.getAssociationType("com.soundposter.graphic_edge", null);
        assignWorkspace(graphicLink);
        AssociationType makerLink = dms.getAssociationType("com.soundposter.marker_edge", null);
        assignWorkspace(makerLink);
        AssociationType authorLink = dms.getAssociationType("com.soundposter.author_edge", null);
        assignWorkspace(authorLink);


    }
    
    /** Worskpace Assignment Helpers */
    
    private void assignWorkspace(Topic topic) {
        if (hasWorkspace(topic)) {
            return;
        }
        dms.createAssociation(new AssociationModel("dm4.core.aggregation",
            new TopicRoleModel(topic.getId(), "dm4.core.whole"),
            new TopicRoleModel("de.workspaces.deepamehta", "dm4.core.part")
        ), null);
    }
    
    private boolean hasWorkspace(Topic topic) {
        return topic.getRelatedTopics("dm4.core.aggregation", "dm4.core.whole", "dm4.core.part",
            "dm4.workspaces.workspace", false, false, 0, null).getSize() > 0;
    }

}
