package com.ustadmobile.core.view;


import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

/**
 * Core View. Screen is for GroupDetail's View
 */
public interface GroupDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "GroupDetail";

    //Any argument keys:
    String GROUP_UID = "GroupUid";

    void setListProvider(UmProvider<PersonWithEnrollment> provider);

    void updateGroupOnView(PersonGroup group);

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

