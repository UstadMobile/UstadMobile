/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.core.opds;

import com.ustadmobile.core.opf.UstadJSOPF;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Vector;

/**
 * Represents an OPDSItem as defined by an entry tag in an OPDS feed
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
    
    /**
     * Constructor that will copy the given srcItem.  The vectors used to store 
     * links, authors etc. will be new vectors but their content will be 
     * references to the same items.
     * 
     * All other properties will be copied by reference.
     * 
     * @param parentFeed
     * @param srcItem 
     */
    public UstadJSOPDSEntry(UstadJSOPDSFeed parentFeed, UstadJSOPDSEntry srcItem) {
        this(parentFeed);
        
        setEntryFromSrcItem(srcItem);
        
        this.linkVector = new Vector();
        for(int i = 0; i < srcItem.linkVector.size(); i++) {
            this.linkVector.addElement(srcItem.linkVector.elementAt(i));
        }
    }
    
    /**
     * Create a new entry for a given OPF
     * 
     * @param parentFeed
     * @param opf 
     */
    public UstadJSOPDSEntry(UstadJSOPDSFeed parentFeed, UstadJSOPF opf, String mimeType, String containerHREF) {
        this(parentFeed);
        this.linkVector = new Vector();
        this.title = opf.title;
        this.id = opf.id;
        
        this.addLink(UstadJSOPDSEntry.LINK_ACQUIRE, mimeType, containerHREF);
    }
    
    /**
     * Creates a new OPDSEntry with one link for the given parent feed
     * 
     * @param parentFeed The parent feed that this item is part going to be part of
     * @param title The title for this entry
     * @param id An ID for this entry
     * @param linkRel The link relation e.g. subsection or UstadJSOPDSEntry.LINK_ACQUIRE
     * @param linkMimeType The mime type for the link
     * @param linkHref The href for the link
     */
    public UstadJSOPDSEntry(UstadJSOPDSFeed parentFeed, String title, String id, String linkRel, String linkMimeType, String linkHref) {
        this(parentFeed);
        this.title = title;
        this.id = id;
        this.linkVector = new Vector();
        this.addLink(linkRel, linkMimeType, linkHref);
    }
    
    
    
    /**
     * Constructs an entry that can be added to another feed with one link with
     * the given parameters - useful when you want to link one feed to another
     * 
     * @param item
     * @param href
     * @param mimeType
     * @param rel
     * @return 
     */
    public static UstadJSOPDSEntry makeEntryForItem(UstadJSOPDSItem item, UstadJSOPDSFeed parentFeed, String rel, String mimeType, String href) {
        UstadJSOPDSEntry retVal = new UstadJSOPDSEntry(parentFeed);
        retVal.setEntryFromSrcItem(item);
        retVal.addLink(rel, mimeType, href);
        return retVal;
    }
    
    private void setEntryFromSrcItem(UstadJSOPDSItem srcItem) {
        this.title = srcItem.title;
        this.id = srcItem.id;
        this.updated = srcItem.updated;
        this.summary = srcItem.summary;
        this.authors = new Vector();
        if(srcItem.authors != null) {
            for(int i = 0; i < srcItem.authors.size(); i++) {
                this.authors.addElement(srcItem.authors.elementAt(i));
            }
        }
        
        this.publisher = srcItem.publisher;
    }
    
    

    public Vector getAcquisitionLinks() {
        return this.getLinks(LINK_ACQUIRE, null, true, false);
    } 
    
    public Vector getAcquisitionLinks(String mimeType){
        return this.getLinks(LINK_ACQUIRE, mimeType, true, false);
    }

    /**
     * Get the first acquisition link for this entry
     *
     * @param mimeType Required mime type. Can be null to indicate any mime type
     * @return String[] array of link attributes or null if there is no matching acquisition link
     */
    public String[] getFirstAcquisitionLink(String mimeType) {
        return this.getFirstLink(LINK_ACQUIRE, mimeType, true, false);
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


    /**
     * Serialize this entry to it's own XML document. When this is done the entry tag will be the
     * root element of the XML document.
     *
     * @param xs XmlSerializer to use
     * @throws IOException
     */
    public void serialize(XmlSerializer xs) throws IOException{
        serializeStartDoc(xs);
        serializeEntryTag(xs);
        xs.endDocument();
    }

    /**
     * Serialize this entry as a tag the given XmlSerializer. This will *NOT* start and end the document.
     * It is used both by the serialize method and UstadJSOPDSFeed,
     *
     * @param xs
     * @throws IOException
     */
    protected void serializeEntryTag(XmlSerializer xs) throws IOException{
        xs.startTag(UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_ENTRY]);
        serializeAttrs(xs);
        xs.endTag(UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_ENTRY]);
    }

}
