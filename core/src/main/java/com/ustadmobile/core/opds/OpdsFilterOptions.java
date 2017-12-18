package com.ustadmobile.core.opds;

import java.util.Vector;

/**
 * Created by mike on 12/16/17.
 */

public class OpdsFilterOptions {

    private Vector filterOptionFields;

    public OpdsFilterOptions() {
        filterOptionFields = new Vector();
    }

    public OpdsFilterOptionField getFilter(int index) {
        return (OpdsFilterOptionField)filterOptionFields.elementAt(index);
    }

    public int getNumOptions() {
        return filterOptionFields.size();
    }

    public void addFilter(OpdsFilterOptionField field) {
        filterOptionFields.addElement(field);
    }


}
