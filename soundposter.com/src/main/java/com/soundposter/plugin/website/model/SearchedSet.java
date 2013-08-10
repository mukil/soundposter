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

    public String title = "", streaming_url = "", publisher_url = "", source_url = "", description = "";
    public boolean streamable = false;
    public int setId = 0;
    public ArrayList<SearchedTrack> tracks = null;

    public SearchedSet(int id, String title, String publisher_url, String source_url,
            boolean streamable, String description, ArrayList<SearchedTrack> tracks) {
        this.setId = id;
        this.title = title;
        this.publisher_url = publisher_url;
        this.streamable = streamable;
        this.source_url = source_url;
        this.description = description;
        this.tracks = tracks;
    }

}
