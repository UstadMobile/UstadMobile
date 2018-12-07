package com.ustadmobile.port.android.view;

import com.toughra.ustadmobile.R;

import tellh.com.recyclertreeview_lib.LayoutItemType;

public class LocationLayoutType implements LayoutItemType {
    public String name;
    public long uid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public LocationLayoutType(String name) {
        this.name = name;
    }

    public LocationLayoutType(String name, Long uid){
        this.name = name;
        this.uid = uid;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_select_multiple_tree_dialog;
    }
}
