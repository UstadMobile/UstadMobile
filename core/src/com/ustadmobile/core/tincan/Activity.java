/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.tincan;

import java.util.Hashtable;

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
    
    public Activity(String id, String aType) {
        this.id = id;
        this.aType = aType;
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
    
}
