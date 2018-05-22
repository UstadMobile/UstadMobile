package com.ustadmobile.port.jcommon.opds.db.entities;

import com.ustadmobile.core.opds.entities.UmOpdsLink;

/**
 * Created by mike on 1/3/18.
 */

public class UmOpdsLinkEntity implements UmOpdsLink{

    private String href;

    private String hrefLang;

    private String rel;

    private String mimeType;

    private String title;

    private long length;

    private int id;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getHref() {
        return href;
    }

    @Override
    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public String getHrefLang() {
        return hrefLang;
    }

    @Override
    public void setHrefLang(String hrefLang) {
        this.hrefLang = hrefLang;
    }

    @Override
    public String getRel() {
        return rel;
    }

    @Override
    public void setRel(String rel) {
        this.rel = rel;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public void setLength(long length) {
        this.length = length;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
}
