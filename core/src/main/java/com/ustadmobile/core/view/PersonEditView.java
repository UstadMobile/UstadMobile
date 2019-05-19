package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.lib.db.entities.CustomField;

/**
 * View for the editing a person - responsible for creating edit fields with the right edit types
 * as well as custom fields. PersonEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface PersonEditView extends UstadView {

    String VIEW_NAME = "PersonEdit";

    int IMAGE_MAX_HEIGHT = 1024;
    int IMAGE_MAX_WIDTH = 1024;
    int IMAGE_QUALITY = 75;

    /**
     * Sets every field to the view (mostly a Linear Layout) based on the index and the
     * PersonDetailViewField object.
     *
     * @param index The index where the field should go in the (Linear) layout
     * @param field The PersonDetailViewField field representation that has its id, type label & options
     * @param value The value of the field to be set to the view.
     */
    void setField(int index, long fieldUid, PersonDetailViewField field, Object value);

    /**
     * Sets the class list um provider to the view.
     *
     * @param clazzListProvider     The class list provider of ClazzWithNumStudents type
     */
    void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider );

    /**
     * This will close the activity (and finish it)
     */
    void finish();

    /**
     * This will update the toolbar's title
     *
     * @param titleName The title string
     */
    void updateToolbarTitle(String titleName);

    /**
     * Starts the camera intent - this will save the image in the view to be used later.
     */
    void addImageFromCamera();

    /**
     * Updates the image path on the view.
     *
     * @param imagePath     The image path to be updated on the view.
     */
    void updateImageOnView(String imagePath);

    /**
     * Clears all fields in the edit view - usually called when the image view gets updated and
     * any of the edit fields gets updated - ie the whole edit screen gets rendered again.
     */
    void clearAllFields();

    void addCustomFieldText(CustomField label, String value);

    void addCustomFieldDropdown(CustomField label, String[] options, int selected);

    void clearAllCustomFields();


}
