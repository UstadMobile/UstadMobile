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

import java.io.IOException;
import java.util.Vector;
import org.xmlpull.v1.XmlSerializer;

/**
 *
 * @author varuna
 */
public abstract class UstadJSOPDSItem {
    
    public String title;
    
    public String id;
    
    protected Vector linkVector;
    
    public static final int LINK_REL = 0;
    public static final int LINK_MIMETYPE = 1;
    public static final int LINK_HREF = 2;
    public static final int LINK_LENGTH = 3;
    public static final int LINK_TITLE = 4;
    public static final int LINK_HREFLANG = 5;
    
    /**
     * Mapping of attribute names to constants above
     */
    protected static final String[] LINK_ATTR_NAMES = new String[]{ "rel", "type",
        "href", "length", "title", "hreflang"};
    
    public String updated;
    
    public String summary;
    public Vector authors;    
    public String publisher;
    /**
    * Atom/XML feed mime type constant
    * 
    * @type String
    */
    public static String TYPE_ATOMFEED = "application/atom+xml";
    
    /**
    * OPDS constant for the standard acquisition link:
    * 
    * http://opds-spec.org/acquisition
    * 
    * @type String
    */
    public static String LINK_ACQUIRE = "http://opds-spec.org/acquisition";

    /**
    * OPDS constant for open access acquisition link
    * 
    * http://opds-spec.org/acquisition/open-access
    * 
    * @type String
    */
    public static String LINK_ACQUIRE_OPENACCESS = 
           "http://opds-spec.org/acquisition/open-access";

    /**
    * Type to be used for a catalog link of an acquisition feed as per OPDS spec
    * 
    * @type String
    */
    public static String TYPE_ACQUISITIONFEED = 
           "application/atom+xml;profile=opds-catalog;kind=acquisition";


    /**
    * Type to be used for a navigation feed as per OPDS spec
    * 
    * @type String
    */
    public static String TYPE_NAVIGATIONFEED =
          "application/atom+xml;profile=opds-catalog;kind=navigation";

    /**
    * The type of link used for an epub file itself
    * 
    * @type String
    */
    public static String TYPE_EPUBCONTAINER = "application/epub+zip";

   /**
    * OPDS constant for the cover image / artwork for an item
    * @type Strnig
    */
   public static String LINK_IMAGE = "http://opds-spec.org/image";

   /**
    * OPDS constnat for the thumbnail
    * @type String
    */
    public static String LINK_THUMBNAIL = "http://opds-spec.org/image/thumbnail";

    public UstadJSOPDSItem() {
        this.linkVector = new Vector();
    }
    
    public void addLink(String rel, String mimeType, String href) {
        linkVector.addElement(new String[]{rel, mimeType, href, null, null, null});
    }
    
    /**
     * Add a link to this item
     * 
     * @param linkVals Link values as per the LINK_* constants
     */
    public void addLink(String[] linkVals) {
        linkVector.addElement(linkVals);
    }
    
    public String[] getLink(int index) {
        return (String[])linkVector.elementAt(index);
    }
    
    /**
     * Get a vector containing all links for this entry as String arrays: indexed
     * as per LINK_ constants.
     * 
     * @see UstadJSOPDSItem#LINK_REL
     * @see UstadJSOPDSItem#LINK_MIMETYPE
     * @see UstadJSOPDSItem#LINK_HREF
     * 
     * @return Vector containing all links for this entry as String arrays
     */
    public Vector getLinks() {
        return this.linkVector;
    }
     
    public Vector getLinks(String linkRel, String mimeType) {
        return this.getLinks(linkRel, mimeType, false, false);
    }
    
