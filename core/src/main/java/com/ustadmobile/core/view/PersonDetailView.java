package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

/**
 * View responsible for Person detail view.
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
    void setField(int index, PersonDetailViewField field, Object value);

    /**
     * Set's Class list provider to the person detail page. This is a list within the person's detail
     * depicting every class that person is a member of.
     *
     * @param clazzListProvider     The class list umprovider of ClazzWithNumStudents type.
     */
    void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider );

    /**
     * Handles click call button on the person detail page. Usually fires a call intent on the
     * platform it is implemented in.
     *
     * @param number    The number to call .
     */
    void handleClickCall(String number);

    /**
     * Handles click sms button on the person detail page. Usually fires a send sms intent on the
     * platform it is implemented in.
     *
     * @param number    The number to sms/text
     */
    void handleClickText(String number);

    /**
     * Updates the image from the given image path to the person detail view. Adds the image in the
     * right position.
     *
     * @param imagePath The path of the image to be shown on the view.
     */
    void updateImageOnView(String imagePath);

    /**
     * Clears all person's detail views. Usually called before we render all fields again (ie upon
     * updating the image of the person)
     */
    void clearAllFields();

    /**
     * Handles finishing this view.
     */
    void finish();

    void showFAB(boolean show);

    void showUpdateImageButton(boolean show);

}
