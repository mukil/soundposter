package com.soundposter.plugin.migrations;

import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.AssociationDefinitionModel;
import de.deepamehta.core.service.Migration;



public class Migration2 extends Migration {

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {
        TopicType type = dms.getTopicType("dm4.topicmaps.topicmap", null);
            type.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
                "dm4.topicmaps.topicmap", "com.soundposter.web_alias", "dm4.core.one", "dm4.core.one"));
            type.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
                "dm4.topicmaps.topicmap", "com.soundposter.published", "dm4.core.one", "dm4.core.one"));
        // update web client icon configuration of the search topic
        // "dm4.webclient.add_to_create_menu": true, "dm4.webclient.is_searchable_unit": true,
        // hide "Person"-topic from create menu
        dms.getTopicType("dm4.contacts.person", null).getViewConfig()
            .addSetting("dm4.webclient.view_config", "dm4.webclient.add_to_create_menu", false);
        dms.getTopicType("dm4.contacts.person", null).getViewConfig()
            .addSetting("dm4.webclient.view_config", "dm4.webclient.is_searchable_unit", false);
        // hide "Institution"-topic from create menu
        dms.getTopicType("dm4.contacts.institution", null).getViewConfig()
            .addSetting("dm4.webclient.view_config", "dm4.webclient.add_to_create_menu", false);
        // hide "Note"-topic from create menu
        dms.getTopicType("dm4.notes.note", null).getViewConfig()
            .addSetting("dm4.webclient.view_config", "dm4.webclient.add_to_create_menu", false);
        dms.getTopicType("dm4.notes.note", null).getViewConfig()
            .addSetting("dm4.webclient.view_config", "dm4.webclient.is_searchable_unit", false);
        // addIconToTopicType("dm4.core.meta_type",   "box-gray.png");
        // Note: on the canvas HSL-specified colors are rendered pale (Safari and Firefox)
        // addColorToAssociationType("dm4.core.association",     "rgb(178, 178, 178)" /*"hsl(  0,  0%, 75%)"*/);
    }

    // ------------------------------------------------------------------------------------------------- Private Methods

    private void addIconToTopicType(String topicTypeUri, String iconfile) {
        addTopicTypeSetting(topicTypeUri, "icon", "/de.deepamehta.webclient/images/" + iconfile);
    }

    private void addColorToAssociationType(String assocTypeUri, String color) {
        addAssociationTypeSetting(assocTypeUri, "color", color);
    }
}
