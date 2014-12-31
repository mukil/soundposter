/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soundposter.plugin.model;

import java.util.ArrayList;

/**
 *
 * @author malt
 */
public class SearchedBandcampTrack {

    public String title = "", streaming_url = "", small_art_url = "", large_art_url = "", artist = "";
    public int number = -1;
    public long track_id = -1;
    //
    public String duration = "";
    public String credits = "";
    public String about = "";
    //
    public String album_id = "";
    public String band_id = "";

    public SearchedBandcampTrack(long trackId, int number, String title, String streaming_url, String artwork_url_small,
            String artwork_url_large, String artist, String about, String credits) {
        this.track_id = trackId;
        this.number = number;
        //
        this.artist = artist;
        this.about = about;
        this.credits = credits;
        //
        this.small_art_url = artwork_url_small;
        this.large_art_url = artwork_url_large;
        //
        this.title = title;
        this.streaming_url = streaming_url;
    }

}
