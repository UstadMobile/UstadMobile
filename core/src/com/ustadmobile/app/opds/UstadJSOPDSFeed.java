/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.opds;
import java.io.IOException;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author varuna
 */
public class UstadJSOPDSFeed extends UstadJSOPDSItem{
    
    public UstadJSOPDSEntry[] entries;
    
    public UstadJSOPDSFeed() {
        
    }
    
    public static UstadJSOPDSFeed loadFromXML(XmlPullParser xpp) throws 
            XmlPullParserException, IOException{
        UstadJSOPDSFeed resultFeed = new UstadJSOPDSFeed();
        
        int evtType = xpp.getEventType();
        
        UstadJSOPDSItem currentItem = resultFeed;
        Vector entryVector = new Vector();
        String rel;
        String mimeType;
        String href;
        //cache the content string of an entry in case we dont find summary
        String content = null;
        do {
            
            if(evtType == XmlPullParser.START_TAG) {
                if(xpp.getName().equals("entry")) {
                    currentItem = new UstadJSOPDSEntry(resultFeed);
                }
                
                if(xpp.getName().equals("title")) {                    
                    currentItem.title = xpp.nextText();
                }else if(xpp.getName().equals("id")) {
                    currentItem.id = xpp.nextText();
                }else if(xpp.getName().equals("link")){
                    rel = xpp.getAttributeValue(null, "rel");
                    mimeType = xpp.getAttributeValue(null, "type");
                    href = xpp.getAttributeValue(null, "href");
                    currentItem.addLink(rel, mimeType, href);
                }else if(xpp.getName().equals("updated")){
                    currentItem.updated = xpp.nextText();
                }else if(xpp.getName().equals("summary")) {
                    currentItem.summary = xpp.nextText();
                }else if(xpp.getName().equals("content")) {
                    content = xpp.nextText();
                }else if(xpp.getName().equals("dc:publisher")){ // Fix this
                    currentItem.publisher = xpp.nextText();
                }else if(xpp.getName().equals("dcterms:publisher")){
                    currentItem.publisher = xpp.nextText();
                }
                if(xpp.getName().equals("author")){
                    
                    UstadJSOPDSAuthor currentAuthor = new UstadJSOPDSAuthor();
                    do
                    {
                        evtType = xpp.next();
                        
                        if(xpp.getName().equals("name")){
                            currentAuthor.name = xpp.nextText();
                        }else if (xpp.getName().equals("uri")){
                            currentAuthor.uri = xpp.nextText();
                        }
                        if(evtType == XmlPullParser.END_TAG
                                && xpp.getName().equals("author")){
                            if (currentItem.authors == null){
                                currentItem.authors = new Vector();
                                currentItem.authors.addElement(currentAuthor);
                            }else{
                                currentItem.authors.addElement(currentAuthor);
                            }
                        }
                        
                    }while(evtType != XmlPullParser.END_TAG && 
                            !xpp.getName().equals("author"));
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
        
        return resultFeed;
    }
    
    public UstadJSOPDSEntry getEntryById (String id) {
        UstadJSOPDSEntry entry;
        for (int i=0;i<=this.entries.length; i++){
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
        for (int i=0; i<=this.entries.length; i++){
            Vector entryResult = this.entries[i].getLinks(linkRel, linkType, 
                    relByPrefix, mimeTypeByPrefix);
            for(int j = 0; j < entryResult.size(); j++) {
                matches.addElement(entryResult.elementAt(j));
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
    

}
