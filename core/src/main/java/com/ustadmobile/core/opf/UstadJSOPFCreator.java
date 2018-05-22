package com.ustadmobile.core.opf;

/**
 * Created by mike on 12/12/17.
 */

public class UstadJSOPFCreator {

    private String creator;

    private String id;

    public UstadJSOPFCreator(String creator, String id) {
        this.creator = creator;
        this.id = id;
    }

    public UstadJSOPFCreator() {

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
