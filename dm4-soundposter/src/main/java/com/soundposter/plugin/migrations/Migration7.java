package com.soundposter.plugin.migrations;

import de.deepamehta.core.AssociationType;
import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.*;
import de.deepamehta.core.service.Migration;



public class Migration7 extends Migration {

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {

        /** Enrich sounds about some additional attributes **/
        TopicType sound = dms.getTopicType("com.soundposter.sound", null);
        sound.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.sound", "com.soundposter.stream_unavailable", "dm4.core.one", "dm4.core.one"));
        sound.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.sound", "com.soundposter.sound_description", "dm4.core.one", "dm4.core.one"));
        // make "labels" and "events" aggregate "sounds"
        // todo: think a bit harder about "events", "locations" and other "sets"
        TopicType event = dms.getTopicType("com.soundposter.event", null);
        event.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "com.soundposter.event", "com.soundposter.sound", "dm4.core.one", "dm4.core.many"));

        TopicType stream_unavailable = dms.getTopicType("com.soundposter.stream_unavailable", null);
        assignWorkspace(stream_unavailable);
        TopicType sound_description = dms.getTopicType("com.soundposter.sound_description", null);
        assignWorkspace(sound_description);

        /** Enrich soundposter about some additional attributes */
        TopicType topicmap = dms.getTopicType("dm4.topicmaps.topicmap", null);
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.soundposter_description", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.soundposter_hashtag", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.buy_link_label", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.buy_link_href", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.setlist_label", "dm4.core.one", "dm4.core.one"));

        TopicType poster_description = dms.getTopicType("com.soundposter.soundposter_description", null);
        assignWorkspace(poster_description);
        TopicType poster_hashtag = dms.getTopicType("com.soundposter.soundposter_hashtag", null);
        assignWorkspace(poster_hashtag);
        TopicType buy_link_label = dms.getTopicType("com.soundposter.buy_link_label", null);
        assignWorkspace(buy_link_label);
        TopicType buy_link_href = dms.getTopicType("com.soundposter.buy_link_href", null);
        assignWorkspace(buy_link_href);
        TopicType setlist_label = dms.getTopicType("com.soundposter.setlist_label", null);
        assignWorkspace(setlist_label);

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
