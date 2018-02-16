package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;


/**
 * Created by mike on 1/13/18.
 */
@UmEntity
public class OpdsLink {

    @UmPrimaryKey(autoIncrement = true)
    private Integer id;

    @UmIndexField
    private String entryUuid;

    private int linkIndex;

    private String mimeType;

    private String href;

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
}
