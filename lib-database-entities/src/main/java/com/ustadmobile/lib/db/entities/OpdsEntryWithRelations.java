package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmRelation;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 1/13/18.
 */

public class OpdsEntryWithRelations extends OpdsEntry{

    @UmRelation(parentColumn = "uuid", entityColumn = "entryUuid")
    private List<OpdsLink> links;

    @UmRelation(parentColumn = "entryId", entityColumn = "containerEntryId")
    private List<ContainerFileEntry> containerFileEntries;

    public List<OpdsLink> getLinks() {
        return links;
    }

    public void setLinks(List<OpdsLink> links) {
        this.links = links;
    }

    public List<ContainerFileEntry> getContainerFileEntries() {
        return containerFileEntries;
    }

    public OpdsEntryWithRelations(String uuid, String entryId, String title) {
        super(uuid, entryId, title);
    }

    public OpdsEntryWithRelations() {
    }

    public void setContainerFileEntries(List<ContainerFileEntry> containerFileEntries) {
        this.containerFileEntries = containerFileEntries;
    }

    /**
     * Serialize this as an atom entry tag
     *
     * @param xs XmlSerializer to serialize to
     *
     * @throws IOException
     */
    public void serializeEntryTag(XmlSerializer xs) throws IOException{
        xs.startTag(NS_ATOM, TAG_ENTRY);
        serializeAttrs(xs);
        xs.endTag(NS_ATOM, TAG_ENTRY);
    }

    /**
     * Serialize as an OPDS feed.
     *
     * @param xs XmlSerializer to serialize through
     * @param childEntries List of entries that will be child entries in the feed
     * @throws IOException
     */
    public void serializeFeed(XmlSerializer xs, List<OpdsEntryWithRelations> childEntries)
            throws IOException{
        serializeStartDocument(xs);
        xs.startTag(NS_ATOM, TAG_FEED);
        serializeAttrs(xs);
        for(OpdsEntryWithRelations entry : childEntries) {
            entry.serializeEntryTag(xs);
        }
        xs.endTag(NS_ATOM, TAG_FEED);
        xs.endDocument();
    }

    /**
     * Serialize the attributes of this item. This will generate the idm title, summary tags etc.
     * e.g. &lt;id&gt;item-id&lt;/id&gt;
     *
     * @param xs
     * @throws IOException
     */
    public void serializeAttrs(XmlSerializer xs) throws IOException {
        serializeStringToTag(xs, OpdsEntry.NS_ATOM, ATTR_TITLE, title);
        serializeStringToTag(xs, NS_ATOM, TAG_ID, getEntryId());
        serializeStringToTag(xs, NS_ATOM, ATTR_SUMMARY, summary);


        if (content != null) {
            if (getContentType() == null || getContentType().equals(CONTENT_TYPE_TEXT)) {
                xs.startTag(NS_ATOM, TAG_CONTENT);
                if (getContentType() != null)
                    xs.attribute(null, ATTR_TYPE, getContentType());

                xs.text(content);
                xs.endTag(NS_ATOM, TAG_CONTENT);
            } else {
//                TODO: Handle HTML content
//                try {
//                    XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
//                    parser.setInput(new ByteArrayInputStream(content.getBytes("UTF-8")), "UTF-8");
//                    UMUtil.passXmlThrough(parser, xs);
//                } catch (XmlPullParserException e) {
//                    e.printStackTrace();
//                }
            }
        }

        serializeStringToTag(xs, NS_ATOM, TAG_UPDATED, updated);
        serializeStringToTag(xs, NS_DC, TAG_PUBLISHER, publisher);
        serializeStringToTag(xs, NS_DC, TAG_LANGUAGE, language);

//        TODO: Serialize authors (not supported yet)
//        if (authors != null) {
//            UstadJSOPDSAuthor author;
//            for (int i = 0; i < authors.size(); i++) {
//                author = getAuthor(i);
//                xs.startTag(UstadJSOPDSFeed.NS_ATOM, "author");
//                if (author.getName() != null) {
//                    serializeStringToTag(xs, UstadJSOPDSFeed.NS_ATOM, "name",
//                            author.getName());
//                }
//
//                if (author.getUri() != null) {
//                    serializeStringToTag(xs, UstadJSOPDSFeed.NS_ATOM, "uri",
//                            author.getUri());
//                }
//
//                xs.endTag(UstadJSOPDSFeed.NS_ATOM, "author");
//            }
//        }

        if(getLinks() != null) {
            for(OpdsLink link : getLinks()) {
                xs.startTag(NS_ATOM, TAG_LINK);
                if (link.getRel() != null)
                    xs.attribute(null, ATTR_REL, link.getRel());
                if (link.getHref() != null)
                    xs.attribute(null, ATTR_HREF, link.getHref());
                if (link.getMimeType() != null)
                    xs.attribute(null, ATTR_TYPE, link.getMimeType());
                if (link.getHreflang() != null)
                    xs.attribute(null, ATTR_HREFLANG, link.getHreflang());
                if (link.getLength() >= 0)
                    xs.attribute(null, ATTR_LENGTH, String.valueOf(link.getLength()));
                if (link.getTitle() != null)
                    xs.attribute(null, ATTR_TITLE, link.getTitle());

                xs.endTag(NS_ATOM, TAG_LINK);
            }
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

    protected void serializeStartDocument(XmlSerializer xs) throws IOException{
        xs.startDocument("UTF-8", Boolean.FALSE);
        xs.setPrefix("", NS_ATOM);
        xs.setPrefix("dc", NS_DC);
        xs.setPrefix("opds", NS_OPDS);
    }

    public static List<OpdsEntry> toOpdsEntryList(List<OpdsEntryWithRelations> entryWithRelList) {
        ArrayList<OpdsEntry> entryList = new ArrayList<>(entryWithRelList);

        return entryList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpdsEntryWithRelations)) return false;
        if (!super.equals(o)) return false;

        OpdsEntryWithRelations that = (OpdsEntryWithRelations) o;

        if (links != null ? !links.equals(that.links) : that.links != null) return false;
        return containerFileEntries != null ? containerFileEntries.equals(that.containerFileEntries) : that.containerFileEntries == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (links != null ? links.hashCode() : 0);
        result = 31 * result + (containerFileEntries != null ? containerFileEntries.hashCode() : 0);
        return result;
    }
}
