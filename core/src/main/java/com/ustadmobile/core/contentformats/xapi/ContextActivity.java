package com.ustadmobile.core.contentformats.xapi;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

@JsonAdapter(ContextDeserializer.class)
public class ContextActivity {

    private List<XObject> parent;

    private List<XObject> grouping;

    private List<XObject> category;

    private List<XObject> other;

    public List<XObject> getParent() {
        return parent;
    }

    public void setParent(List<XObject> parent) {
        this.parent = parent;
    }

    public List<XObject> getGrouping() {
        return grouping;
    }

    public void setGrouping(List<XObject> grouping) {
        this.grouping = grouping;
    }

    public List<XObject> getCategory() {
        return category;
    }

    public void setCategory(List<XObject> category) {
        this.category = category;
    }

    public List<XObject> getOther() {
        return other;
    }

    public void setOther(List<XObject> other) {
        this.other = other;
    }
}
