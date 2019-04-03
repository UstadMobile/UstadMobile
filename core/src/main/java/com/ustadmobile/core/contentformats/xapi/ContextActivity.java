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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContextActivity that = (ContextActivity) o;

        if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;
        if (grouping != null ? !grouping.equals(that.grouping) : that.grouping != null)
            return false;
        if (category != null ? !category.equals(that.category) : that.category != null)
            return false;
        return other != null ? other.equals(that.other) : that.other == null;
    }

    @Override
    public int hashCode() {
        int result = parent != null ? parent.hashCode() : 0;
        result = 31 * result + (grouping != null ? grouping.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (other != null ? other.hashCode() : 0);
        return result;
    }
}
