package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndex;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * The OpdsEntry can represent one of three types of content entries:
 *
 * 1. An Entry that represents an entry in a container file (e.g. EPUB, SCORM, Xapi Package) file etc.
 *  Depending on the type of container, one container file can contain more than one entry. In this
 *  case there will be a corresponding ContainerFileEntry where the ContainerFileEntry.opdsEntryUuid =
 *  OpdsEntry.uuid. ContainerFileEntry is a separate entity to make it easy to have a relationship field
 *  on each OpdsEntry to determine if the given entry loaded from a catalog or sync'd entity is
 *  present in a ContainerFile.
 *
 *
 * 2. An OpdsFeed: The feed itself is an entry, and is joined it's child entries using an
 *    OpdsEntryParentToChildJoin,
 *
 * 3. An Entry that is part of the main entity sync system between the client and the server.
 *
 * Created by mike on 1/13/18.
 */
@UmEntity
public class OpdsEntry {


    /**
     * A UUID that is distinct from the entryId which can be found in OPDS feeds, EPUB files, etc.
     * The same entry id might be used on two different servers, whilst having different info. These
     * could be seen as mirrors of each other, but we need a separate database entry.
     */
    @UmPrimaryKey
    private String uuid;

    /**
     * The entryId as defined by the id element in an OPDS feed
     */
    @UmIndexField
    private String entryId;


    /**
     * The uuid element as per the XML from the entry/feed
     */
//    protected String itemId;

    protected String title;

//    TODO: re-enable this index
//    @UmIndexField
    protected String updated;

    protected String summary;

    protected String content;

    protected String contentType;

    protected String publisher;

    protected String language;

    protected String url;

    private int entryType;

    public static final int ENTRY_TYPE_OPDS_FEED = 1;

    public static final int ENTRY_TYPE_OPDS_FEED_ENTRY = 2;

    public static final int ENTRY_TYPE_OPDS_ENTRY_STANDALONE = 3;

    protected static final String ATTR_REL = "rel";

    protected static final String ATTR_TYPE = "type";

    protected static final String ATTR_HREF = "href";

    protected static final String ATTR_LENGTH = "length";

    protected static final String ATTR_TITLE = "title";

    protected static final String ATTR_HREFLANG = "hreflang";



    protected static final String ATTR_SUMMARY = "summary";

    protected static final String ATTR_PUBLISHER = "publisher";


    public static final String TAG_ENTRY = "entry";

    public static final String TAG_FEED = "feed";

    protected static final String TAG_CONTENT = "content";

    protected static final String TAG_UPDATED = "updated";

    protected static final String TAG_ID = "id";

    protected static final String TAG_LINK = "link";

    protected static final String TAG_PUBLISHER = "publisher";

    protected static final String TAG_LANGUAGE = "language";

    /**
     * Entry content type - text
     */
    public static final String CONTENT_TYPE_TEXT = "text";

    /**
     * Entry content type - html
     */
    public static final String CONTENT_TYPE_XHTML = "xhtml";

    /**
     * OPDS constant for the cover image / artwork for an item
     * @type Strnig
     */
    public static final String LINK_IMAGE = "http://opds-spec.org/image";

    /**
     * OPDS constnat for the thumbnail
     * @type String
     */
    public static final String LINK_REL_THUMBNAIL = "http://opds-spec.org/image/thumbnail";

    /**
     * OPDS constant for the standard acquisition link:
     *
     * http://opds-spec.org/acquisition
     *
     * @type String
     */
    public static final String LINK_REL_ACQUIRE = "http://opds-spec.org/acquisition";

    public static final String LINK_REL_P2P_SELF = "http://www.ustadmobile.com/ns/opds/p2p-self";

    public static final String LINK_REL_SUBSECTION = "subsection";

    /**
     * Type to be used to represent an OPDS entry as per the opds spec
     *
     * @type String
     */
    public static final String TYPE_ENTRY_OPDS =
            "application/atom+xml;type=entry;profile=opds-catalog";

    public static final String TYPE_OPDS_ACQUISITION_FEED = "application/atom+xml;profile=opds-catalog;kind=acquisition";

    public static final String TYPE_OPDS_NAVIGATION_FEED = "application/atom+xml;profile=opds-catalog;kind=navigation";

    public static final String ENTRY_PROTOCOL = "entry:///";

    public static final String NS_ATOM = "http://www.w3.org/2005/Atom";

    public static final String NS_DC = "http://purl.org/dc/terms/";

    public static final String NS_OPDS = "http://opds-spec.org/2010/catalog";


    public interface OpdsItemLoadCallback {

        void onDone(OpdsEntry item);

        void onEntryAdded(OpdsEntryWithRelations childEntry, OpdsEntry parentFeed, int position);

        void onLinkAdded(OpdsLink link, OpdsEntry parentItem, int position);

        void onError(OpdsEntry item, Throwable cause);

    }

