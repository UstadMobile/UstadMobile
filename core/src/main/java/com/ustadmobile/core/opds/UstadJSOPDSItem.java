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

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 * Abstract class that represents an OPDS Item - this can be a feed itself or an entry within a feed.
 *
 * Items can be serialized to Xml using the XmlSerializer class and the serialize method
 * Items can be loaded from Xml using the XmlPullParser and the method loadFromXpp
 *
 * @author varuna
 */
public abstract class UstadJSOPDSItem {
    
    public String title;
    
    public String id;
    
    protected Vector linkVector;

    protected String language;

    public static final String NS_ATOM = "http://www.w3.org/2005/Atom";

    public static final String NS_DC = "http://purl.org/dc/terms/";

    public static final String NS_OPDS = "http://opds-spec.org/2010/catalog";

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
    public static final int ATTR_CONTENT = 12;
    public static final int ATTR_TYPE = 13;
    
    public static final String[] ATTR_NAMES = {
        "rel", //ATTR_REL 
        "type", // ATTR_MIMETYPE
        "href", //ATTR_HREF
        "length", //ATTR_LENGTH
        "title",  //ATTR_TITLE
        "hreflang", //ATTR_HREFLANG
        "id", //ATTR_ID
        "summary",//ATTR_SUMMARY
        "publisher", //ATTR_PUBLISHER
        "updated", //ATTR_UPDATED
        "link", //ATTR_LINK
        "entry", //ATTR_ENTRY
        "content", // ATTR_CONTENT
        "type" //ATTR_TYPE
    };

    /**
     * Entry content type - text
     */
    public static final String CONTENT_TYPE_TEXT = "text";

    /**
     * Entry content type - html
     */
    public static final String CONTENT_TYPE_XHTML = "xhtml";

    /**
     * The type attribute of the content tag.
     */
    protected String contentType;

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

    public String content;

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

    public static final String LINK_REL_SELF = "self";

    /**
     * Sometimes we need to know where a feed came from in order to resolve a link contained in that
     * feed. The self link itself might be relative. The feed might be serialized along the way.
     * Therefor we need to embed an absolute link to the feed itself.
     *
     * We do this by adding a link with the rel attribute set
     */
    public static final String LINK_REL_SELF_ABSOLUTE = "http://www.ustadmobile.com/namespace/self-absolute";

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

    /**
     * The language of this item as specified by the dublin core dc:language tag if present
     *
     * @return Language specified by dc language tag, or null if not present
     */
    public String getLanguage() {
        return language;
    }

    /**
     * The language of this item as specified by the dublin core dc:language tag
     *
     * @param language The langauge for this item
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    public String[] addLink(String rel, String mimeType, String href) {
        String[] newLink = new String[]{rel, mimeType, href, null, null, null};
        linkVector.addElement(newLink);
        return newLink;
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
     * Returns the String of attributes for the first link matching the given criteria. Convenience
     * method that returns the first result that would be found by getLinks
     *
     * @param linkRel
     * @param mimeType
     * @param relByPrefix
     * @param mimeTypeByPrefix
     *
     * @return
     */
    public String[] getFirstLink(String linkRel, String mimeType, boolean relByPrefix, boolean mimeTypeByPrefix ){
        Vector result = getLinks(linkRel, mimeType, relByPrefix, mimeTypeByPrefix);
        if(result.size() == 0) {
            return null;
        }else {
            return (String[])result.elementAt(0);
        }
    }

