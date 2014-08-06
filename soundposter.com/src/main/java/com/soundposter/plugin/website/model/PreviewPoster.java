/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soundposter.plugin.website.model;

import de.deepamehta.core.Topic;
import java.util.Date;
import java.util.List;

/**
 *
 * @author malt
 */
public class PreviewPoster {

    public String title = "", url = "", username = "", background_css = "", onclick = "", description = "",
            subtitle = "";
    public List<Topic> tags;
    public long last_modified;

    public PreviewPoster(String title, String url, String username, String preview_style, String onclick,
            String subtitle, String description, long last_modified) {
        this.title = title;
        this.url = url;
        this.username = username;
        this.background_css = preview_style;
        this.onclick = onclick;
        this.description = description;
        this.subtitle = subtitle;
        this.tags = null;
        this.last_modified = last_modified;
    }

}
