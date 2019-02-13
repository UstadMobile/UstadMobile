package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

public interface PersonListSearchView extends UstadView {

    String VIEW_NAME = "PersonListSearch";

    String ARGUMENT_CURRNET_CLAZZ_UID = "PersonListSearchCurrentclazzUid";
    /**
     * Set people list provider to the view.
     *
     * @param listProvider  The people list of PersonWithEnrollment type
     */
    void setPeopleListProvider(UmProvider<PersonWithEnrollment> listProvider);
}
