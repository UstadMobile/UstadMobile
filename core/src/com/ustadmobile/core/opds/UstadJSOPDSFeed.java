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
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 *
 * @author varuna
 */
public class UstadJSOPDSFeed extends UstadJSOPDSItem{
    
    public static final String NS_ATOM = "http://www.w3.org/2005/Atom";
    
    public static final String NS_DC = "http://purl.org/dc/terms/";
    
    public static final String NS_OPDS = "http://opds-spec.org/2010/catalog";
    
    
    public UstadJSOPDSEntry[] entries;
    
    
    
    /**
     * The absolute URL of this catalog (HTTP or Filesystem based)
     */
    public String href;
    
    public UstadJSOPDSFeed() {
        href = null;
    }
    
    /**
     * Full argument constructor used to create a new OPDS feed with mandatory
     * elements: 
     * 
     * @param srcHref The base HREF for relative links of this feed
     * @param title The OPDS Title
     * @param id The OPDS ID of the feed itself
     */
    public UstadJSOPDSFeed(String srcHref, String title, String id) {
        this.href = srcHref;
        this.entries = new UstadJSOPDSEntry[0];
        this.title = title;
        this.id = id;
    }
    
    public static UstadJSOPDSFeed loadFromXML(String str) throws XmlPullParserException, IOException {
        
        XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
        ByteArrayInputStream bin = new ByteArrayInputStream(str.getBytes("UTF-8"));
        parser.setInput(bin, "UTF-8");
        return loadFromXML(parser);
    }
    
    public static UstadJSOPDSFeed loadFromXML(XmlPullParser xpp) throws 
            XmlPullParserException, IOException{
        UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.VERBOSE,
            511, null);
        UstadJSOPDSFeed resultFeed = new UstadJSOPDSFeed();
        
        int evtType = xpp.getEventType();
        
        UstadJSOPDSItem currentItem = resultFeed;
        Vector entryVector = new Vector();
        
        String[] linkAttrs;
        String rel;
        String mimeType;
        String href;
        //cache the content string of an entry in case we dont find summary
        String content = null;
        String name;
        
