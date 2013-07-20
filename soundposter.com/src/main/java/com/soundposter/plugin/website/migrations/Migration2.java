package com.soundposter.plugin.website.migrations;

import de.deepamehta.core.AssociationType;
import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.AssociationDefinitionModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.service.Migration;



public class Migration2 extends Migration {

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {

        /** Create default workspace for all soundposter users */
        // Topic workspace = dms.createTopic(new TopicModel("dm4.workspaces.workspace"), null);
        // workspace.setSimpleValue(new SimpleValue("Soundposter"));
        // workspace.setUri("com.soundposter.workspace");

        /** Enrich topicmap type about soundposter properties */
        TopicType topicmap = dms.getTopicType("dm4.topicmaps.topicmap", null);
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.published", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.featured", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.web_alias", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "dm4.topicmaps.topicmap", "com.soundposter.display_options", "dm4.core.one", "dm4.core.many"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "dm4.topicmaps.topicmap", "com.soundposter.custom_style", "dm4.core.one", "dm4.core.many"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "dm4.topicmaps.topicmap", "com.soundposter.custom_script", "dm4.core.one", "dm4.core.many"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "dm4.topicmaps.topicmap", "com.soundposter.tag", "dm4.core.one", "dm4.core.many"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.license_info", "dm4.core.one", "dm4.core.one"));
        topicmap.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.topicmaps.topicmap", "com.soundposter.last_modified", "dm4.core.one", "dm4.core.one"));

        /** E-Mail, 3rd Party Id, username and a lot of other stuff for a user account */
        TopicType account = dms.getTopicType("com.soundposter.account", null);
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "com.soundposter.account_alias", "dm4.core.one", "dm4.core.one"));
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
            "com.soundposter.account", "com.soundposter.profile_age", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            "com.soundposter.account", "com.soundposter.account_type", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "com.soundposter.thirdparty", "dm4.core.one", "dm4.core.many"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "com.soundposter.account_started", "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.account", "com.soundposter.account_expires", "dm4.core.one", "dm4.core.one"));

        /** Assign "com.soundposter.marker_time" to AssocType "com.soundposter.marker" */
        AssociationType markerEdge = dms.getAssociationType("com.soundposter.marker_edge", null);
        markerEdge.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "com.soundposter.marker_edge", "com.soundposter.marker_time", "dm4.core.one", "dm4.core.one"));

        /** Hide some dm-topics from create_menu */
        dms.getTopicType("dm4.contacts.person", null).getViewConfig()
            .addSetting("dm4.webclient.view_config", "dm4.webclient.add_to_create_menu", false);
        dms.getTopicType("dm4.contacts.institution", null).getViewConfig()
            .addSetting("dm4.webclient.view_config", "dm4.webclient.add_to_create_menu", false);
        dms.getTopicType("dm4.notes.note", null).getViewConfig()
            .addSetting("dm4.webclient.view_config", "dm4.webclient.add_to_create_menu", false);

        /** Enrich "File" (Graphics) about source_info, author_info and  license_info */
        TopicType file = dms.getTopicType("dm4.files.file", null);
        file.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.files.file", "com.soundposter.source_info", "dm4.core.one", "dm4.core.one"));
        file.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.files.file", "com.soundposter.author_info", "dm4.core.one", "dm4.core.one"));
        file.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            "dm4.files.file", "com.soundposter.license_info", "dm4.core.one", "dm4.core.one"));

    }

}
