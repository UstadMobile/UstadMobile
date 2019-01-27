package com.ustadmobile.core.contentformats.epub.opf;

/**
 * Created by mike on 12/12/17.
 */

public class OpfCreator {

    private String creator;

    private String id;

    public OpfCreator(String creator, String id) {
        this.creator = creator;
        this.id = id;
    }

    public OpfCreator() {

    }


    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString() {
        return creator != null ? creator : super.toString();
    }
}
