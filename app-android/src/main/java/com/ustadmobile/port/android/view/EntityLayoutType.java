package com.ustadmobile.port.android.view;

import com.toughra.ustadmobile.R;

import tellh.com.recyclertreeview_lib.LayoutItemType;

/**
 * This Layout Class represents every entry part of the Multi select recycler view.
 * This is the data that will get fed into the recycler view and hence would need to be
 * updated with at least the name and uid. Selected and leaf are navigation properties.
 */
public class EntityLayoutType implements LayoutItemType {
    public String name;
    public long uid;
    public boolean selected;
    public boolean leaf;

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

    public EntityLayoutType(String name, Long uid, boolean selected, boolean leaf){
        this.name = name;
        this.uid = uid;
        this.selected = selected;
        this.leaf = leaf;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_select_multiple_tree_dialog;
    }
}
