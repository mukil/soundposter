/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soundposter.plugin.website.model;

/**
 *
 * @author malt
 */
public class SearchedTrack {

    public String title = "", streaming_url = "", username = "", permalink_url = "", description = "";
    public boolean streamable = false;

    public SearchedTrack(String title, String url, String username, String permalink,
            boolean streamable, String description) {
        this.title = title;
        this.streaming_url = url;
        this.username = username;
        this.streamable = streamable;
        this.permalink_url = permalink;
        this.description = description;
    }

}