    /**
     * Search through the links of this opds item
     * 
     * @param linkRel
     * @param mimeType
     * @param relByPrefix
     * @param mimeTypeByPrefix
     * @return 
     */
    public Vector getLinks(String linkRel, String mimeType, boolean relByPrefix,
            boolean mimeTypeByPrefix) {
        Vector result = new Vector();
        boolean matchRel = false;
        boolean matchType = false;
        
        for(int i = 0; i < linkVector.size(); i++) {
            matchRel = true;
            matchType = true;
            
            String[] thisLink = (String[])linkVector.elementAt(i);
            if(linkRel != null && thisLink[LINK_REL] != null) {
                matchRel = relByPrefix ? 
                    thisLink[LINK_REL].startsWith(linkRel) :
                        thisLink[LINK_REL].equals(linkRel);
            }else if(linkRel != null && thisLink[LINK_REL] == null) {
                matchRel = false;
            }
            
            if(mimeType != null && thisLink[LINK_MIMETYPE] != null) {
                matchType = mimeTypeByPrefix ? 
                        thisLink[LINK_MIMETYPE].startsWith(mimeType) :
                    thisLink[LINK_MIMETYPE].equals(mimeType);
            }else if(mimeType != null && thisLink[LINK_MIMETYPE] == null) {
                matchType = false;
            }
            
            if(matchRel && matchType) {
                result.addElement(thisLink);
            }
        }
        
        return result;
    }
    
    /**
     * Return the link String array for the thumbnail for this item
     * 
     * @param imgFallback If true use the IMAGE link for items that do not have a thumbnail (could be a bigger image)
     * 
     * @return String][ array of link items as per getLinks or null if not found on this item
     */
    public String[] getThumbnailLink(boolean imgFallback) {
        Vector results = getLinks(LINK_THUMBNAIL, null);
        if(results.size() > 0) {
            return ((String[])results.elementAt(0));
        }
        
        if(imgFallback) {
            results = getLinks(LINK_IMAGE, null);
            if(results.size() > 0) {
                return ((String[])results.elementAt(0));
            }
        }
        
        return null;
    }
    
    public Vector getNavigationLinks(){
        return this.getLinks(null, TYPE_ATOMFEED, false, true);
    }
    
    /**
     * 
     * @param xs
     * @throws IOException 
     */
    public void serializeAttrs(XmlSerializer xs) throws IOException{
        xs.startTag(UstadJSOPDSFeed.NS_ATOM, "title").text(title).endTag(
            UstadJSOPDSFeed.NS_ATOM, "title");
        
        xs.startTag(UstadJSOPDSFeed.NS_ATOM, "id").text(id).endTag(
            UstadJSOPDSFeed.NS_ATOM, "id");
        
        if(summary != null) {
            xs.startTag(UstadJSOPDSFeed.NS_ATOM, "summary").text(summary).endTag(
                UstadJSOPDSFeed.NS_ATOM, "summary");
        }
        
        if(updated != null) {
            xs.startTag(UstadJSOPDSFeed.NS_ATOM, "updated").text(updated).endTag(
                UstadJSOPDSFeed.NS_ATOM, "updated");
        }
        
        if(publisher != null) {
            xs.startTag(UstadJSOPDSFeed.NS_DC, "publisher").text(publisher).endTag(
                UstadJSOPDSFeed.NS_DC, "publisher");
        }
        
        for(int i = 0; i < linkVector.size(); i++) {
            String[] thisLink = (String[])linkVector.elementAt(i);
            xs.startTag(UstadJSOPDSFeed.NS_ATOM, "link");
            xs.attribute(null, "href", thisLink[LINK_HREF]);
            if(thisLink[LINK_REL] != null) {
                xs.attribute(null, "rel", thisLink[LINK_REL]);
            }
            if(thisLink[LINK_MIMETYPE] != null) {
                xs.attribute(null, "type", thisLink[LINK_MIMETYPE]);
            }
            xs.endTag(UstadJSOPDSFeed.NS_ATOM, "link");
        }
    }
    
    public void serializeEntry(XmlSerializer xs) throws IOException{
        xs.startTag(UstadJSOPDSFeed.NS_ATOM, "entry");
        serializeAttrs(xs);
        xs.endTag(UstadJSOPDSFeed.NS_ATOM, "entry");
    }

}
