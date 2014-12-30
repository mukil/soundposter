package com.soundposter.plugin.migrations;

import de.deepamehta.core.AssociationType;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.AssociationDefinitionModel;
import de.deepamehta.core.service.Migration;



public class Migration2 extends Migration {

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {

        /** Enrich topicmap type about soundposter properties */
        TopicType topicmap = dms.getTopicType("dm4.topicmaps.topicmap");
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.web_alias", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.poster_subtitle", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.poster_description", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.license_info", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.poster_hashtag", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.setlist_label", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.buy_link_label", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.buy_link_href", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "dm4.topicmaps.topicmap", "com.soundposter.display_options", "dm4.core.one", "dm4.core.many"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "dm4.topicmaps.topicmap", "com.soundposter.custom_style", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "dm4.topicmaps.topicmap", "com.soundposter.custom_script", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.published", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.featured", "dm4.core.one", "dm4.core.one"));
        // in a soundposter, publisher name and url is implicitly available via the related user account, or
        // could be (at one point) get overriden by exploiting the home_edge

        /** E-Mail, Username and a lot of other stuff for a user account */
        TopicType account = dms.getTopicType("com.soundposter.account");
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "com.soundposter.account_alias", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "com.soundposter.account_name", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "com.soundposter.account", "dm4.accesscontrol.username", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "com.soundposter.account_active", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "dm4.contacts.email_address", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "dm4.contacts.first_name", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "dm4.contacts.last_name", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "com.soundposter.account", "dm4.contacts.city", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "com.soundposter.account", "dm4.contacts.country", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "com.soundposter.account_profile_age", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "com.soundposter.account", "com.soundposter.account_type", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "com.soundposter.account_started", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "com.soundposter.account_expires", "dm4.core.one", "dm4.core.one"));

        /** Assign "com.soundposter.marker_time" to AssocType "com.soundposter.marker" */
        AssociationType markerEdge = dms.getAssociationType("com.soundposter.marker_edge");
        markerEdge.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.marker_edge", "com.soundposter.marker_time", "dm4.core.one", "dm4.core.one"));

        /** Hide some dm-topics from create_menu */
        dms.getTopicType("dm4.contacts.person").getViewConfig()
            .addSetting("dm4.webclient.view_config", "dm4.webclient.show_in_create_menu", false);
        dms.getTopicType("dm4.contacts.institution").getViewConfig()
            .addSetting("dm4.webclient.view_config", "dm4.webclient.show_in_create_menu", false);
        dms.getTopicType("dm4.notes.note").getViewConfig()
            .addSetting("dm4.webclient.view_config", "dm4.webclient.show_in_create_menu", false);

        /** Enrich "File" (Graphics) about source_info, author_info and  license_info */
        TopicType file = dms.getTopicType("dm4.files.file");
        file.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.files.file", "com.soundposter.source_page", "dm4.core.one", "dm4.core.one"));
        file.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.files.file", "com.soundposter.publisher_info", "dm4.core.one", "dm4.core.one"));
        file.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.files.file", "com.soundposter.publisher_name", "dm4.core.one", "dm4.core.one"));
        file.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.files.file", "com.soundposter.license_info", "dm4.core.one", "dm4.core.one"));

    }

}
