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
    
    public static final int ATTR_REL = 0;
    public static final int ATTR_MIMETYPE = 1;
    public static final int ATTR_HREF = 2;
    public static final int ATTR_LENGTH = 3;
    public static final int ATTR_TITLE = 4;
    public static final int ATTR_HREFLANG = 5;
    public static final int ATTR_ID = 6;
    public static final int ATTR_SUMMARY = 7;
    public static final int ATTR_PUBLISHER = 8;
    public static final int ATTR_UPDATED = 9;
    public static final int ATTR_LINK = 10;
    public static final int ATTR_ENTRY = 11;
    
    public static final String[] ATTR_NAMES = {
        "rel", //ATTR_REL 
        "type", // ATTR_MIMETYPE
        "href", //ATTR_HREF
        "length", //ATTR_LENGTH
        "title",  //ATTR_TITLE
        "hreflang", //AtTR_HREFLANG
        "id", //ATTR_ID
        "summary",//ATTR_SUMMARY
        "publisher", //ATTR_PUBLISHER
        "updated", //ATTR_UPDATED
        "link", //ATTR_LINK
        "entry"
    };
    
    /**
     * ATTR_NAMES from 0 to LINK_ATTRS_END are those that are stored in the 
     * String array returned by UstadJSOPDSFeed to represent a link
     */
    public static final int LINK_ATTRS_END = 6;

    /**
     * Mapping of attribute names= to constants above
     */
    //protected static final String[] LINK_ATTR_NAMES = new String[]{ "rel", "type",
    //    "href", "length", "title", "hreflang"};
    
        
    public String updated;
    
    public String summary;
    public Vector authors;    
    public String publisher;
    
    public String bgColor;
    
    public String textColor;
    
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
     * @see UstadJSOPDSItem#ATTR_REL
     * @see UstadJSOPDSItem#ATTR_MIMETYPE
     * @see UstadJSOPDSItem#ATTR_HREF
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
            if(linkRel != null && thisLink[ATTR_REL] != null) {
                matchRel = relByPrefix ? 
                    thisLink[ATTR_REL].startsWith(linkRel) :
                        thisLink[ATTR_REL].equals(linkRel);
            }else if(linkRel != null && thisLink[ATTR_REL] == null) {
                matchRel = false;
            }
            
            if(mimeType != null && thisLink[ATTR_MIMETYPE] != null) {
                matchType = mimeTypeByPrefix ? 
                        thisLink[ATTR_MIMETYPE].startsWith(mimeType) :
                    thisLink[ATTR_MIMETYPE].equals(mimeType);
            }else if(mimeType != null && thisLink[ATTR_MIMETYPE] == null) {
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
        serializeStringToTag(xs, UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_TITLE], title);
        serializeStringToTag(xs, UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_ID], id);
        serializeStringToTag(xs, UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_SUMMARY], summary);
        serializeStringToTag(xs, UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_UPDATED], updated);
        serializeStringToTag(xs, UstadJSOPDSFeed.NS_DC, ATTR_NAMES[ATTR_PUBLISHER], publisher);
        
        for(int i = 0; i < linkVector.size(); i++) {
            String[] thisLink = (String[])linkVector.elementAt(i);
            xs.startTag(UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_LINK]);
            xs.attribute(null, ATTR_NAMES[ATTR_HREF], thisLink[ATTR_HREF]);
            if(thisLink[ATTR_REL] != null) {
                xs.attribute(null, ATTR_NAMES[ATTR_REL], thisLink[ATTR_REL]);
            }
            if(thisLink[ATTR_MIMETYPE] != null) {
                xs.attribute(null, ATTR_NAMES[ATTR_MIMETYPE], thisLink[ATTR_MIMETYPE]);
            }
            xs.endTag(UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_LINK]);
        }
    }
    
    
    /**
     * Shorthand to write a simple tag in the form of <ns:tagname>value</ns:tagname>
     * to a XmlSerializer
     * 
     * @param xs
     * @param ns
     * @param tagName
     * @param tagValue
     * @throws IOException 
     */
    private void serializeStringToTag(XmlSerializer xs, String ns, String tagName, String tagValue) throws IOException {
        if(tagValue != null) {
            xs.startTag(ns, tagName).text(tagValue).endTag(ns, tagName);
        }
    }
    
    public void serializeEntry(XmlSerializer xs) throws IOException{
        xs.startTag(UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_ENTRY]);
        serializeAttrs(xs);
        xs.endTag(UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_ENTRY]);
    }

}
