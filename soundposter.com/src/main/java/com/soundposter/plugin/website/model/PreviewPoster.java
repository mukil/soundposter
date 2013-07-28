/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soundposter.plugin.website.model;

/**
 *
 * @author malt
 */
public class PreviewPoster {

    public String title = "", url = "", username = "", background_css = "";

    public PreviewPoster(String title, String url, String username, String preview_style) {
        this.title = title;
        this.url = url;
        this.username = username;
        this.background_css = preview_style;
    }

}
