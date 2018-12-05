package com.ustadmobile.port.android.view;

import com.toughra.ustadmobile.R;

import tellh.com.recyclertreeview_lib.LayoutItemType;

public class LocationLayoutType implements LayoutItemType {
    public String name;

    public LocationLayoutType(String name) {
        this.name = name;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_select_multiple_tree_dialog;
    }
}