    public OpdsEntry(String uuid, String entryId, String title) {
        this.uuid = uuid;
        this.entryId = entryId;
        this.title = title;
    }

    public OpdsEntry() {

    }



    /**
     * A UUID that is distinct from the entryId which can be found in OPDS feeds, EPUB files, etc.
     * The same entry id might be used on two different servers, whilst having different info. These
     * could be seen as mirrors of each other, but we need a separate database entry.
     *
     * @return The UUDI for this feed (the primary key)
     */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     *
     * @return
     */
    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getEntryType() {
        return entryType;
    }

    public void setEntryType(int entryType) {
        this.entryType = entryType;
    }

    public void load(XmlPullParser xpp, OpdsItemLoadCallback callback) throws IOException, XmlPullParserException {
        int evtType;
        String name;
        int i, entryCount = 0, linkCount = 0;

        OpdsLink link;
        String linkLength;

        while((evtType = xpp.next()) != XmlPullParser.END_DOCUMENT) {
            if(evtType == XmlPullParser.START_TAG) {
                name = xpp.getName();

                //TODO: Test this with standalone entry OPDS file
                if(getEntryType() == 0) {
                    if (name.equals(TAG_ENTRY))
                        setEntryType(ENTRY_TYPE_OPDS_ENTRY_STANDALONE);
                    else if (name.equals(TAG_FEED))
                        setEntryType(ENTRY_TYPE_OPDS_FEED);
                }


                if(name.equals(TAG_ENTRY)) {
                    OpdsEntryWithRelations newEntry = new OpdsEntryWithRelations();
                    newEntry.setEntryType(ENTRY_TYPE_OPDS_FEED_ENTRY);
                    newEntry.load(xpp, callback);
                    entryCount++;

                    if(callback != null) {
                        callback.onEntryAdded(newEntry, this, entryCount);
                    }
                }else if(name.equals(ATTR_TITLE) && xpp.next() == XmlPullParser.TEXT) {
                    this.title = xpp.getText();
                }else if(name.equals(TAG_ID) && xpp.next() == XmlPullParser.TEXT) {
                    this.entryId= xpp.getText();
                }else if(name.equals(TAG_LINK)){
                    link = new OpdsLink();
                    link.setHref(xpp.getAttributeValue(null, ATTR_HREF));
                    link.setRel(xpp.getAttributeValue(null, ATTR_REL));
                    link.setMimeType(xpp.getAttributeValue(null, ATTR_TYPE));
                    link.setHreflang(xpp.getAttributeValue(null, ATTR_HREFLANG));
                    linkLength = xpp.getAttributeValue(null, ATTR_LENGTH);
                    if(linkLength != null) {
                        try {
                            link.setLength(Long.parseLong(linkLength));
                        }catch(NumberFormatException n) {
                            n.printStackTrace();
                        }
                    }

                    link.setTitle(xpp.getAttributeValue(null, ATTR_TITLE));
                    link.setEntryUuid(this.getUuid());
                    link.setLinkIndex(linkCount);

                    if(this instanceof OpdsEntryWithRelations) {
                        OpdsEntryWithRelations itemWithLinks = (OpdsEntryWithRelations)this;
                        if(itemWithLinks.getLinks() == null)
                            itemWithLinks.setLinks(new ArrayList<>());

                        itemWithLinks.getLinks().add(link);
                    }

                    if(callback != null)
                        callback.onLinkAdded(link, this, linkCount);

                    linkCount++;
                }else if(name.equals(TAG_UPDATED) && xpp.next() == XmlPullParser.TEXT){
                    this.updated = xpp.getText();
                }else if(name.equals(ATTR_SUMMARY) && xpp.next() == XmlPullParser.TEXT) {
                    this.summary = xpp.getText();
                }else if(name.equals(TAG_CONTENT)) {
                    contentType = xpp.getAttributeValue(null, ATTR_TYPE);
                    if(contentType != null && contentType.equals(CONTENT_TYPE_XHTML)) {
//                        TODO: re-implement this
//                        this.content = UMUtil.passXmlThroughToString(xpp, TAG_CONTENT);
                    }else if(xpp.next() == XmlPullParser.TEXT){
                        this.content = xpp.getText();
                    }
                }else if(name.equals("dc:publisher") && xpp.next() == XmlPullParser.TEXT){
                    this.publisher = xpp.getText();
                }else if(name.equals("dcterms:publisher") && xpp.next() == XmlPullParser.TEXT){
                    this.publisher = xpp.getText();
                }else if(name.equals("dc:language") && xpp.next() == XmlPullParser.TEXT) {
                    this.language = xpp.getText();
                }else if(name.equals("author")){
//                    TODO: Implement handling authors
//                    UstadJSOPDSAuthor currentAuthor = new UstadJSOPDSAuthor();
//                    do {
//                        evtType = xpp.next();
//
//                        if(evtType == XmlPullParser.START_TAG) {
//                            if(xpp.getName().equals("name")){
//                                if(xpp.next() == XmlPullParser.TEXT) {
//                                    currentAuthor.name = xpp.getText();
//                                }
//                            }else if (xpp.getName().equals("uri")) {
//                                if(xpp.next() == XmlPullParser.TEXT) {
//                                    currentAuthor.uri = xpp.getText();
//                                }
//                            }
//                        }else if(evtType == XmlPullParser.END_TAG
//                                && xpp.getName().equals("author")){
//                            if (this.authors == null){
//                                this.authors = new Vector();
//                                this.authors.addElement(currentAuthor);
//                            }else{
//                                this.authors.addElement(currentAuthor);
//                            }
//                        }
//                    }while(!(evtType == XmlPullParser.END_TAG && xpp.getName().equals("author")));
                }
            }else if(evtType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals(TAG_ENTRY)) {
                    return;
                }
            }
        }

