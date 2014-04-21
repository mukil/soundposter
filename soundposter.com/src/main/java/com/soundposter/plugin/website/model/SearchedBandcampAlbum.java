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
public class SearchedBandcampAlbum {

    public String name = "", url = "", small_art_url = "", large_art_url = "";

    public String album_id = "";
    public String track_id = "";
    public String band_id = "";
    public String artist_name = "";

    public int release_date = -1;

    public SearchedBandcampAlbum(long album, long track, long band_id, String title, String url,
            String artwork_url_small, String artwork_url_large, String artist) {
        if (album >= 0) {
            this.album_id = "" + album;
        }
        if (track >= 0) {
            this.track_id = "" + track;
        }
        this.band_id = "" + band_id;
        this.small_art_url = artwork_url_small;
        this.large_art_url = artwork_url_large;
        // this.release_date = release_date;
        this.name = title;
        this.url = url;
        this.artist_name = artist;
    }

}
