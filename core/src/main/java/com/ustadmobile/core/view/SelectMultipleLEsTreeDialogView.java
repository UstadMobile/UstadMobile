package com.ustadmobile.core.view;


import com.ustadmobile.lib.db.entities.Person;

import java.util.List;

/**
 * SelectMultipleTreeDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SelectMultipleLEsTreeDialogView extends UstadView {

    String VIEW_NAME = "SelectMultipleLEsTreeDialog";

    String ARG_LE_SET = "LEsSelected";

    void populateTopLEs(List<Person> locations);

    void setTitle(String title);

    /**
     * For Android: closes the activity.
     */
    void finish();

}
