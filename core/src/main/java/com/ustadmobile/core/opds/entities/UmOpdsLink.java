package com.ustadmobile.core.opds.entities;

/**
 * Created by mike on 1/3/18.
 */

public interface UmOpdsLink {

    int getId();

    void setId(int id);

    String getHref();

    void setHref(String href);

    String getHrefLang();

    void setHrefLang(String hrefLang);

    String getRel();

    void setRel(String rel);

    String getMimeType();

    void setMimeType(String mimeType);

    long getLength();

    void setLength(long length);

    String getTitle();

    void setTitle(String title);

}
