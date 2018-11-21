package com.ustadmobile.lib.db.entities;

public class WamdaPersonSubjectInfo extends WamdaSubject{

    private boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WamdaPersonSubjectInfo)) return false;

        WamdaPersonSubjectInfo info = (WamdaPersonSubjectInfo) o;

        return selected == info.selected;
    }

    @Override
    public int hashCode() {
        return (selected ? 1 : 0);
    }
}