    public String[] getFirstLink(String linkRel, String mimeType) {
        return getFirstLink(linkRel, mimeType, false, false);
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
     * Serialize this item to an XmlSerializer. This will create a new Xml Document, set the name
     * spaces and then create a root item.
     *
     * @param xs XmlSerializer to use
     * @throws IOException If an IOException is thrown by the underlying output stream
     */
    public abstract void serialize(XmlSerializer xs) throws IOException;

    /**
     * Serialize the attributes of this item. This will generate the idm title, summary tags etc.
     * e.g. &lt;id&gt;item-id&lt;/id&gt;
     *
     * @param xs
     * @throws IOException 
     */
    public void serializeAttrs(XmlSerializer xs) throws IOException{
        serializeStringToTag(xs, UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_TITLE], title);
        serializeStringToTag(xs, UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_ID], id);
        serializeStringToTag(xs, UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_SUMMARY], summary);


        serializeStringToTag(xs, UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_CONTENT], content);
        if(content != null) {
            if(getContentType().equals(CONTENT_TYPE_TEXT)) {
                xs.startTag(UstadJSOPDSFeed.NS_ATOM, ATTR_NAMES[ATTR_CONTENT])
                        .attribute(null, ATTR_NAMES[ATTR_TYPE], getContentType());
                xs.text(content);
                xs.endTag(NS_ATOM, ATTR_NAMES[ATTR_CONTENT]);
            }else {
                try {
                    XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
                    parser.setInput(new ByteArrayInputStream(content.getBytes("UTF-8")), "UTF-8");
                    UMUtil.passXmlThrough(parser, xs, null);//TODO: check depth at which tag ends
                }catch(XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }

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



    protected void serializeStartDoc(XmlSerializer xs) throws IOException {
        xs.startDocument("UTF-8", Boolean.FALSE);
        xs.setPrefix("", NS_ATOM);
        xs.setPrefix("dc", NS_DC);
        xs.setPrefix("opds", NS_OPDS);
    }



    /**
     * Serialize this item to a String.
     *
     * @return
     */
    public String serializeToString()  {
        try {
            XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            serializer.setOutput(bout, "UTF-8");
            serialize(serializer);
            bout.flush();
            return new String(bout.toByteArray(), "UTF-8");
        }catch(IOException e) {
            //This should not happen as we're only using a byte array output stream
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Load values from An XmlPullParser into this OPDSItem. This method can be used both to parse a
     * single entry on it's own or it can be used to parse a feed of entries.
     *
     * @param xpp XmlPullParser being used to parse XML
     * @param parentFeed The OPDS Feed object to be set as the parent if we found a new entry
     * @throws XmlPullParserException If there's an XML parsing exception
     * @throws IOException If there's an underlying IO exception
     */
    public void loadFromXpp(XmlPullParser xpp, UstadJSOPDSFeed parentFeed) throws XmlPullParserException, IOException{
        int evtType;
        String name;
        String[] linkAttrs;
        int i;
        boolean isFeed = this instanceof UstadJSOPDSFeed;
        Vector entriesFound = isFeed? new Vector() : null;

        while((evtType = xpp.next()) != XmlPullParser.END_DOCUMENT) {
            if(evtType == XmlPullParser.START_TAG) {
                name = xpp.getName();
                if(isFeed && name.equals(ATTR_NAMES[ATTR_ENTRY])) {
                    UstadJSOPDSEntry newEntry = new UstadJSOPDSEntry(parentFeed);
                    newEntry.loadFromXpp(xpp, parentFeed);
                    entriesFound.addElement(newEntry);
                }else if(name.equals(ATTR_NAMES[ATTR_TITLE]) && xpp.next() == XmlPullParser.TEXT) {
                    this.title = xpp.getText();
                }else if(name.equals("id") && xpp.next() == XmlPullParser.TEXT) {
                    this.id = xpp.getText();
                }else if(name.equals("link")){
                    linkAttrs = new String[UstadJSOPDSItem.LINK_ATTRS_END];
                    for(i = 0; i < UstadJSOPDSItem.LINK_ATTRS_END; i++) {
                        linkAttrs[i] = xpp.getAttributeValue(null, ATTR_NAMES[i]);
                    }
                    this.addLink(linkAttrs);
                }else if(name.equals("updated") && xpp.next() == XmlPullParser.TEXT){
                    this.updated = xpp.getText();
                }else if(name.equals(ATTR_NAMES[ATTR_SUMMARY]) && xpp.next() == XmlPullParser.TEXT) {
                    this.summary = xpp.getText();
                }else if(name.equals("content")) {
                    contentType = xpp.getAttributeValue(null, ATTR_NAMES[ATTR_TYPE]);
                    if(contentType != null && contentType.equals(CONTENT_TYPE_XHTML)) {
                        this.content = UMUtil.passXmlThroughToString(xpp, ATTR_NAMES[ATTR_CONTENT]);
                        //int openContentTagStart = content.indexOf("<content");
                        //int openContentTagEnd = content.indexOf('>', openContentTagStart+1);
                        //int closeContentTagStart = content.lastIndexOf("</content");
                        //this.content = this.content.substring(openContentTagEnd+1, closeContentTagStart);
                    }else if(xpp.next() == XmlPullParser.TEXT){
                        this.content = xpp.getText();
                    }
                }else if(name.equals("dc:publisher") && xpp.next() == XmlPullParser.TEXT){ // Fix this
                    this.publisher = xpp.getText();
                }else if(name.equals("dcterms:publisher") && xpp.next() == XmlPullParser.TEXT){
                    this.publisher = xpp.getText();
                }else if(name.equals("dc:language") && xpp.next() == XmlPullParser.TEXT) {
                    this.language = xpp.getText();
                }else if(name.equals("author")){
                    UstadJSOPDSAuthor currentAuthor = new UstadJSOPDSAuthor();
                    do {
                        evtType = xpp.next();

                        if(evtType == XmlPullParser.START_TAG) {
                            if(xpp.getName().equals("name")){
                                if(xpp.next() == XmlPullParser.TEXT) {
                                    currentAuthor.name = xpp.getText();
                                }
                            }else if (xpp.getName().equals("uri")) {
                                if(xpp.next() == XmlPullParser.TEXT) {
                                    currentAuthor.uri = xpp.getText();
                                }
                            }
                        }else if(evtType == XmlPullParser.END_TAG
                                && xpp.getName().equals("author")){
                            if (this.authors == null){
                                this.authors = new Vector();
                                this.authors.addElement(currentAuthor);
                            }else{
                                this.authors.addElement(currentAuthor);
                            }
                        }
                    }while(!(evtType == XmlPullParser.END_TAG && xpp.getName().equals("author")));
                }
            }else if(evtType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("entry")) {
                    if(this.summary == null && content != null) {
                        this.summary = content;
                    }
                    return;
                }
            }
        }

        if(isFeed) {
            UstadJSOPDSFeed feed = (UstadJSOPDSFeed)this;
            feed.entries = new UstadJSOPDSEntry[entriesFound.size()];
            entriesFound.copyInto(feed.entries);
        }
    }

    /**
     * Load the given item from an XmlPullParser.
     *
     * @param xpp XmlPullParser to read from
     * @throws XmlPullParserException
     * @throws IOException
     */
    public void loadFromXpp(XmlPullParser xpp) throws XmlPullParserException, IOException{
        loadFromXpp(xpp, null);
    }


    /**
     * Load the OPDS feed from a String.
     *
     * @param str
     * @throws XmlPullParserException
     * @throws IOException
     */
    public void loadFromString(String str) throws XmlPullParserException, IOException{
        XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
        ByteArrayInputStream bin = new ByteArrayInputStream(str.getBytes("UTF-8"));
        parser.setInput(bin, "UTF-8");
        loadFromXpp(parser);
    }

    /**
     * Find a list of the languages a link is available in. If no language is specified on hreflang
     * then we assume that link is in the language of the item it came from.
     *
     * @param links Vector of String[] arrays representing links
     * @param languageList (can be null) The vector into which we will add new languages found.
     *
     * @return Vector containing all distinct languages found
     */
    public Vector getHrefLanguagesFromLinks(Vector links, Vector languageList) {
        if(languageList == null)
            languageList = new Vector();

        int numLinks = links.size();
        String lang;
        for(int i = 0; i < numLinks; i++) {
            lang = ((String[])links.elementAt(i))[ATTR_HREFLANG];
            if(lang == null)
                lang = getLanguage();

            if(languageList.indexOf(lang) == -1)
                languageList.addElement(lang);
        }

        return languageList;
    }


    private int parseColorString(String colorStr) {
        if(bgColor == null) {
            return -1;
        }

        try {
            //This should be HTML/CSS style hex e.g. #000000
            return Integer.parseInt(colorStr.substring(1), 16);
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 84, colorStr, e);
        }

        return -2;
    }

    public String getContentType() {
        return contentType != null ? contentType: CONTENT_TYPE_TEXT;
    }

    public int getBgColor() {
        return parseColorString(bgColor);
    }

    public int getTextColor() {
        return parseColorString(textColor);
    }




}
