/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soundposter.plugin.website.model;

import java.util.ArrayList;

/**
 *
 * @author malt
 */
public class SearchedSet {

    public String title = "", streaming_url = "", username = "", permalink_url = "", description = "";
    public boolean streamable = false;
    public ArrayList<SearchedTrack> tracks = null;

    public SearchedSet(String title, String username, String permalink,
            boolean streamable, String description, ArrayList<SearchedTrack> tracks) {
        this.title = title;
        this.username = username;
        this.streamable = streamable;
        this.permalink_url = permalink;
        this.description = description;
        this.tracks = tracks;
    }

}
