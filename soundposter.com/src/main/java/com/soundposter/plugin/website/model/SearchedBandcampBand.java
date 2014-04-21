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
public class SearchedBandcampBand {

    public String name = "", url = "", subdomain = "", offsite_url = "";
    public String band_id = "";

    public SearchedBandcampBand(long band_id, String title, String url, String subdomain, String offsite_url) {
        this.band_id = "" + band_id;
        this.name = title;
        this.url = url;
        this.subdomain = subdomain;
        this.offsite_url = offsite_url;
    }

}
