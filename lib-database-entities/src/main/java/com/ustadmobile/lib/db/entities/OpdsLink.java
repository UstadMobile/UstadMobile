package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndex;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;


/**
 * Created by mike on 1/13/18.
 */
//TODO: re-enable this index (indices = {@UmIndex(name = "idx_uuid_linkIndex", unique = true, value = {"entryUuid", "linkIndex"})})
@UmEntity
public class OpdsLink {

    @UmPrimaryKey(autoIncrement = true)
    private Integer id;

    @UmIndexField
    private String entryUuid;

    private int linkIndex;

    private String mimeType;

    private String href;

    @UmIndexField
    private String rel;

    private long length;

    private String hreflang;

    private String title;

    public OpdsLink() {

    }

    public OpdsLink(String entryUuid, String mimeType, String href, String rel) {
        this.entryUuid = entryUuid;
        this.mimeType = mimeType;
        this.href = href;
        this.rel = rel;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEntryUuid() {
        return entryUuid;
    }

    public void setEntryUuid(String entryUuid) {
        this.entryUuid = entryUuid;
    }

    public int getLinkIndex() {
        return linkIndex;
    }

    public void setLinkIndex(int linkIndex) {
        this.linkIndex = linkIndex;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getHreflang() {
        return hreflang;
    }

    public void setHreflang(String hreflang) {
        this.hreflang = hreflang;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpdsLink)) return false;

        OpdsLink opdsLink = (OpdsLink) o;

        if (linkIndex != opdsLink.linkIndex) return false;
        if (length != opdsLink.length) return false;
        if (!id.equals(opdsLink.id)) return false;
        if (entryUuid != null ? !entryUuid.equals(opdsLink.entryUuid) : opdsLink.entryUuid != null)
            return false;
        if (mimeType != null ? !mimeType.equals(opdsLink.mimeType) : opdsLink.mimeType != null)
            return false;
        if (href != null ? !href.equals(opdsLink.href) : opdsLink.href != null) return false;
        if (rel != null ? !rel.equals(opdsLink.rel) : opdsLink.rel != null) return false;
        if (hreflang != null ? !hreflang.equals(opdsLink.hreflang) : opdsLink.hreflang != null)
            return false;
        return title != null ? title.equals(opdsLink.title) : opdsLink.title == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (entryUuid != null ? entryUuid.hashCode() : 0);
        result = 31 * result + linkIndex;
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (href != null ? href.hashCode() : 0);
        result = 31 * result + (rel != null ? rel.hashCode() : 0);
        result = 31 * result + (int) (length ^ (length >>> 32));
        result = 31 * result + (hreflang != null ? hreflang.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}