        if(callback != null)
            callback.onDone(this);
    }

    public OpdsLink getAcquisitionLink(String mimeType, boolean mimeTypeByPrefix) {
        List<OpdsLink> result = getLinks(LINK_REL_ACQUIRE, mimeType, null, true, mimeTypeByPrefix, false, 1);
        if(result != null && !result.isEmpty())
            return result.get(0);
        else
            return null;
    }

    public List<OpdsLink> getLinks(String linkRel, String mimeType,
                                   String href, boolean relByPrefix,
                                   boolean mimeTypeByPrefix, boolean hrefByPrefix,
                                   int limit) {
        if(!(this instanceof OpdsEntryWithRelations)) {
            return null;
        }

        OpdsEntryWithRelations thisWithRelations = (OpdsEntryWithRelations)this;
        if(thisWithRelations.getLinks() == null)
            return null;

        List<OpdsLink> result = new ArrayList<>();

        boolean matchRel, matchType, matchHref;
        int matches = 0;
        for(OpdsLink link : thisWithRelations.getLinks()){
            matchRel = true;
            matchType = true;
            matchHref = true;

            if(linkRel != null && link.getRel() != null) {
                matchRel = relByPrefix ?
                        link.getRel().startsWith(linkRel) :
                        link.getRel().equals(linkRel);
            }else if(linkRel != null && link.getRel() == null) {
                matchRel = false;
            }

            if(mimeType != null && link.getMimeType() != null) {
                matchType = mimeTypeByPrefix ?
                        link.getMimeType().startsWith(mimeType) :
                        link.getMimeType().equals(mimeType);
            }else if(mimeType != null && link.getMimeType() == null) {
                matchType = false;
            }

            if(href != null && link.getHref() != null) {
                matchHref = hrefByPrefix ?
                        link.getHref().startsWith(href)
                        : link.getHref().equals(href);
            }else if(href != null && link.getHref() == null){
                matchHref = false;
            }

            if(matchRel && matchType && matchHref) {
                result.add(link);
                matches++;
                if(limit > 0 && matches == limit)
                    return result;
            }
        }

        return result;
    }

    public OpdsLink getThumbnailLink(boolean imgFallback) {
        List<OpdsLink> links = getLinks(LINK_REL_THUMBNAIL, null, null,
                false, false, false, 1);
        OpdsLink link = null;
        if(!links.isEmpty()) {
            link = links.get(0);
        }else if(imgFallback) {
            links = getLinks(LINK_IMAGE, null, null, false,
                    false, false, 1);
            if(!links.isEmpty()) {
                link = links.get(0);
            }
        }

        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpdsEntry)) return false;

        OpdsEntry opdsEntry = (OpdsEntry) o;

        if (entryType != opdsEntry.entryType) return false;
        if (uuid != null ? !uuid.equals(opdsEntry.uuid) : opdsEntry.uuid != null) return false;
        if (entryId != null ? !entryId.equals(opdsEntry.entryId) : opdsEntry.entryId != null)
            return false;
        if (title != null ? !title.equals(opdsEntry.title) : opdsEntry.title != null) return false;
        if (updated != null ? !updated.equals(opdsEntry.updated) : opdsEntry.updated != null)
            return false;
        if (summary != null ? !summary.equals(opdsEntry.summary) : opdsEntry.summary != null)
            return false;
        if (content != null ? !content.equals(opdsEntry.content) : opdsEntry.content != null)
            return false;
        if (contentType != null ? !contentType.equals(opdsEntry.contentType) : opdsEntry.contentType != null)
            return false;
        if (publisher != null ? !publisher.equals(opdsEntry.publisher) : opdsEntry.publisher != null)
            return false;
        if (language != null ? !language.equals(opdsEntry.language) : opdsEntry.language != null)
            return false;
        return url != null ? url.equals(opdsEntry.url) : opdsEntry.url == null;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (entryId != null ? entryId.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (updated != null ? updated.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        result = 31 * result + (publisher != null ? publisher.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + entryType;
        return result;
    }
}