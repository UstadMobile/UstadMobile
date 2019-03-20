package com.ustadmobile.core.view;


/**
 * Core View. Screen is for PersonAuthDetail's View
 */
public interface PersonAuthDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "PersonAuthDetail";

    //Any argument keys:
    String ARG_PERSONAUTH_PERSONUID = "PersonAuthPersonUid";

    void updateUsername(String username);

    void sendMessage(int messageId);

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

