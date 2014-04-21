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

    public String title = "", streaming_url = "", publisher_url = "", publisher_name = "",
            source_url = "", description = "";
    public int setId = 0;
    public ArrayList<SearchedTrack> tracks = null;

    public SearchedSet(int id, String title, String publisher_url, String publisher_name, String source_url,
            String description, ArrayList<SearchedTrack> tracks) {
        this.setId = id;
        this.title = title;
        this.publisher_url = publisher_url;
        this.publisher_name = publisher_name;
        this.source_url = source_url;
        this.description = description;
        this.tracks = tracks;
    }

}
