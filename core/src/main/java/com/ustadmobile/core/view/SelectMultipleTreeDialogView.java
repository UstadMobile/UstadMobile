package com.ustadmobile.core.view;


import com.ustadmobile.lib.db.entities.Location;

import java.util.List;

/**
 * SelectMultipleTreeDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SelectMultipleTreeDialogView extends UstadView {

    String VIEW_NAME = "SelectMultipleTreeDialog";

    void populateTopLocation(List<Location> locations, Object treeNode, Object parentNode);

    /**
     * For Android: closes the activity.
     */
    void finish();

}
