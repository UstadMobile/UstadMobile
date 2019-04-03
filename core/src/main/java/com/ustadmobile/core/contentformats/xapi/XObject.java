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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XObject object = (XObject) o;

        if (id != null ? !id.equals(object.id) : object.id != null) return false;
        return objectType != null ? objectType.equals(object.objectType) : object.objectType == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (objectType != null ? objectType.hashCode() : 0);
        return result;
    }
}
