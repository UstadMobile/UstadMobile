package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

public interface SelectPeopleDialogView extends UstadView {

    String VIEW_NAME="SelectPeopleDialogView";
    String ARG_SELECTED_PEOPLE = "SelectedPeople";
    void finish();

    void setPeopleProvider(UmProvider<PersonWithEnrollment> peopleListProvider);
}
