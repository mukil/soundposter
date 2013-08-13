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

    // fixme: tag_list, bpm, label object, release date object, comment/play/fav_count waveform_url = "";
    public String title = "", streaming_url = "", username = "", source_url = "", description = "",
            publisher_url = "", publisher_name = "", artist_name = "", album_name = "", license_info = "",
            artwork_url = "";
    public int trackId = 0, ordinal = 0;
    public boolean streamable = false;

    public SearchedTrack(int trackId, int ordinal, String title, String streaming_url, String publisher_url,
            String publisher_name, String source_url,String description, String license, String album_name,
            String artist_name, String artwork_url) {
        this.trackId = trackId;
        this.ordinal = ordinal;
        this.title = title;
        this.album_name = album_name;
        this.artist_name = artist_name;
        this.streaming_url = streaming_url;
        this.publisher_url = publisher_url;
        this.publisher_name = publisher_name;
        this.source_url = source_url;
        this.description = description;
        this.license_info = license;
        this.artwork_url = artwork_url;
    }

    public SearchedTrack(int trackId, int ordinal, String title, String url, String userpage, String username,
            String source_url, String description, String license, String artwork_url) {
        this.trackId = trackId;
        this.ordinal = ordinal;
        this.title = title;
        this.streaming_url = url;
        this.publisher_url = userpage;
        this.username = username;
        this.source_url = source_url;
        this.description = description;
        this.license_info = license;
        this.artwork_url = artwork_url;
    }

    public SearchedTrack(String title, String url, String userpage, String source_url,
            boolean streamable, String description, int trackId, int ordinal) {
        this.title = title;
        this.streaming_url = url;
        this.publisher_url = userpage;
        this.streamable = streamable;
        this.source_url = source_url;
        this.description = description;
        this.trackId = trackId;
        this.ordinal = ordinal;
    }

    public SearchedTrack(String title, String url, String userpage, String publisher_name, String source_url,
            boolean streamable, String description, int trackId, int ordinal, String license, String artwork_url) {
        this.title = title;
        this.streaming_url = url;
        this.publisher_url = userpage;
        this.publisher_name = publisher_name;
        this.streamable = streamable;
        this.source_url = source_url;
        this.description = description;
        this.trackId = trackId;
        this.ordinal = ordinal;
        this.license_info = license;
        this.artwork_url = artwork_url;
    }

    public SearchedTrack(String title, String url, String userpage, String source_url, boolean streamable,
            String description, int trackId, int ordinal, String license, String album_name, String artist_name) {
        this.title = title;
        this.streaming_url = url;
        this.publisher_url = userpage;
        this.streamable = streamable;
        this.source_url = source_url;
        this.description = description;
        this.trackId = trackId;
        this.ordinal = ordinal;
        this.license_info = license;
        this.album_name = album_name;
        this.artist_name = artist_name;
    }

    public SearchedTrack(String title, String url, String userpage, String source_url, boolean streamable,
            String description, int trackId, int ordinal, String license, String album_name,
            String artist_name, String artwork_url) {
        this.title = title;
        this.streaming_url = url;
        this.publisher_url = userpage;
        this.streamable = streamable;
        this.source_url = source_url;
        this.description = description;
        this.trackId = trackId;
        this.ordinal = ordinal;
        this.license_info = license;
        this.album_name = album_name;
        this.artist_name = artist_name;
        this.artwork_url = artwork_url;
    }

}
