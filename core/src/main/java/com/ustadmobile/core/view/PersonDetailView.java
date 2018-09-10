package com.ustadmobile.core.view;

import com.ustadmobile.core.controller.PersonDetailPresenter;

/**
 * PersonDetail Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface PersonDetailView extends UstadView {

    String VIEW_NAME = "PersonDetail";

    String ARG_PERSON_UID = "personUid";

    /**
     * Sets every field to the view (mostly a Linear Layout) based on the index and the
     * PersonDetailViewField object.
     *
     * @param index The index where the field should go in the (Linear) layout
     * @param field The PersonDetailViewField field representation that has its id, type label & options
     * @param value The value of the field to be set to the view.
     */
    void setField(int index, PersonDetailPresenter.PersonDetailViewField field, Object value);

}
