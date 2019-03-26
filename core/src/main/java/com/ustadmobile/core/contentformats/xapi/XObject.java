package com.ustadmobile.core.contentformats.xapi;

public class XObject {

    private String id;

    private Definition definition;

    private String objectType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
}
