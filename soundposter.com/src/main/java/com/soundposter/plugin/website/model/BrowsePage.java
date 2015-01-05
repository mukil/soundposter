
package com.soundposter.plugin.website.model;

import de.deepamehta.core.JSONEnabled;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class BrowsePage implements JSONEnabled {

    boolean on_first_page = false;
    boolean on_last_page = false;

    int page_id = -1;
    int next_page_id = -1;
    int prev_page_id = -1;
    
    // int max_count = -1;
    // int from = -1;
    // int to = -1;
    
    int overall_pages = -1; // is calculated ~ nr. of soundposters / max_count
    int overall_soundposter = -1;
    
    ArrayList<SoundPosterPreview> set = null;
    
    public BrowsePage(boolean firstPage, boolean lastPage, int currentPage, int nextPage, 
            int prevPage, ArrayList<SoundPosterPreview> poster) {
        this.page_id = currentPage;
        this.on_first_page = firstPage;
        this.on_last_page = lastPage;
        this.next_page_id = nextPage;
        this.prev_page_id = prevPage;
        this.set = poster;
    }
    
    public BrowsePage() {
        
    }
    
    public boolean isOn_first_page() {
        return on_first_page;
    }

    public void setOn_first_page(boolean on_first_page) {
        this.on_first_page = on_first_page;
    }

    public boolean isOn_last_page() {
        return on_last_page;
    }

    public void setOn_last_page(boolean on_last_page) {
        this.on_last_page = on_last_page;
    }
    
    
    public int getPage_id() {
        return page_id;
    }

    public void setPage_id(int page_id) {
        this.page_id = page_id;
    }

    public int getNext_page_id() {
        return next_page_id;
    }

    public void setNext_page_id(int next_page) {
        this.next_page_id = next_page;
    }

    public int getPrev_page_id() {
        return prev_page_id;
    }

    public void setPrev_page_id(int prev_page) {
        this.prev_page_id = prev_page;
    }

    /** public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    } **/

    public int getOverall_pages() {
        return overall_pages;
    }

    public void setOverall_pages(int overall_pages) {
        this.overall_pages = overall_pages;
    }

    public int getOverall_soundposter() {
        return overall_soundposter;
    }

    public void setOverall_soundposter(int overall_soundposter) {
        this.overall_soundposter = overall_soundposter;
    }

    public ArrayList<SoundPosterPreview> getSet() {
        return set;
    }

    public void setSet(ArrayList<SoundPosterPreview> set) {
        this.set = set;
    }

    public JSONObject toJSON() {
        try {
            JSONObject object = new JSONObject();
            object.put("page_id", this.page_id);
            object.put("next_page_id", this.next_page_id);
            object.put("prev_page_id", this.prev_page_id);
            object.put("on_first_page", this.on_first_page);
            object.put("on_last_page", this.on_last_page);
            object.put("page_count", this.overall_pages);
            object.put("soundposter_count", this.overall_soundposter);
            object.put("set", this.set);
            return object;
        } catch (JSONException ex) {
            Logger.getLogger(BrowsePage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    
}
