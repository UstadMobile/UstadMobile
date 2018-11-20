package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.core.db.UmProvider;

/**
 * View responsible for recognising other students.
 * SELRecognition Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELRecognitionView extends UstadView {

    //View name
    String VIEW_NAME = "SELRecognition";

    //Arguments:
    String ARG_RECOGNITION_UID = "argRecognitioUid";

    /**
     * Sets Current provider
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param listProvider The provider data
     */
    void setListProvider(UmProvider<Person> listProvider);


    /**
     * Closes the view.
     */
    void finish();

}
