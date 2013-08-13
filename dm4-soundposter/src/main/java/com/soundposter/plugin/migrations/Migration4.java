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

        // General
        TopicType sourceInfo = dms.getTopicType("com.soundposter.source_page", null);
        assignWorkspace(sourceInfo);
        TopicType authorInfo = dms.getTopicType("com.soundposter.publisher_info", null);
        assignWorkspace(authorInfo);
        TopicType authorName = dms.getTopicType("com.soundposter.publisher_name", null);
        assignWorkspace(authorName);
        TopicType licenseInfo = dms.getTopicType("com.soundposter.license_info", null);
        assignWorkspace(licenseInfo);

        // Poster
        TopicType webAlias = dms.getTopicType("com.soundposter.web_alias", null);
        assignWorkspace(webAlias);
        TopicType featuredFlag = dms.getTopicType("com.soundposter.featured", null);
        assignWorkspace(featuredFlag);
        TopicType publishedFlag = dms.getTopicType("com.soundposter.published", null);
        assignWorkspace(publishedFlag);
        TopicType poster_subtitle = dms.getTopicType("com.soundposter.poster_subtitle", null);
        assignWorkspace(poster_subtitle);
        TopicType poster_description = dms.getTopicType("com.soundposter.poster_description", null);
        assignWorkspace(poster_description);
        TopicType poster_hashtag = dms.getTopicType("com.soundposter.poster_hashtag", null);
        assignWorkspace(poster_hashtag);
        TopicType buy_link_label = dms.getTopicType("com.soundposter.buy_link_label", null);
        assignWorkspace(buy_link_label);
        TopicType buy_link_href = dms.getTopicType("com.soundposter.buy_link_href", null);
        assignWorkspace(buy_link_href);
        TopicType setlist_label = dms.getTopicType("com.soundposter.setlist_label", null);
        assignWorkspace(setlist_label);
        TopicType customStyle = dms.getTopicType("com.soundposter.custom_style", null);
        assignWorkspace(customStyle);
        TopicType customScript = dms.getTopicType("com.soundposter.custom_script", null);
        assignWorkspace(customScript);
        TopicType displayOptions = dms.getTopicType("com.soundposter.display_options", null);
        assignWorkspace(displayOptions);

        // Set
        TopicType set = dms.getTopicType("com.soundposter.set", null);
        assignWorkspace(set);
        TopicType set_name = dms.getTopicType("com.soundposter.set_name", null);
        assignWorkspace(set_name);
        TopicType set_description = dms.getTopicType("com.soundposter.set_description", null);
        assignWorkspace(set_description);

        // Text
        TopicType text = dms.getTopicType("com.soundposter.text", null);
        assignWorkspace(text);
        TopicType text_value = dms.getTopicType("com.soundposter.text_value", null);
        assignWorkspace(text_value);

        // Sound
        TopicType sound = dms.getTopicType("com.soundposter.sound", null);
        assignWorkspace(sound);
        TopicType soundName = dms.getTopicType("com.soundposter.sound_name", null);
        assignWorkspace(soundName);
        TopicType ordinalNumber = dms.getTopicType("com.soundposter.ordinal_number", null);
        assignWorkspace(ordinalNumber);
        TopicType artistName = dms.getTopicType("com.soundposter.artist_name", null);
        assignWorkspace(artistName);
        TopicType albumName = dms.getTopicType("com.soundposter.album_name", null);
        assignWorkspace(albumName);
        TopicType gigTime = dms.getTopicType("com.soundposter.gig_start_time", null);
        assignWorkspace(gigTime);
        TopicType stream_unavailable = dms.getTopicType("com.soundposter.stream_unavailable", null);
        assignWorkspace(stream_unavailable);
        TopicType sound_description = dms.getTopicType("com.soundposter.sound_description", null);
        assignWorkspace(sound_description);
        TopicType sound_artwork_url = dms.getTopicType("com.soundposter.sound_artwork_url", null);
        assignWorkspace(sound_artwork_url);

        // Event
        TopicType event = dms.getTopicType("com.soundposter.event", null);
        assignWorkspace(event);
        TopicType eventName = dms.getTopicType("com.soundposter.event_name", null);
        assignWorkspace(eventName);
        TopicType dateTime = dms.getTopicType("com.soundposter.event_start_date_time", null);
        assignWorkspace(dateTime);
        TopicType endDateTime = dms.getTopicType("com.soundposter.event_end_date_time", null);
        assignWorkspace(endDateTime);
        TopicType locationName = dms.getTopicType("com.soundposter.location_name", null);
        assignWorkspace(locationName);

        // Marker
        TopicType markerTime = dms.getTopicType("com.soundposter.marker_time", null);
        assignWorkspace(markerTime);

        // Remark
        TopicType remark = dms.getTopicType("com.soundposter.remark", null);
        assignWorkspace(remark);

        // Accounts should just be editable by user "admin"
        /** TopicType accountAlias = dms.getTopicType("com.soundposter.account_alias", null);
        assignWorkspace(accountAlias);
        TopicType accountName = dms.getTopicType("com.soundposter.account_name", null);
        assignWorkspace(accountName);
        TopicType accountStarted = dms.getTopicType("com.soundposter.account_started", null);
        assignWorkspace(accountStarted);
        TopicType accountExpires = dms.getTopicType("com.soundposter.account_expires", null);
        assignWorkspace(accountExpires);
        TopicType accountActive = dms.getTopicType("com.soundposter.account_active", null);
        assignWorkspace(accountActive);
        TopicType accountType = dms.getTopicType("com.soundposter.account_type", null);
        assignWorkspace(accountType);
        TopicType profile_age = dms.getTopicType("com.soundposter.account_profile_age", null);
        assignWorkspace(profile_age); **/

        // Thirdparty IDs
        TopicType thirdPartyId = dms.getTopicType("com.soundposter.thirdparty_id", null);
        assignWorkspace(thirdPartyId);

        // Assign all assocTypes to the default workspace
        AssociationType homeLink = dms.getAssociationType("com.soundposter.home_edge", null);
        assignWorkspace(homeLink);
        AssociationType moreLink = dms.getAssociationType("com.soundposter.more_edge", null);
        assignWorkspace(moreLink);
        AssociationType buyLink = dms.getAssociationType("com.soundposter.buy_edge", null);
        assignWorkspace(buyLink);
        AssociationType previewGraphicLink = dms.getAssociationType("com.soundposter.preview_graphic_edge", null);
        assignWorkspace(previewGraphicLink);
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
            new TopicRoleModel(topic.getId(), "dm4.core.parent"),
            new TopicRoleModel("de.workspaces.deepamehta", "dm4.core.child")
        ), null);
    }

    private boolean hasWorkspace(Topic topic) {
        return topic.getRelatedTopics("dm4.core.aggregation", "dm4.core.parent", "dm4.core.child",
            "dm4.workspaces.workspace", false, false, 0, null).getSize() > 0;
    }

}
