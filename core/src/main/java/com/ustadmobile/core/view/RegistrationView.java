package com.ustadmobile.core.view;

/**
 * Created by varuna on 7/28/2017.
 */

public interface  RegistrationView extends UstadView, DismissableDialog {

    //The view name
    String VIEW_NAME = "RegistrationView";

    /**
     * Adds Field
     * @param fieldName Name corresponding to String
     * @param fieldType Extra info on this field type.
     */
    void addField(int fieldName, int fieldType, String[] options) ;

}
