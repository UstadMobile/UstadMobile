package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;


/**
 * Core View. Screen is for UserProfile's View
 */
public interface UserProfileView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "UserProfile";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();

    void updateToolbarTitle(String personName);

    void setLanguageSet(String languageSet);

    void updateImageOnView(String imagePath);

    void addImageFromCamera();

    void addImageFromGallery();


}