        int i;
        do {
            name = null;
            if(evtType == XmlPullParser.START_TAG) {
                name = xpp.getName();
                
                if(name.equals("entry")) {
                    currentItem = new UstadJSOPDSEntry(resultFeed);
                }else if(name.equals("title") && xpp.next() == XmlPullParser.TEXT) {                    
                    currentItem.title = xpp.getText();
                }else if(name.equals("id") && xpp.next() == XmlPullParser.TEXT) {
                    currentItem.id = xpp.getText();
                }else if(name.equals("link")){
                    linkAttrs = new String[LINK_ATTR_NAMES.length];
                    for(i = 0; i < LINK_ATTR_NAMES.length; i++) {
                        linkAttrs[i] = xpp.getAttributeValue(null, 
                            LINK_ATTR_NAMES[i]);
                    }
                    currentItem.addLink(linkAttrs);
                }else if(name.equals("updated") && xpp.next() == XmlPullParser.TEXT){
                    currentItem.updated = xpp.getText();
                }else if(name.equals("summary") && xpp.next() == XmlPullParser.TEXT) {
                    currentItem.summary = xpp.getText();
                }else if(name.equals("content") && xpp.next() == XmlPullParser.TEXT) {
                    content = xpp.getText();
                }else if(name.equals("dc:publisher") && xpp.next() == XmlPullParser.TEXT){ // Fix this
                    currentItem.publisher = xpp.getText();
                }else if(name.equals("dcterms:publisher") && xpp.next() == XmlPullParser.TEXT){
                    currentItem.publisher = xpp.getText();
                }else if(name.equals("author")){
                    
                    UstadJSOPDSAuthor currentAuthor = new UstadJSOPDSAuthor();
                    do
                    {
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
                            if (currentItem.authors == null){
                                currentItem.authors = new Vector();
                                currentItem.authors.addElement(currentAuthor);
                            }else{
                                currentItem.authors.addElement(currentAuthor);
                            }
                        }
                    }while(!(evtType == XmlPullParser.END_TAG && xpp.getName().equals("author")));
                }
                
            }else if(evtType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("entry")) {
                    if(currentItem.summary == null && content != null) {
                        currentItem.summary = content;
                    }
                    
                    entryVector.addElement(currentItem);
                    currentItem = resultFeed;
                    content = null;
                }else if (xpp.getName().equals("author")){
                    //currentItem.author = currentAuthor;
                }
            }
            
            evtType = xpp.next();
        }while(evtType != XmlPullParser.END_DOCUMENT);
        
        resultFeed.entries = new UstadJSOPDSEntry[entryVector.size()];
        entryVector.copyInto(resultFeed.entries);
        
        UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.VERBOSE,
            512, null);
        
        return resultFeed;
    }
    
    /**
     * Add an entry to the end of this feed
     * 
     * @param entry  The entry to be added
     */
    public void addEntry(UstadJSOPDSEntry entry) {
        UstadJSOPDSEntry[] newEntries = new UstadJSOPDSEntry[entries.length + 1];
        System.arraycopy(this.entries, 0, newEntries, 0, this.entries.length);
        newEntries[entries.length] = entry;
        this.entries = newEntries;
    }
    
    /**
     * Sort entries use a given comparer
     * 
     * @param comparer 
     */
    public void sortEntries(UMUtil.Comparer comparer) {
        UMUtil.bubbleSort(entries, comparer);
    }
    
    
    public UstadJSOPDSEntry getEntryById (String id) {
        UstadJSOPDSEntry entry;
        for (int i = 0;i < this.entries.length; i++){
            if (this.entries[i].id.equals(id)){
                entry = this.entries[i];
                return entry;
            }
        }
        
        return null;
    }
    
    public UstadJSOPDSEntry[] getEntriesByLinkParams(String linkRel, 
        String linkType, boolean relByPrefix, boolean mimeTypeByPrefix){
        Vector matches = new Vector();
        for (int i=0; i< this.entries.length; i++){
            Vector entryResult = this.entries[i].getLinks(linkRel, linkType, 
                    relByPrefix, mimeTypeByPrefix);
            if(entryResult.size() > 0) {
                matches.addElement(this.entries[i]);
            }
        }
        
        UstadJSOPDSEntry[] matchEntries = new UstadJSOPDSEntry[matches.size()];
        matches.copyInto(matchEntries);
        return matchEntries;
    }
    
    public boolean isAcquisitionFeed(){
        Object[] entries = this.getEntriesByLinkParams(
                UstadJSOPDSEntry.LINK_ACQUIRE, null, true, false);
        return (entries.length > 0);
    }
    
    public void serialize(XmlSerializer xs) throws IOException {
        xs.startDocument("UTF-8", Boolean.FALSE);
        xs.setPrefix("", NS_ATOM);
        xs.setPrefix("dc", NS_DC);
        xs.setPrefix("opds", NS_OPDS);
        xs.startTag(NS_ATOM, "feed");
        serializeAttrs(xs);
        
        for(int i = 0; i < entries.length; i++) {
            entries[i].serializeEntry(xs);
        }
        xs.endTag(NS_ATOM, "feed");
        xs.endDocument();
    }
    
    /**
     * Serializes the feed as XML to the given output stream.  This will flush
     * and close the output stream.  Closing will happen even if there is
     * an exception when writing
     * 
     * @param out
     * @throws IOException 
     */
    public void serialize(OutputStream out) throws IOException {
        IOException ioe = null;
        XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
        try {
            serializer.setOutput(out, "UTF-8");
            serialize(serializer);
            out.flush();
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 170, title, e);
            ioe = e;
        }finally {
            UMIOUtils.closeOutputStream(out);
            UMIOUtils.throwIfNotNullIO(ioe);
        }
        
    }
    
    
}
