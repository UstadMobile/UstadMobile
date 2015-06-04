/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.opds;

import java.util.Vector;

/**
 *
 * @author varuna
 */
public class UstadJSOPDSEntry extends UstadJSOPDSItem {
    public UstadJSOPDSFeed parentFeed;
    
    public static int LINK_REL = 0;
    public static int LINK_MIMETYPE = 1;
    public static int LINK_HREF = 2;
    
                
    public UstadJSOPDSEntry(UstadJSOPDSFeed parentFeed) {
        super();
        this.parentFeed = parentFeed;
    }

    public Vector getAcquisitionLinks() {
        return this.getLinks(LINK_ACQUIRE, null, true, false);
    } 
    
    public Vector getAcquisitionLinks(String mimeType){
        return this.getLinks(LINK_ACQUIRE, mimeType, true, false);
    }
    
    public Vector getNavigationLinks(){
        return this.getLinks(null, TYPE_ATOMFEED, false, true);
    }
    
    public Vector getThumbnails(){
        Vector tentries = new Vector();
        tentries = this.getLinks(LINK_THUMBNAIL, null);
        if (tentries.size() > 0){
            return tentries;
        }
        Vector ientries = new Vector();
        ientries = this.getLinks(LINK_IMAGE, null);
        if (ientries.size() > 0){
            return ientries;
        }
        
        return null;
    }
    
    public Vector getImages(){
        Vector ientries = new Vector();
        ientries = this.getLinks(LINK_IMAGE, null);
        if (ientries.size() > 0){
            return ientries;
        }
        
        Vector tentries = new Vector();
        tentries = this.getLinks(LINK_THUMBNAIL, null);
        if (tentries.size() > 0){
            return tentries;
        }
        
        return null;
    }

}
