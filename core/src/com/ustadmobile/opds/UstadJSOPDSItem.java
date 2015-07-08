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
package com.ustadmobile.opds;

import java.util.Vector;

/**
 *
 * @author varuna
 */
public abstract class UstadJSOPDSItem {
    
    public String title;
    
    public String id;
    
    public Vector linkVector;
    
    public static int LINK_REL = 0;
    public static int LINK_MIMETYPE = 1;
    public static int LINK_HREF = 2;
    
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
    * OPDS constant for the standard acquisition link
    * @type String
    */
    public static String LINK_ACQUIRE = "http://opds-spec.org/acquisition";

    /**
    * OPDS constant for open access acquisition link
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
        String[] s = new String[]{rel, mimeType, href};
        linkVector.addElement(s);        
    }
    
    public String[] getLink(int index) {
        return (String[])linkVector.elementAt(index);
    }
     
    public Vector getLinks(String linkRel, String mimeType) {
        return this.getLinks(linkRel, mimeType, false, false);
    }
    
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
    
    public Vector getNavigationLinks(){
        return this.getLinks(null, TYPE_ATOMFEED, false, true);
    }

}
