package com.ustadmobile.core.view;

/**
 * View responsible for seeking student consent before commencing SEL tasks.
 * SELSelectConsent Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELSelectConsentView extends UstadView {

    //View name
    String VIEW_NAME = "SELSelectConsent";

    /**
     * Closes the view
     */
    void finish();

    /** Notify a message **/
    void toastMessage(String message);

}
