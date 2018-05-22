/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.tincan;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
/* $endif$ */


/**
 *
 * @author mike
 */
public class Activity {
    
    String id;
    
    String desc;
    
    String name;
    
    String aType;
    
    String launchURL;
    
    private Hashtable extensions;
    
    String lang;
    
    public Activity(String id, String aType, String lang) {
        this.id = id;
        this.aType = aType;
        this.lang = lang;
    }
    
    public Activity(String id, String aType) {
        this(id, aType, "en");
    }
    
    /**
     * Sets an extension for this activity.
     * @param key
     * @param value 
     */
    public void setExtension(String key, String value) {
        if(extensions == null) {
            extensions = new Hashtable();
        }
        
        extensions.put(key, value);
    }
    
    public String getExtension(String key) {
        if(extensions == null) {
            return null;
        }else if(!extensions.containsKey(key)) {
            return null;
        }else {
            return extensions.get(key).toString();
        }
    }
    
    /**
     * Gets the id of the activity
     * 
     * @return ID of activity as per "id" attribute
     */
    public String getId() {
        return this.id;
    }
    
    /**
     * Gets the name of the activity as per it's child name tag 
     * 
     * @return Name of the activity
     */
    public String getName() {
        return this.name;
    }
    
    public String getDesc() {
        return this.desc;
    }
    
    /**
     * Returns a minimal xAPI statement which references the ID of this activity
     * @return 
     */
    public JSONObject getActivityJSON() {
        JSONObject activityDef = null;
        try {
            activityDef = new JSONObject();
            activityDef.put("id", this.id);
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 187,null, e);
        }
        
        return activityDef;
        
    }

    public String getLaunchUrl() {
        return launchURL;
    }
    
    
}
